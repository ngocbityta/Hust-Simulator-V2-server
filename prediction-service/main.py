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

def get_prediction_db():
    return psycopg2.connect(host=DB_HOST, database=DB_PREDICTION, user=DB_USER, password=DB_PASSWORD, port=DB_PORT, sslmode=DB_SSLMODE)

# Load learned weights
weights = {"alpha": 0.4, "beta": 0.4, "gamma": 0.2}
weights_path = 'model/learned_weights.json'
if os.path.exists(weights_path):
    try:
        with open(weights_path, 'r') as f:
            weights = json.load(f)
            logger.info(f"Loaded learned weights: {weights}")
    except Exception as e:
        logger.error(f"Failed to load weights, using defaults: {e}")

def get_hour_of_week(timestamp_ms=None):
    """Return hour_of_week [0..167] for the given timestamp, or current time if None."""
    if timestamp_ms and timestamp_ms > 0:
        dt = datetime.fromtimestamp(timestamp_ms / 1000, tz=timezone.utc)
    else:
        dt = datetime.now(timezone.utc)
    return dt.weekday() * 24 + dt.hour

def temporal_distance(hw1, hw2):
    """Circular distance between two hour_of_week values [0..167], accounting for weekly wrap-around."""
    diff = abs(hw1 - hw2)
    return min(diff, 168 - diff)

def temporal_weight(hw1, hw2, bandwidth=2):
    """Gaussian kernel weight for temporal similarity. bandwidth in hours."""
    dist = temporal_distance(hw1, hw2)
    return math.exp(-0.5 * (dist / bandwidth) ** 2)

class PredictionServiceServicer(prediction_pb2_grpc.PredictionServiceServicer):
    
    def fetch_user_history(self, user_id):
        import uuid
        try:
            uuid_obj = uuid.UUID(user_id, version=4)
        except ValueError:
            return []
            
        conn = get_prediction_db()
        cursor = conn.cursor(cursor_factory=RealDictCursor)
        cursor.execute("""
            SELECT poi_id, timestamp, duration_seconds 
            FROM prediction.checkin_sequences 
            WHERE user_id = %s ORDER BY timestamp ASC
        """, (str(uuid_obj),))
        rows = cursor.fetchall()
        cursor.close()
        conn.close()
        return rows

    def PredictNextLocation(self, request, context):
        user_id = request.user_id
        
        # Safe check using getattr to avoid AttributeError if proto is missing it
        ts_val = getattr(request, 'target_timestamp_ms', 0)
        target_ts_ms = ts_val if ts_val and ts_val > 0 else None
        target_hw = get_hour_of_week(target_ts_ms)
        
        # 1. Fetch History
        history = self.fetch_user_history(user_id)
        
        # If no history, return empty predictions
        if not history:
            return prediction_pb2.PredictNextLocationResponse(
                predicted_poi_id="",
                predicted_poi_name="Unknown",
                confidence=0.0,
                intent_type="UNKNOWN",
                target_lat=0.0,
                target_lng=0.0,
                candidate_destinations=[]
            )
            
        uuid_to_id = {v.get('db_uuid', k): k for k, v in POIS.items()}
        
        # Determine Current POI from history
        current_uuid_str = str(history[-1]['poi_id'])
        current_poi = uuid_to_id.get(current_uuid_str)
        
        # Calculate Base Probabilities
        transitions = {} # from current_poi -> next_poi
        temporal = {}    # poi -> count at target_hw
        preferences = {} # poi -> total count
        
        total_visits = len(history)
        total_temporal = 0
        total_transitions = 0
        
        for i in range(total_visits):
            r = history[i]
            pid = uuid_to_id.get(str(r['poi_id']))
            if pid is None: continue
            
            ts_dt = r['timestamp']
            hw = ts_dt.weekday() * 24 + ts_dt.hour
            
            preferences[pid] = preferences.get(pid, 0) + 1
            
            # Use gaussian kernel for temporal similarity (±2hr window)
            tw = temporal_weight(hw, target_hw, bandwidth=2)
            if tw > 0.01:  # Only count if within meaningful range
                temporal[pid] = temporal.get(pid, 0) + tw
                total_temporal += tw
                
            if i > 0 and current_poi is not None:
                prev_pid = uuid_to_id.get(str(history[i-1]['poi_id']))
                if prev_pid == current_poi:
                    transitions[pid] = transitions.get(pid, 0) + 1
                    total_transitions += 1
                
        # 3. Score all POIs
        scores = {}
        for poi_id in POIS.keys():
            p_trans = (transitions.get(poi_id, 0) / total_transitions) if total_transitions > 0 else 0
            p_temp = (temporal.get(poi_id, 0) / total_temporal) if total_temporal > 0 else 0
            p_pref = (preferences.get(poi_id, 0) / total_visits) if total_visits > 0 else 0
            
            # Linear combination
            score = (weights['alpha'] * p_trans) + \
                    (weights['beta'] * p_temp) + \
                    (weights['gamma'] * p_pref)
            
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
    server.add_insecure_port('0.0.0.0:50051')
    server.start()
    logger.info("Prediction Service running with Context-Aware Recommender on port 50051.")
    try:
        while True:
            time.sleep(86400)
    except KeyboardInterrupt:
        server.stop(0)

if __name__ == '__main__':
    serve()
