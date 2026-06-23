import grpc
from concurrent import futures
import time
import logging
import math
import os
import json
import psycopg2
from psycopg2.extras import RealDictCursor
from datetime import datetime, timezone

# Import generated classes
import prediction_pb2
import prediction_pb2_grpc
from poi_config import POIS, NUM_POIS, POI_IDS_SORTED

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
logger = logging.getLogger('PredictionService')

# Database configs
DB_HOST = os.environ.get('POSTGRES_HOST', 'localhost')
DB_USER = os.environ.get('POSTGRES_USER', 'neondb_owner')
DB_PASSWORD = os.environ.get('POSTGRES_PASSWORD', 'password')
DB_PORT = os.environ.get('POSTGRES_PORT', '5432')
DB_SSLMODE = os.environ.get('POSTGRES_SSL', 'disable')
DB_PREDICTION = os.environ.get('POSTGRES_DB', 'neondb')
DB_CONTEXT = os.environ.get('POSTGRES_DB_CONTEXT', 'neondb')

def get_prediction_db():
    return psycopg2.connect(host=DB_HOST, database=DB_PREDICTION, user=DB_USER, password=DB_PASSWORD, port=DB_PORT, sslmode=DB_SSLMODE)

def get_context_db():
    return psycopg2.connect(host=DB_HOST, database=DB_CONTEXT, user=DB_USER, password=DB_PASSWORD, port=DB_PORT, sslmode=DB_SSLMODE)

# Load learned weights
weights = {"alpha": 0.35, "beta": 0.35, "gamma": 0.1, "delta": 0.2}
weights_path = 'model/learned_weights.json'
if os.path.exists(weights_path):
    try:
        with open(weights_path, 'r') as f:
            weights = json.load(f)
            logger.info(f"Loaded learned weights: {weights}")
    except Exception as e:
        logger.error(f"Failed to load weights, using defaults: {e}")

CHECK_IN_RADIUS_M = 30

def haversine_m(lat1, lng1, lat2, lng2):
    R = 6_371_000
    phi1, phi2 = math.radians(lat1), math.radians(lat2)
    dphi  = math.radians(lat2 - lat1)
    dlambda = math.radians(lng2 - lng1)
    a = math.sin(dphi / 2) ** 2 + math.cos(phi1) * math.cos(phi2) * math.sin(dlambda / 2) ** 2
    return R * 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))

def snap_to_poi(lat, lng):
    best_id, best_dist = None, float('inf')
    for poi_id, poi in POIS.items():
        d = haversine_m(lat, lng, poi['lat'], poi['lng'])
        if d < best_dist:
            best_dist, best_id = d, poi_id
    if best_dist <= CHECK_IN_RADIUS_M:
        return best_id, best_dist
    return None, best_dist

def get_hour_of_week():
    now = datetime.now(timezone.utc)
    return now.weekday() * 24 + now.hour

class PredictionServiceServicer(prediction_pb2_grpc.PredictionServiceServicer):
    
    def fetch_user_history(self, user_id):
        conn = get_prediction_db()
        cursor = conn.cursor(cursor_factory=RealDictCursor)
        cursor.execute("""
            SELECT poi_id, timestamp, duration_seconds 
            FROM prediction.checkin_sequences 
            WHERE user_id = %s ORDER BY timestamp ASC
        """, (user_id,))
        rows = cursor.fetchall()
        cursor.close()
        conn.close()
        return rows

    def check_user_has_events(self, user_id):
        try:
            conn = get_context_db()
            cursor = conn.cursor()
            cursor.execute("SELECT COUNT(*) FROM context.event_attendance WHERE user_id = %s", (user_id,))
            count = cursor.fetchone()[0]
            cursor.close()
            conn.close()
            return count > 0
        except Exception as e:
            logger.error(f"Error checking user event affinity: {e}")
            return False


    def fetch_active_events(self):
        try:
            conn = get_context_db()
            cursor = conn.cursor(cursor_factory=RealDictCursor)
            # Fetch events active currently
            cursor.execute("""
                SELECT map_id, type FROM context.events 
                WHERE status = 'ONGOING' OR (start_time <= NOW() AND end_time >= NOW())
            """)
            events = cursor.fetchall()
            cursor.close()
            conn.close()
            
            # Map map_id (UUID) back to our short POI IDs
            active_pois = {}
            for e in events:
                map_id = str(e['map_id'])
                # Find matching POI
                for poi_id, poi_data in POIS.items():
                    if poi_data.get('db_uuid') == map_id:
                        active_pois[poi_id] = e['type']
            return active_pois
        except Exception as e:
            logger.error(f"Error fetching events: {e}")
            return {}

    def PredictNextLocation(self, request, context):
        user_id = request.user_id
        target_hw = get_hour_of_week()
        
        # 1. Determine Current POI
        current_poi = None
        if len(request.trajectory) > 0:
            last_pt = request.trajectory[-1]
            current_poi, _ = snap_to_poi(last_pt.latitude, last_pt.longitude)
            
        # 2. Fetch History
        history = self.fetch_user_history(user_id)
        
        # Calculate Base Probabilities
        transitions = {} # from current_poi -> next_poi
        temporal = {}    # poi -> count at target_hw
        preferences = {} # poi -> total count
        
        total_visits = len(history)
        total_temporal = 0
        total_transitions = 0
        
        uuid_to_id = {v.get('db_uuid', k): k for k, v in POIS.items()}

        for i in range(total_visits):
            r = history[i]
            pid = uuid_to_id.get(str(r['poi_id']))
            if pid is None: continue
            
            ts_dt = r['timestamp']
            hw = ts_dt.weekday() * 24 + ts_dt.hour
            
            preferences[pid] = preferences.get(pid, 0) + 1
            if hw == target_hw:
                temporal[pid] = temporal.get(pid, 0) + 1
                total_temporal += 1
                
            if i > 0 and current_poi is not None:
                prev_pid = uuid_to_id.get(str(history[i-1]['poi_id']))
                if prev_pid == current_poi:
                    transitions[pid] = transitions.get(pid, 0) + 1
                    total_transitions += 1
                
        # 3. Fetch Events Context
        active_events = self.fetch_active_events()
        has_event_affinity = self.check_user_has_events(user_id)

        
        # 4. Score all POIs
        scores = {}
        for poi_id in POIS.keys():
            p_trans = (transitions.get(poi_id, 0) / total_transitions) if total_transitions > 0 else 0
            p_temp = (temporal.get(poi_id, 0) / total_temporal) if total_temporal > 0 else 0
            p_pref = (preferences.get(poi_id, 0) / total_visits) if total_visits > 0 else 0
            p_event = 1.0 if (poi_id in active_events and has_event_affinity) else 0.0
            
            # Linear combination
            score = (weights['alpha'] * p_trans) + \
                    (weights['beta'] * p_temp) + \
                    (weights['gamma'] * p_pref) + \
                    (weights['delta'] * p_event)
            
            # If completely 0, give tiny baseline based on global preference or default
            if score == 0:
                score = 0.001
            scores[poi_id] = score
            
        # Normalize
        total_score = sum(scores.values())
        for k in scores:
            scores[k] /= total_score
            
        # Sort and return Top 10
        sorted_pois = sorted(scores.items(), key=lambda x: x[1], reverse=True)[:10]
        
        response = prediction_pb2.PredictNextLocationResponse()
        if len(sorted_pois) > 0:
            top_poi_id, top_score = sorted_pois[0]
            top_poi_data = POIS[top_poi_id]
            response.predicted_poi_id = top_poi_data.get('db_uuid', str(top_poi_id))
            response.predicted_poi_name = top_poi_data['name']
            response.confidence = top_score
            response.intent_type = "GOING_TO_LOCATION"
            response.target_lat = top_poi_data['lat']
            response.target_lng = top_poi_data['lng']
            
            for pid, score in sorted_pois:
                poi = POIS[pid]
                response.candidate_destinations.append(
                    prediction_pb2.PoiPrediction(
                        poi_id=poi.get('db_uuid', str(pid)),
                        poi_name=poi['name'],
                        probability=score,
                        lat=poi['lat'],
                        lng=poi['lng']
                    )
                )
        return response

def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    prediction_pb2_grpc.add_PredictionServiceServicer_to_server(PredictionServiceServicer(), server)
    server.add_insecure_port('[::]:50051')
    server.start()
    logger.info("Prediction Service running with Context-Aware Recommender on port 50051.")
    try:
        while True:
            time.sleep(86400)
    except KeyboardInterrupt:
        server.stop(0)

if __name__ == '__main__':
    serve()
