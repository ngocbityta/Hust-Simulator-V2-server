import grpc
from concurrent import futures
import time
import logging
import random
import os
import math

os.environ["OMP_NUM_THREADS"] = "1"
os.environ["MKL_NUM_THREADS"] = "1"
os.environ["OPENBLAS_NUM_THREADS"] = "1"
os.environ["VECLIB_MAXIMUM_THREADS"] = "1"
os.environ["NUMEXPR_NUM_THREADS"] = "1"

import torch
torch.set_num_threads(1)
torch.set_num_interop_threads(1)

import numpy as np
import torch.nn as nn
from datetime import datetime, timezone

# Import generated classes
import prediction_pb2
import prediction_pb2_grpc
from poi_config import POIS, NUM_POIS, POI_IDS_SORTED

# ===================== Model Hyperparameters =====================
# Matching paper: ijgi-12-00079 (STTF-Recommender)
NUM_USERS = 100000     # Max unique users for embedding lookup
TIME_BINS = 168       # 7 days x 24 hours — captures weekly periodicity
D_MODEL = 64          # Embedding dimension d
NUM_HEADS = 8         # Multi-Head Attention heads h = 8 (paper default)
NUM_LAYERS = 2        # Stacked Transformer layers L = 2 (paper default)
DROPOUT = 0.1
MAX_SEQ_LEN = 50      # Max check-in sequence length n

# GPS → check-in conversion
CHECK_IN_RADIUS_M = 30   # meters — snap GPS point to POI if within this radius

# ===================== Utility Functions =====================

def haversine_m(lat1, lng1, lat2, lng2):
    """Return distance in meters between two GPS coordinates."""
    R = 6_371_000  # Earth radius in meters
    phi1, phi2 = math.radians(lat1), math.radians(lat2)
    dphi  = math.radians(lat2 - lat1)
    dlambda = math.radians(lng2 - lng1)
    a = math.sin(dphi / 2) ** 2 + math.cos(phi1) * math.cos(phi2) * math.sin(dlambda / 2) ** 2
    return R * 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a))

def snap_to_poi(lat, lng):
    """
    Find nearest POI within CHECK_IN_RADIUS_M.
    Returns (poi_id, distance_m) or (None, dist) if outside radius.
    """
    best_id, best_dist = None, float('inf')
    for poi_id, poi in POIS.items():
        d = haversine_m(lat, lng, poi['lat'], poi['lng'])
        if d < best_dist:
            best_dist, best_id = d, poi_id
    if best_dist <= CHECK_IN_RADIUS_M:
        return best_id, best_dist
    return None, best_dist

def get_hour_of_week():
    """Return integer [0, 167] = (day_of_week * 24 + hour_of_day)."""
    now = datetime.now(timezone.utc)
    return now.weekday() * 24 + now.hour   # weekday(): 0=Mon … 6=Sun

import hashlib

def hash_user_id(user_id_str: str) -> int:
    """Map string user_id to deterministic integer in [1, NUM_USERS]."""
    # Use MD5 to get a deterministic integer hash across different Python runs
    hash_int = int(hashlib.md5(str(user_id_str).encode('utf-8')).hexdigest(), 16)
    return (hash_int % NUM_USERS) + 1   # 0 is reserved for padding

def gps_trajectory_to_checkins(trajectory_points, fallback_hour_of_week: int):
    """
    Convert raw GPS trajectory to check-in sequence.
    Applies POI-snapping and deduplication (consecutive same POI removed).
    Returns list of (poi_id, hour_of_week) tuples.
    """
    checkins = []
    last_poi = None
    for pt in trajectory_points:
        poi_id, _ = snap_to_poi(pt.latitude, pt.longitude)
        if poi_id is not None and poi_id != last_poi:
            if hasattr(pt, 'timestamp') and pt.timestamp > 0:
                ts_dt = datetime.fromtimestamp(pt.timestamp / 1000.0, tz=timezone.utc)
                hw = ts_dt.weekday() * 24 + ts_dt.hour
            else:
                hw = fallback_hour_of_week
            checkins.append((poi_id, hw))
            last_poi = poi_id
    return checkins

class STTFRecommender(nn.Module):
    """
    STTF-Recommender: Spatio-Temporal Transformer Fusion Recommender.
    Reference: ijgi-12-00079 (ISPRS IJGI 2023, Section 4).
    """
    def __init__(
        self,
        num_users=NUM_USERS,
        num_pois=NUM_POIS,
        d_model=D_MODEL,
        num_heads=NUM_HEADS,
        num_layers=NUM_LAYERS,
        time_bins=TIME_BINS,
        dropout=DROPOUT,
        max_seq_len=MAX_SEQ_LEN,
    ):
        super().__init__()
        self.d_model = d_model
        self.num_pois = num_pois

        # --- Tầng 1: Spatio-Temporal Embedding Layer ---
        self.user_embedding     = nn.Embedding(num_users + 1, d_model, padding_idx=0)
        self.location_embedding = nn.Embedding(num_pois  + 1, d_model, padding_idx=0)
        self.time_embedding     = nn.Embedding(time_bins,     d_model)

        # Positional encoding for sequence order (paper §4.1, extended)
        self.position_embedding = nn.Embedding(max_seq_len, d_model)

        # --- Tầng 2: Transformer Aggregation Layer ---
        encoder_layer = nn.TransformerEncoderLayer(
            d_model=d_model,
            nhead=num_heads,
            dim_feedforward=d_model * 4,  # Position-wise FFN hidden dim
            dropout=dropout,
            activation='gelu',            # GELU activation (paper eq. 3)
            batch_first=True,
        )
        self.transformer = nn.TransformerEncoder(encoder_layer, num_layers=num_layers)
        self.layer_norm  = nn.LayerNorm(d_model)

    def forward(self, user_ids, poi_ids, time_ids, src_key_padding_mask=None):
        """
        Args:
            user_ids  : (batch,)            — integer user IDs
            poi_ids   : (batch, seq_len)    — integer POI IDs; 0 = padding
            time_ids  : (batch, seq_len)    — hour-of-week [0..167]
            src_key_padding_mask: (batch, seq_len) — True for padded positions
        Returns:
            S: (batch, d_model) — trajectory summary representation S(u) = H^L
        """
        batch_size = poi_ids.shape[0]
        seq_len = poi_ids.shape[1]

        # e_u broadcast across sequence
        u_emb = self.user_embedding(user_ids).unsqueeze(1).expand(-1, seq_len, -1)
        p_emb = self.location_embedding(poi_ids)   # (batch, seq_len, d)
        t_emb = self.time_embedding(time_ids)       # (batch, seq_len, d)

        # Positional encoding for sequence order
        positions = torch.arange(seq_len, device=poi_ids.device).unsqueeze(0).expand(batch_size, -1)
        pos_emb = self.position_embedding(positions)  # (batch, seq_len, d)

        # Combined check-in representation: e_r = e_u + e_p + e_t + e_pos  (paper §4.1, extended)
        H = u_emb + p_emb + t_emb + pos_emb        # (batch, seq_len, d)

        # Transformer aggregation (paper §4.2)
        H = self.transformer(H, src_key_padding_mask=src_key_padding_mask)

        # Trajectory summary from last position
        S = self.layer_norm(H[:, -1, :])           # (batch, d)
        return S

    def predict_proba(self, user_ids, poi_ids, time_ids, src_key_padding_mask=None):
        """
        Attention Matcher (paper §4.3, eq. 5-6):
            A(u) = Matching(E(p), S(u))
            Matching(Q, K) = Sum(softmax(Q K^T / sqrt(d)))
        Returns probability distribution over all POI candidates.
        """
        S = self.forward(user_ids, poi_ids, time_ids, src_key_padding_mask)

        # Candidate location embeddings E(p): shape (num_pois, d)
        cand_ids = torch.tensor(POI_IDS_SORTED, dtype=torch.long)
        # Shift by 1 because embedding index 0 is padding
        E_cand = self.location_embedding(cand_ids + 1)   # (P, d)

        # Scaled dot-product matching: (batch, d) x (d, P) → (batch, P)
        scores = torch.matmul(S, E_cand.T) / math.sqrt(self.d_model)
        TEMPERATURE = 0.5  # Sharper distribution — lower = more decisive
        probs  = torch.softmax(scores / TEMPERATURE, dim=-1)
        return probs


# ===================== Model Loading =====================
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger('PredictionService')

model = None
last_model_load_time = 0


def load_model_if_needed():
    global model, last_model_load_time

    flag_file  = 'model_updated.flag'
    model_file = 'model/sttf_model.pth'

    needs_load = (model is None)
    if not needs_load and os.path.exists(flag_file):
        if os.path.getmtime(flag_file) > last_model_load_time:
            logger.info("Detected new model version. Hot-reloading...")
            needs_load = True

    if needs_load and os.path.exists(model_file):
        try:
            # Dynamically reload POI configuration because pois.json might have changed
            import importlib
            import poi_config
            importlib.reload(poi_config)
            
            # Rebind global variables that depend on poi_config
            global POIS, NUM_POIS, POI_IDS_SORTED
            POIS = poi_config.POIS
            NUM_POIS = poi_config.NUM_POIS
            POI_IDS_SORTED = poi_config.POI_IDS_SORTED

            new_model = STTFRecommender(num_pois=NUM_POIS)
            new_model.load_state_dict(
                torch.load(model_file, map_location='cpu', weights_only=True)
            )
            new_model.eval()
            model = new_model
            last_model_load_time = time.time()
            logger.info(f"Loaded STTF-Recommender model successfully (NUM_POIS={NUM_POIS}).")
        except Exception as e:
            logger.error(f"Failed to load model: {e}. Keeping previous model.")


# ---- POI Time-Aware Weights ----

def get_time_weight(poi_category: str, hour: float) -> float:
    """Return multiplier [0.05, 2.0] for a POI category at given hour of day (local time)."""
    weights = {
        'CLASSROOM': {
            # Active during class hours, closed at night
            (7, 11.5): 1.0, (13, 17): 1.0, (17, 21): 0.4,
            'default': 0.05
        },
        'CANTEEN': {
            # Peak at lunch and dinner
            (6, 7.5): 0.5, (11, 13): 2.0, (17, 19): 1.5,
            'default': 0.15
        },
        'LIBRARY': {
            # Open 7-22, peak evening study
            (7, 17): 0.8, (17, 22): 1.5,
            'default': 0.05
        },
        'DORM': {
            # Active at night, early morning
            (0, 7): 1.5, (21, 24): 1.5, (7, 17): 0.2,
            'default': 0.3
        },
        'SPORTS': {
            # Afternoon/evening
            (16, 21): 1.2,
            'default': 0.1
        },
        'LAB': {
            # Extended hours
            (8, 22): 0.8,
            'default': 0.05
        },
    }
    cat_weights = weights.get(poi_category, weights['CLASSROOM'])
    for (start, end), mult in cat_weights.items():
        if isinstance(start, (int, float)) and start <= hour < end:
            return mult
    return cat_weights.get('default', 0.1)


# ===================== gRPC Service =====================
class PredictionServiceServicer(prediction_pb2_grpc.PredictionServiceServicer):

    def PredictNextLocation(self, request, context):
        logger.info(f"Prediction request — user: {request.user_id}, "
                    f"trajectory points: {len(request.trajectory)}")

        load_model_if_needed()

        # --- Convert GPS trajectory to check-in sequence ---
        if hasattr(request, 'target_timestamp_ms') and request.target_timestamp_ms > 0:
            target_dt = datetime.fromtimestamp(request.target_timestamp_ms / 1000.0, tz=timezone.utc)
            hour_of_week = target_dt.weekday() * 24 + target_dt.hour
        else:
            hour_of_week = get_hour_of_week()
        checkins = gps_trajectory_to_checkins(request.trajectory, hour_of_week)
        logger.info(f"Check-in sequence length after GPS→POI snapping: {len(checkins)}, hour_of_week: {hour_of_week}")

        # --- Fallback: Heuristic Predictor ---
        # Used when: model not ready OR check-in sequence too short
        if model is None or len(checkins) < 2:
            confidence = 0.4
            # Simple heuristic: pick POI closest to last GPS point
            if len(request.trajectory) > 0:
                last_pt = request.trajectory[-1]
                best_id, _ = snap_to_poi(last_pt.latitude, last_pt.longitude)
                predicted_class = best_id if best_id is not None else random.choice(POI_IDS_SORTED)
            else:
                predicted_class = random.choice(POI_IDS_SORTED)
            logger.info(f"Heuristic fallback → POI class {predicted_class}")
        else:
            # --- AI Predictor: STTF-Recommender Inference ---
            try:
                user_int = hash_user_id(request.user_id)

                # Build input tensors
                seq = checkins[-MAX_SEQ_LEN:]          # truncate to max length
                seq_len = len(seq)

                # Pad to MAX_SEQ_LEN (pad at left, mask padded positions)
                pad_len = MAX_SEQ_LEN - seq_len
                poi_seq  = [0] * pad_len + [c[0] + 1 for c in seq]  # +1: 0=pad
                time_seq = [0] * pad_len + [c[1]     for c in seq]
                mask     = [True]  * pad_len + [False] * seq_len     # True=ignore

                user_tensor = torch.tensor([[user_int]], dtype=torch.long)
                poi_tensor  = torch.tensor([poi_seq],   dtype=torch.long)
                time_tensor = torch.tensor([time_seq],  dtype=torch.long)
                mask_tensor = torch.tensor([mask],      dtype=torch.bool)

                with torch.no_grad():
                    probs = model.predict_proba(
                        user_tensor.squeeze(0),  # (1,)
                        poi_tensor,              # (1, seq_len)
                        time_tensor,             # (1, seq_len)
                        src_key_padding_mask=mask_tensor,
                    )  # (1, num_pois)
                    probs_np = probs[0].numpy()
                    
                    # Apply time-aware POI scoring post-processing (using Vietnam local hour)
                    local_hour = (hour_of_week % 24 + 7) % 24
                    for idx in range(len(probs_np)):
                        poi_id = POI_IDS_SORTED[idx]
                        poi = POIS[poi_id]
                        category = str(poi.get('category') or 'CLASSROOM').upper()
                        time_weight = get_time_weight(category, local_hour)
                        probs_np[idx] *= time_weight

                    # Re-normalize
                    total = probs_np.sum()
                    if total > 0:
                        probs_np /= total
                    
                    top_k = 10
                    top_indices = np.argsort(probs_np)[-top_k:][::-1]
                    
                    predicted_idx = int(top_indices[0])
                    predicted_class = POI_IDS_SORTED[predicted_idx]
                    confidence = float(probs_np[predicted_idx])
                    
                    candidate_destinations = []
                    for idx in top_indices:
                        prob = float(probs_np[idx])
                        if prob > 0.005:  # lower threshold slightly for softmax candidates
                            c_poi_id = POI_IDS_SORTED[idx]
                            c_poi = POIS[c_poi_id]
                            candidate_destinations.append(
                                prediction_pb2.PoiPrediction(
                                    poi_id=c_poi.get("db_uuid", c_poi.get("id")),
                                    poi_name=c_poi["name"],
                                    probability=prob,
                                    lat=c_poi["lat"],
                                    lng=c_poi["lng"]
                                )
                            )

                logger.info(
                    f"STTF inference → POI {predicted_class} "
                    f"({POIS[predicted_class]['name']}) "
                    f"confidence={confidence:.3f} (Total candidates: {len(candidate_destinations)})"
                )
            except Exception as e:
                logger.error(f"Inference error: {e}. Using heuristic fallback.")
                predicted_class = random.choice(POI_IDS_SORTED)
                confidence = 0.4
                candidate_destinations = []

        # --- Build response ---
        poi = POIS[predicted_class]
        
        if 'candidate_destinations' not in locals() or not candidate_destinations:
            candidate_destinations = [
                prediction_pb2.PoiPrediction(
                    poi_id=poi.get("db_uuid", poi.get("id")),
                    poi_name=poi["name"],
                    probability=confidence,
                    lat=poi["lat"],
                    lng=poi["lng"]
                )
            ]

        response = prediction_pb2.PredictNextLocationResponse(
            predicted_poi_id   = poi.get("db_uuid", poi.get("id")),
            predicted_poi_name = poi["name"],
            confidence         = confidence,
            intent_type        = "GOING_TO_LOCATION",
            target_lat         = poi["lat"],
            target_lng         = poi["lng"],
            candidate_destinations = candidate_destinations
        )
        logger.info(
            f"Response → {poi['name']} (conf={confidence:.3f})"
        )
        return response


def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    prediction_pb2_grpc.add_PredictionServiceServicer_to_server(
        PredictionServiceServicer(), server
    )
    server.add_insecure_port('[::]:50055')
    server.start()
    logger.info("STTF-Recommender Prediction Service started on port 50055")
    try:
        server.wait_for_termination()
    except KeyboardInterrupt:
        server.stop(0)


if __name__ == '__main__':
    serve()
