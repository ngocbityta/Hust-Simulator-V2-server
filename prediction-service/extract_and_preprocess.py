"""
ETL: Extract GPS journey data from PostgreSQL → Check-in sequences for STTF-Recommender.

STTF input format: each row = one check-in = (journey_id, step, user_id, poi_id, hour_of_week, target_poi)
- poi_id       : integer index of nearest POI (GPS → POI snapping)
- hour_of_week : int [0..167] = day_of_week*24 + hour  (weekly periodicity, paper §4.1)
- target_poi   : POI id of the FINAL destination of this journey (label for next-location prediction)

Reference: ijgi-12-00079, Section 3.1 (User Trajectory definition)
"""

import os
import json
import math
import psycopg2
import pandas as pd
from datetime import datetime
from poi_config import POIS, NUM_POIS, POI_IDS_SORTED

# DB Config
DB_HOST     = os.environ.get('POSTGRES_HOST',     'localhost')
DB_NAME     = os.environ.get('POSTGRES_DB',       'neondb')
DB_USER     = os.environ.get('POSTGRES_USER',     'neondb_owner')
DB_PASSWORD = os.environ.get('POSTGRES_PASSWORD', 'password')
DB_PORT     = os.environ.get('POSTGRES_PORT',     '5432')
DB_SSLMODE  = os.environ.get('POSTGRES_SSL',      'disable')

THRESHOLD      = 100          # Min new records before retraining
LAST_SYNC_FILE = 'last_sync.txt'
MIN_CHECKINS   = 3            # Paper: discard trajectories with < min check-ins
CHECK_IN_RADIUS_M = 80        # Metres — GPS point must be this close to snap to POI

# ===================== Spatial Utilities =====================
def haversine_m(lat1, lng1, lat2, lng2):
    """Distance in metres between two GPS coordinates."""
    R = 6_371_000
    phi1, phi2 = math.radians(lat1), math.radians(lat2)
    dphi    = math.radians(lat2 - lat1)
    dlambda = math.radians(lng2 - lng1)
    a = math.sin(dphi / 2)**2 + math.cos(phi1) * math.cos(phi2) * math.sin(dlambda / 2)**2
    return R * 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))

def snap_to_poi(lat, lng):
    """Return (poi_id, dist_m) of nearest POI, or (None, dist) if outside radius."""
    best_id, best_dist = None, float('inf')
    for poi_id, poi in POIS.items():
        d = haversine_m(lat, lng, poi['lat'], poi['lng'])
        if d < best_dist:
            best_dist, best_id = d, poi_id
    return (best_id, best_dist) if best_dist <= CHECK_IN_RADIUS_M else (None, best_dist)

def gps_to_checkin_seq(gps_points, timestamps):
    checkins = []
    last_poi = None
    for pt, ts in zip(gps_points, timestamps):
        poi_id, _ = snap_to_poi(pt['lat'], pt['lng'])
        if poi_id is None:
            continue
        # Deduplicate: skip same consecutive POI
        if poi_id == last_poi:
            continue
        hour_of_week = ts.weekday() * 24 + ts.hour   # [0..167]
        checkins.append((poi_id, hour_of_week))
        last_poi = poi_id
    return checkins


# ===================== DB Helpers =====================
def get_db_connection():
    return psycopg2.connect(
        host=DB_HOST, database=DB_NAME, user=DB_USER,
        password=DB_PASSWORD, port=DB_PORT, sslmode=DB_SSLMODE,
    )

# Reverse lookup mapping from building UUID to lat/lng coordinates
UUID_TO_COORDS = {}
for poi in POIS.values():
    if 'db_uuid' in poi:
        UUID_TO_COORDS[poi['db_uuid']] = {'lat': poi['lat'], 'lng': poi['lng']}

def check_new_data():
    last_sync = '1970-01-01 00:00:00'
    if os.path.exists(LAST_SYNC_FILE):
        with open(LAST_SYNC_FILE, 'r') as f:
            last_sync = f.read().strip()

    conn   = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("""
        SELECT 
            (SELECT COUNT(*) FROM social.journey_items WHERE created_at > %s AND type = 'CHECKIN') +
            (SELECT COUNT(*) FROM prediction.checkin_sequences WHERE created_at > %s)
    """, (last_sync, last_sync))
    count = cursor.fetchone()[0]
    cursor.close(); conn.close()
    return count, last_sync


# ===================== ETL Main =====================
def extract_and_preprocess(last_sync):
    conn   = get_db_connection()
    cursor = conn.cursor()

    print(f"Extracting journey data since {last_sync} ...")

    # Union active check-ins (social schema) and passive check-ins (prediction schema)
    # For passive check-ins, we group by user and date, synthesize a deterministic journey ID,
    # and pass the POI UUID as the metadata value.
    cursor.execute("""
        SELECT j.id::text       AS journey_id,
               j.user_id::text  AS user_id,
               ji.metadata::text,
               ji.timestamp,
               ji.created_at
        FROM   social.journey_items ji
        JOIN   social.journeys j ON ji.journey_id = j.id
        WHERE  ji.type = 'CHECKIN' AND ji.created_at > %s

        UNION ALL

        SELECT md5(user_id::text || timestamp::date::text)::uuid::text AS journey_id,
               user_id::text,
               poi_id::text AS metadata,
               timestamp,
               created_at
        FROM   prediction.checkin_sequences
        WHERE  created_at > %s

        ORDER  BY journey_id, timestamp ASC
    """, (last_sync, last_sync))

    rows = cursor.fetchall()
    cursor.close(); conn.close()

    if not rows:
        print("No rows found.")
        return False

    max_created_at = max(row[4] for row in rows)

    # Group GPS points by journey
    journeys = {}   # journey_id → {user_id, gps_points, timestamps}
    for journey_id, user_id, metadata, ts, _ in rows:
        lat, lng = 0.0, 0.0
        if metadata:
            try:
                # Try to parse as JSON first (active checkin)
                meta_json = json.loads(metadata)
                lat = float(meta_json.get('lat', meta_json.get('latitude', 0)))
                lng = float(meta_json.get('lng', meta_json.get('longitude', 0)))
            except (json.JSONDecodeError, TypeError):
                # If not JSON, it's a POI UUID (passive checkin stay point)
                if metadata in UUID_TO_COORDS:
                    lat = UUID_TO_COORDS[metadata]['lat']
                    lng = UUID_TO_COORDS[metadata]['lng']
                else:
                    # Skip if POI is not active/found in pois.json
                    continue

        if journey_id not in journeys:
            journeys[journey_id] = {'user_id': user_id, 'points': [], 'timestamps': []}
        journeys[journey_id]['points'].append({'lat': lat, 'lng': lng})
        journeys[journey_id]['timestamps'].append(ts)

    # Convert GPS → check-in sequences
    records = []
    skipped = 0
    for journey_id, jdata in journeys.items():
        checkins = gps_to_checkin_seq(jdata['points'], jdata['timestamps'])

        # Paper: discard trajectories that are too short
        if len(checkins) < MIN_CHECKINS:
            skipped += 1
            continue

        target_poi = checkins[-1][0]   # Final destination = training label

        for step, (poi_id, hour_of_week) in enumerate(checkins):
            records.append({
                'journey_id'   : journey_id,
                'step'         : step,
                'user_id'      : jdata['user_id'],
                'poi_id'       : poi_id,
                'hour_of_week' : hour_of_week,
                'target_poi'   : target_poi,
            })

    if not records:
        print(f"No valid check-in sequences (skipped {skipped} short journeys).")
        return False

    df = pd.DataFrame(records)
    os.makedirs('data', exist_ok=True)
    out_path = 'data/checkin_sequences.csv'
    df.to_csv(out_path, index=False)
    print(f"Saved {len(journeys) - skipped} journeys "
          f"({len(records)} check-in records) → {out_path}")
    print(f"Skipped {skipped} journeys with < {MIN_CHECKINS} check-ins.")

    # Update sync timestamp
    with open(LAST_SYNC_FILE, 'w') as f:
        f.write(max_created_at.strftime('%Y-%m-%d %H:%M:%S'))

    return True



if __name__ == '__main__':
    count, last_sync = check_new_data()
    print(f"New location records since last sync: {count}")
    if count >= THRESHOLD:
        success = extract_and_preprocess(last_sync)
        if success:
            print("ETL complete. Run train.py to retrain STTF-Recommender.")
    else:
        print(f"Threshold not met ({count} < {THRESHOLD}). Skipping extraction.")
