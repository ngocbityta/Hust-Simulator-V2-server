import os
import json
import psycopg2
import logging
from hashlib import md5

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
logger = logging.getLogger("POISync")

DB_HOST     = os.environ.get('POSTGRES_HOST',     'localhost')
DB_NAME     = os.environ.get('POSTGRES_DB',       'neondb')
DB_USER     = os.environ.get('POSTGRES_USER',     'neondb_owner')
DB_PASSWORD = os.environ.get('POSTGRES_PASSWORD', 'password')
DB_PORT     = os.environ.get('POSTGRES_PORT',     '5432')
DB_SSLMODE  = os.environ.get('POSTGRES_SSL',      'disable')

POIS_JSON_PATH = 'pois.json'

def get_db_connection():
    return psycopg2.connect(
        host=DB_HOST,
        database=DB_NAME,
        user=DB_USER,
        password=DB_PASSWORD,
        port=DB_PORT,
        sslmode=DB_SSLMODE
    )


def sync_pois():
    conn = get_db_connection()
    cursor = conn.cursor()
    
    logger.info("Fetching active buildings from context.buildings...")
    cursor.execute("""
        SELECT id, name, centroid_lat, centroid_lng, category, coordinates
        FROM context.buildings 
        WHERE is_active = true 
        ORDER BY id ASC
    """)
    rows = cursor.fetchall()
    cursor.close()
    conn.close()
    
    if not rows:
        logger.warning("No active buildings found in database!")
        return False
        
    new_pois = {}
    valid_count = 0
    for b_id, b_name, lat, lng, category, coordinates in rows:
        if lat is None or lng is None:
            continue
            
        new_pois[str(valid_count)] = {
            "db_uuid": str(b_id),
            "name": b_name,
            "lat": lat,
            "lng": lng,
            "category": category or "CLASSROOM",
            "polygon_coords": json.loads(coordinates) if coordinates else []
        }
        valid_count += 1
        
    logger.info(f"Processed {len(new_pois)} buildings.")
    
    # Check if changed by computing MD5 of current and new POIs
    current_hash = ""
    if os.path.exists(POIS_JSON_PATH):
        with open(POIS_JSON_PATH, 'r', encoding='utf-8') as f:
            current_hash = md5(f.read().encode('utf-8')).hexdigest()
            
    # Serialize new POIs
    new_json_str = json.dumps(new_pois, ensure_ascii=False, indent=4)
    new_hash = md5(new_json_str.encode('utf-8')).hexdigest()
    
    if current_hash != new_hash:
        logger.info("POIs have changed. Updating pois.json...")
        with open(POIS_JSON_PATH, 'w', encoding='utf-8') as f:
            f.write(new_json_str)
        return True # Return True to indicate a change happened (requires retraining)
    else:
        logger.info("POIs have not changed.")
        return False

if __name__ == '__main__':
    # When run directly, exit code 1 means POIs changed (needs retrain), 0 means no change.
    changed = sync_pois()
    if changed:
        exit(1)
    else:
        exit(0)
