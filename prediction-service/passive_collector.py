import os
import redis
import json
import uuid
import math
import psycopg2
import logging
from datetime import datetime, timezone

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger("PassiveCollector")

# Redis Config
REDIS_HOST     = os.environ.get('REDIS_HOST',     'localhost')
REDIS_PORT     = int(os.environ.get('REDIS_PORT', 6379))
REDIS_PASSWORD = os.environ.get('REDIS_PASSWORD', 'hustsim_redis')

# DB Config
DB_HOST     = os.environ.get('POSTGRES_HOST',     'localhost')
DB_NAME     = os.environ.get('POSTGRES_DB',       'neondb')
DB_USER     = os.environ.get('POSTGRES_USER',     'neondb_owner')
DB_PASSWORD = os.environ.get('POSTGRES_PASSWORD', 'password')
DB_PORT     = os.environ.get('POSTGRES_PORT',     '5432')
DB_SSLMODE  = os.environ.get('POSTGRES_SSL',      'disable')

# Stay Point Parameters
DIST_THRESHOLD_M = 30.0  # Maximum radius of stay cluster
TIME_THRESHOLD_S = 300.0  # Minimum stay duration (5 minutes)
POI_SNAP_RADIUS_M = 10.0  # Snapping radius to a building

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
        return json.load(f)

def detect_stay_points(points, dist_threshold=DIST_THRESHOLD_M, time_threshold=TIME_THRESHOLD_S):
    """
    Stay Point Detection Algorithm.
    points: list of dicts with keys 'lat', 'lng', 'time' (datetime object)
    Returns: list of dicts with keys 'lat', 'lng', 'start_time', 'end_time'
    """
    stay_points = []
    i = 0
    n = len(points)
    
    while i < n:
        j = i + 1
        while j < n:
            # Calculate distance between starting point and subsequent points
            dist = haversine_m(points[i]['lat'], points[i]['lng'], points[j]['lat'], points[j]['lng'])
            if dist > dist_threshold:
                break
            j += 1
            
        # Check if the time difference between the first and last point in the cluster exceeds threshold
        time_diff = (points[j - 1]['time'] - points[i]['time']).total_seconds()
        if time_diff >= time_threshold:
            # Compute centroid of the cluster
            cluster = points[i:j]
            mean_lat = sum(p['lat'] for p in cluster) / len(cluster)
            mean_lng = sum(p['lng'] for p in cluster) / len(cluster)
            
            stay_points.append({
                'lat': mean_lat,
                'lng': mean_lng,
                'start_time': points[i]['time'],
                'end_time': points[j - 1]['time']
            })
            i = j  # Skip past this stay point cluster
        else:
            i += 1
            
    return stay_points

def snap_to_poi(lat, lng, pois):
    """Find the nearest active building POI within snapping radius."""
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
    logger.info("Starting passive trajectory collection...")
    
    # 1. Connect to Redis
    try:
        r = redis.Redis(host=REDIS_HOST, port=REDIS_PORT, password=REDIS_PASSWORD, decode_responses=True)
        keys = r.keys("trajectory:*")
        logger.info(f"Found {len(keys)} active user trajectories in Redis.")
    except Exception as e:
        logger.error(f"Failed to connect to Redis: {e}")
        return
        
    if not keys:
        logger.info("No trajectories to process.")
        return
        
    # 2. Load POIs
    pois = load_pois()
    if not pois:
        logger.error("Skipping passive collection because POIs could not be loaded.")
        return

    checkins_to_insert = []
    
    # 3. Process each trajectory
    for key in keys:
        user_id = key.split(":")[1]
        
        try:
            # Fetch all points from Redis list
            points_str = r.lrange(key, 0, -1)
            if not points_str:
                continue
                
            raw_points = []
            for p_str in points_str:
                p = json.loads(p_str)
                # Convert milliseconds timestamp to datetime
                dt = datetime.fromtimestamp(p['timestamp'] / 1000.0, tz=timezone.utc)
                raw_points.append({
                    'lat': p['latitude'],
                    'lng': p['longitude'],
                    'time': dt
                })
                
            # Sort explicitly by timestamp in ascending order (chronological) for the algorithm
            raw_points.sort(key=lambda x: x['time'])
            
            # Run Stay Point Detection
            stay_points = detect_stay_points(raw_points)
            if stay_points:
                logger.info(f"User {user_id}: Detected {len(stay_points)} stay points.")
                
            for sp in stay_points:
                # Snap stay point to POI
                snapped_poi, dist = snap_to_poi(sp['lat'], sp['lng'], pois)
                if snapped_poi:
                    poi_uuid = snapped_poi['db_uuid']
                    poi_name = snapped_poi['name']
                    
                    # Deterministic ID generation based on user_id, building UUID, and hour of stay
                    # This guarantees idempotency: multiple runs within the same hour won't duplicate stay point
                    time_rounded = sp['start_time'].replace(minute=0, second=0, microsecond=0)
                    deterministic_id = str(uuid.uuid5(
                        uuid.NAMESPACE_DNS, 
                        f"{user_id}_{poi_uuid}_{time_rounded.isoformat()}"
                    ))
                    
                    checkins_to_insert.append((
                        deterministic_id,
                        user_id,
                        poi_uuid,
                        sp['start_time'].replace(tzinfo=None) # Strip timezone for PostgreSQL timestamp
                    ))
                    logger.debug(f"Stay detected for user {user_id} at {poi_name} (dist: {dist:.1f}m).")
        except Exception as e:
            logger.error(f"Error processing trajectory for user {user_id}: {e}")
            
    if not checkins_to_insert:
        logger.info("No new stay points snapped to valid buildings. Passive collection complete.")
        return
        
    # 4. Insert into NeonDB
    try:
        conn = psycopg2.connect(
            host=DB_HOST, database=DB_NAME, user=DB_USER,
            password=DB_PASSWORD, port=DB_PORT, sslmode=DB_SSLMODE
        )
        cursor = conn.cursor()
        
        logger.info(f"Inserting/Updating {len(checkins_to_insert)} stay points into prediction.checkin_sequences...")
        
        # Using ON CONFLICT DO NOTHING to ensure absolute idempotency
        cursor.executemany("""
            INSERT INTO prediction.checkin_sequences (id, user_id, poi_id, timestamp)
            VALUES (%s, %s, %s, %s)
            ON CONFLICT (id) DO NOTHING;
        """, checkins_to_insert)
        
        conn.commit()
        inserted_count = cursor.rowcount
        cursor.close()
        conn.close()
        logger.info(f"Successfully processed passive collection. Rows affected: {inserted_count}.")
    except Exception as e:
        logger.error(f"Failed to write passive checkins to database: {e}")

if __name__ == '__main__':
    collect_and_process()
