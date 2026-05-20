import os
import json
import psycopg2
import logging
from hashlib import md5
from shapely.geometry import Polygon, MultiPoint

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

def compute_centroid(geometry_str):
    """
    Parses a geometry string like '[[105.8, 21.0], [105.81, 21.01], ...]'
    and returns (lat, lng) as the centroid using shapely.
    Note: The input is [lng, lat].
    """
    try:
        coords = json.loads(geometry_str)
        if not coords or not isinstance(coords, list):
            return None, None
        
        valid_coords = [pt for pt in coords if isinstance(pt, list) and len(pt) >= 2]
        if not valid_coords:
            return None, None
            
        if len(valid_coords) >= 3:
            # Create a Polygon (Shapely automatically closes the boundary if not already closed)
            geom = Polygon(valid_coords)
        else:
            # Fallback to MultiPoint for 1 or 2 coordinate pairs
            geom = MultiPoint(valid_coords)
            
        centroid = geom.centroid
        return centroid.y, centroid.x  # returns (lat, lng)
    except Exception as e:
        logger.error(f"Error parsing geometry with shapely: {e}")
        return None, None

def sync_pois():
    conn = get_db_connection()
    cursor = conn.cursor()
    
    logger.info("Fetching active buildings from context.buildings...")
    cursor.execute("""
        SELECT id, name, coordinates 
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
    for b_id, b_name, coordinates in rows:
        lat, lng = compute_centroid(coordinates)
        if lat is None or lng is None:
            continue
            
        new_pois[str(valid_count)] = {
            "db_uuid": str(b_id),
            "name": b_name,
            "lat": lat,
            "lng": lng
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
