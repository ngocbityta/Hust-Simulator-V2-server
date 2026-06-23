import os
import json
import uuid
import math
import psycopg2
import logging
from datetime import datetime, timezone, timedelta
from shapely.geometry import Point, Polygon
from shapely.validation import make_valid

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger("PassiveCollector")

# DB Config
DB_HOST     = os.environ.get('POSTGRES_HOST',     'localhost')
DB_NAME     = os.environ.get('POSTGRES_DB',       'neondb')
DB_USER     = os.environ.get('POSTGRES_USER',     'neondb_owner')
DB_PASSWORD = os.environ.get('POSTGRES_PASSWORD', 'password')
DB_PORT     = os.environ.get('POSTGRES_PORT',     '5432')
DB_SSLMODE  = os.environ.get('POSTGRES_SSL',      'disable')

# Stay Point Parameters
DIST_THRESHOLD_M = 15.0   # Reduced to 15m for tighter clustering
TIME_THRESHOLD_S = 300.0  # Minimum stay duration (5 minutes)
POI_SNAP_RADIUS_M = 10.0  # Fallback snapping radius to centroid

def haversine_m(lat1, lng1, lat2, lng2):
    """Return distance in meters between two GPS coordinates."""
    R = 6_371_000
    phi1, phi2 = math.radians(lat1), math.radians(lat2)
    dphi  = math.radians(lat2 - lat1)
    dlambda = math.radians(lng2 - lng1)
    a = math.sin(dphi / 2) ** 2 + math.cos(phi1) * math.cos(phi2) * math.sin(dlambda / 2) ** 2
    return R * 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))

def load_pois():
    pois_path = os.path.join(os.path.dirname(__file__), 'pois.json')
    if not os.path.exists(pois_path):
        logger.error(f"pois.json not found at {pois_path}!")
        return {}
    with open(pois_path, 'r', encoding='utf-8') as f:
        pois = json.load(f)
        
    # Pre-compute shapely polygons for PiP checking
    for key, poi in pois.items():
        coords = poi.get("polygon_coords", [])
        if coords and len(coords) >= 3:
            try:
                # Expecting coordinates as [lng, lat] since it usually comes from GeoJSON
                poly = Polygon(coords)
                if not poly.is_valid:
                    poly = make_valid(poly)
                poi['shapely_poly'] = poly
            except Exception as e:
                logger.debug(f"Invalid polygon for POI {poi.get('name')}: {e}")
                poi['shapely_poly'] = None
        else:
            poi['shapely_poly'] = None
            
    return pois

def detect_stay_points(points, dist_threshold=DIST_THRESHOLD_M, time_threshold=TIME_THRESHOLD_S, max_tolerance_points=3):
    """
    Stay Point Detection Algorithm with Spike Tolerance.
    points: list of dicts with keys 'lat', 'lng', 'time' (datetime object)
    """
    stay_points = []
    i = 0
    n = len(points)
    
    while i < n:
        j = i + 1
        spike_count = 0
        valid_cluster_points = [points[i]]
        
        while j < n:
            dist = haversine_m(points[i]['lat'], points[i]['lng'], points[j]['lat'], points[j]['lng'])
            if dist > dist_threshold:
                spike_count += 1
                if spike_count > max_tolerance_points:
                    # Broken completely. Rollback j to before the spikes started.
                    j = j - spike_count
                    break
                # It's a spike (within tolerance limit), we skip adding it to valid points
            else:
                # User is back within radius, reset spike count!
                spike_count = 0
                valid_cluster_points.append(points[j])
            j += 1
            
        if len(valid_cluster_points) > 0:
            time_diff = (valid_cluster_points[-1]['time'] - valid_cluster_points[0]['time']).total_seconds()
            if time_diff >= time_threshold:
                # Compute centroid ignoring the noisy spikes
                mean_lat = sum(p['lat'] for p in valid_cluster_points) / len(valid_cluster_points)
                mean_lng = sum(p['lng'] for p in valid_cluster_points) / len(valid_cluster_points)
                
                stay_points.append({
                    'lat': mean_lat,
                    'lng': mean_lng,
                    'start_time': valid_cluster_points[0]['time'],
                    'end_time': valid_cluster_points[-1]['time']
                })
                i = j  
            else:
                i += 1
        else:
            i += 1
            
    return stay_points

def snap_to_poi(lat, lng, pois):
    """Find the POI by Point-in-Polygon, fallback to nearest centroid."""
    pt = Point(lng, lat)  # Shapely uses (x, y) = (lng, lat)
    
    # 1. Exact Point-in-Polygon check
    for key, poi in pois.items():
        poly = poi.get('shapely_poly')
        if poly and poly.contains(pt):
            return poi, 0.0  # Inside building!
            
    # 2. Fallback to centroid distance
    best_poi = None
    best_dist = float('inf')
    
    for key, poi in pois.items():
        dist = haversine_m(lat, lng, poi['lat'], poi['lng'])
        if dist < best_dist:
            best_dist = dist
            best_poi = poi
            
    if best_dist <= POI_SNAP_RADIUS_M:
        return best_poi, best_dist
    return None, best_dist

def collect_and_process():
    logger.info("Starting passive trajectory collection sync from user_locations...")
    
    pois = load_pois()
    if not pois:
        logger.error("Skipping collection because POIs could not be loaded.")
        return

    try:
        conn = psycopg2.connect(
            host=DB_HOST, database=DB_NAME, user=DB_USER,
            password=DB_PASSWORD, port=DB_PORT, sslmode=DB_SSLMODE
        )
        cursor = conn.cursor()
    except Exception as e:
        logger.error(f"Failed to connect to Database: {e}")
        return

    try:
        cursor.execute("SELECT last_sync_time FROM prediction.sync_state WHERE id = 'passive_collector'")
        row = cursor.fetchone()
        
        if row:
            last_sync_time = row[0]
        else:
            last_sync_time = datetime(2000, 1, 1)
            cursor.execute("INSERT INTO prediction.sync_state (id, last_sync_time) VALUES ('passive_collector', %s)", (last_sync_time,))
            conn.commit()

        run_start_time = datetime.now(timezone.utc).replace(tzinfo=None)
        overlap_time = last_sync_time - timedelta(hours=4)
        logger.info(f"Syncing user_locations since {overlap_time} (last_sync: {last_sync_time})")

        # Fetch active map bounding box
        cursor.execute("SELECT coordinates FROM context.virtual_maps WHERE is_active = true LIMIT 1")
        map_row = cursor.fetchone()
        
        if not map_row:
            logger.error("No active virtual map found in context.virtual_maps. Cannot determine bounding box.")
            cursor.close()
            conn.close()
            return
            
        try:
            coords_json = json.loads(map_row[0])
            lats = []
            lngs = []
            for pt in coords_json:
                if isinstance(pt, dict):
                    lats.append(float(pt.get('lat', pt.get('latitude', 0))))
                    lngs.append(float(pt.get('lng', pt.get('longitude', 0))))
                elif isinstance(pt, list) and len(pt) >= 2:
                    if pt[0] > 50: # [lng, lat]
                        lngs.append(float(pt[0]))
                        lats.append(float(pt[1]))
                    else:
                        lats.append(float(pt[0]))
                        lngs.append(float(pt[1]))
            
            lat_min, lat_max = min(lats), max(lats)
            lng_min, lng_max = min(lngs), max(lngs)
        except Exception as e:
            logger.error(f"Failed to parse map coordinates to compute bounding box: {e}")
            cursor.close()
            conn.close()
            return

        cursor.execute("""
            SELECT user_id::text, latitude, longitude, timestamp 
            FROM social.user_locations 
            WHERE timestamp >= %s 
              AND latitude BETWEEN %s AND %s 
              AND longitude BETWEEN %s AND %s
            ORDER BY timestamp ASC
        """, (overlap_time, lat_min, lat_max, lng_min, lng_max))
        
        rows = cursor.fetchall()
        if not rows:
            logger.info("No new trajectory points found in the bounding box.")
            cursor.execute("""
                UPDATE prediction.sync_state 
                SET last_sync_time = %s 
                WHERE id = 'passive_collector'
            """, (run_start_time,))
            conn.commit()
            cursor.close()
            conn.close()
            return
            
        logger.info(f"Found {len(rows)} raw GPS points in campus.")

        trajectories = {}
        for user_id, lat, lng, ts in rows:
            if user_id not in trajectories:
                trajectories[user_id] = []
            trajectories[user_id].append({
                'lat': lat,
                'lng': lng,
                'time': ts
            })

        # ── Merge window: if user's last checkin is at the same POI within
        #    this window, we skip inserting a duplicate record.
        MERGE_WINDOW_S = 7200  # 2 hours

        # Pre-fetch each user's latest checkin from DB for dedup
        user_last_checkin = {}
        user_ids_list = list(trajectories.keys())
        if user_ids_list:
            placeholders = ','.join(['%s'] * len(user_ids_list))
            cursor.execute(f"""
                SELECT DISTINCT ON (user_id)
                       user_id::text, poi_id::text, timestamp, duration_seconds
                FROM   prediction.checkin_sequences
                WHERE  user_id::text IN ({placeholders})
                ORDER  BY user_id, timestamp DESC
            """, user_ids_list)
            for uid, pid, ts, dur in cursor.fetchall():
                user_last_checkin[uid] = {'poi_id': pid, 'timestamp': ts, 'duration': dur or 0}

        checkins_to_insert = []
        skipped_merge = 0
        
        for user_id, points in trajectories.items():
            stay_points = detect_stay_points(points)
            if stay_points:
                logger.info(f"User {user_id}: Detected {len(stay_points)} stay points.")

            # Track the "running" last checkin for this user so that
            # consecutive stay points within the same sync batch are
            # also deduplicated against each other.
            last_checkin = user_last_checkin.get(user_id)

            for sp in stay_points:
                snapped_poi, dist = snap_to_poi(sp['lat'], sp['lng'], pois)
                if not snapped_poi:
                    continue

                poi_uuid = snapped_poi['db_uuid']
                poi_name = snapped_poi['name']

                # ── Dedup: skip if same POI as the last checkin and
                #    within the merge window ──
                if last_checkin and last_checkin['poi_id'] == poi_uuid:
                    gap = abs((sp['start_time'] - last_checkin['timestamp']).total_seconds())
                    if gap <= MERGE_WINDOW_S:
                        skipped_merge += 1
                        # Extend the duration of the existing record
                        new_end = sp['end_time'].replace(tzinfo=None)
                        original_start = last_checkin['timestamp'] - timedelta(seconds=last_checkin.get('duration', 0))
                        extended_duration = int((new_end - original_start).total_seconds())
                        if extended_duration > last_checkin.get('duration', 0):
                            cursor.execute("""
                                UPDATE prediction.checkin_sequences
                                SET duration_seconds = %s
                                WHERE user_id::text = %s AND poi_id::text = %s AND timestamp = %s
                            """, (extended_duration, user_id, poi_uuid, last_checkin['timestamp']))
                            logger.debug(
                                f"Merging: user {user_id} still at {poi_name}, "
                                f"extended duration to {extended_duration}s."
                            )
                        last_checkin['timestamp'] = last_checkin['timestamp']  # keep original timestamp
                        last_checkin['duration'] = extended_duration
                        continue

                time_rounded = sp['start_time'].replace(minute=0, second=0, microsecond=0)
                deterministic_id = str(uuid.uuid5(
                    uuid.NAMESPACE_DNS, 
                    f"{user_id}_{poi_uuid}_{time_rounded.isoformat()}"
                ))
                
                duration_s = int((sp['end_time'] - sp['start_time']).total_seconds())

                checkins_to_insert.append((
                    deterministic_id,
                    user_id,
                    poi_uuid,
                    sp['start_time'].replace(tzinfo=None),
                    duration_s
                ))
                logger.debug(f"Stay detected for user {user_id} at {poi_name} ({duration_s}s, dist: {dist:.1f}m).")

                # Update running tracker
                last_checkin = {'poi_id': poi_uuid, 'timestamp': sp['start_time'].replace(tzinfo=None), 'duration': duration_s}
                user_last_checkin[user_id] = last_checkin

        if skipped_merge:
            logger.info(f"Skipped {skipped_merge} duplicate stay points (merged with previous checkin at same POI).")

        if checkins_to_insert:
            logger.info(f"Inserting/Updating {len(checkins_to_insert)} stay points into prediction.checkin_sequences...")
            cursor.executemany("""
                INSERT INTO prediction.checkin_sequences (id, user_id, poi_id, timestamp, duration_seconds)
                VALUES (%s, %s, %s, %s, %s)
                ON CONFLICT (id) DO UPDATE SET duration_seconds = EXCLUDED.duration_seconds;
            """, checkins_to_insert)
            
            inserted_count = cursor.rowcount
            logger.info(f"Successfully processed passive collection. Rows affected: {inserted_count}.")
        else:
            logger.info("No new stay points snapped to valid buildings.")

        cursor.execute("""
            UPDATE prediction.sync_state 
            SET last_sync_time = %s 
            WHERE id = 'passive_collector'
        """, (run_start_time,))
        
        conn.commit()
        logger.info(f"Sync complete. Updated last_sync_time to {run_start_time}.")

    except Exception as e:
        logger.error(f"Failed during sync process: {e}")
        if 'conn' in locals() and conn:
            conn.rollback()
    finally:
        if 'cursor' in locals() and cursor:
            cursor.close()
        if 'conn' in locals() and conn:
            conn.close()

if __name__ == '__main__':
    collect_and_process()
