import grpc
from concurrent import futures
import time
import logging
import random
import os
import math
import torch
import numpy as np
import torch.nn as nn
import json
from datetime import datetime, timezone

# Import generated classes
import prediction_pb2
import prediction_pb2_grpc

# ===================== Model Hyperparameters =====================
# Matching paper: ijgi-12-00079 (STTF-Recommender)
NUM_POIS = 4          # Number of known POIs (= number of location classes P)
NUM_USERS = 10000     # Max unique users for embedding lookup
TIME_BINS = 168       # 7 days x 24 hours — captures weekly periodicity
D_MODEL = 64          # Embedding dimension d
NUM_HEADS = 8         # Multi-Head Attention heads h = 8 (paper default)
NUM_LAYERS = 2        # Stacked Transformer layers L = 2 (paper default)
DROPOUT = 0.1
MAX_SEQ_LEN = 50      # Max check-in sequence length n

# GPS → check-in conversion
CHECK_IN_RADIUS_M = 80   # meters — snap GPS point to POI if within this radius

# Load Known POIs from config
POIS = {}
try:
    with open('pois.json', 'r', encoding='utf-8') as f:
        data = json.load(f)
        POIS = {int(k): v for k, v in data.items()}
except Exception as e:
    POIS = {
        0: {"id": "c1_building",   "name": "Tòa C1",                  "lat": 21.006, "lng": 105.843},
        1: {"id": "d3_building",   "name": "Tòa D3",                  "lat": 21.004, "lng": 105.845},
        2: {"id": "library_tqb",  "name": "Thư viện Tạ Quang Bửu",   "lat": 21.005, "lng": 105.844},
        3: {"id": "canteen_b",    "name": "Căng tin B",               "lat": 21.003, "lng": 105.842},
    }

POI_IDS_SORTED = sorted(POIS.keys())   # [0, 1, 2, 3]

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

def hash_user_id(user_id_str: str) -> int:
    """Map string user_id to integer in [1, NUM_USERS]."""
    return (hash(user_id_str) % NUM_USERS) + 1   # 0 is reserved for padding

def gps_trajectory_to_checkins(trajectory_points, current_hour_of_week: int):
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
            checkins.append((poi_id, current_hour_of_week))
            last_poi = poi_id
    return checkins

# ===================== STTF-Recommender Model =====================
# Paper: ijgi-12-00079, Section 4
# Three layers:
#   1. Spatio-Temporal Embedding Layer  — user + location + time embeddings
#   2. Transformer Aggregation Layer    — Multi-Head Self-Attention (h=8, L=2)
#   3. Output Layer                     — Attention Matcher over candidate POIs

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
    ):
        super().__init__()
        self.d_model = d_model
        self.num_pois = num_pois

        # --- Tầng 1: Spatio-Temporal Embedding Layer ---
        # e_u: user embedding  (U → R^d)
        # e_p: location embedding (P → R^d), padding_idx=0
        # e_t: time embedding  (168 → R^d), captures weekly periodicity
        self.user_embedding     = nn.Embedding(num_users + 1, d_model, padding_idx=0)
        self.location_embedding = nn.Embedding(num_pois  + 1, d_model, padding_idx=0)
        self.time_embedding     = nn.Embedding(time_bins,     d_model)

        # --- Tầng 2: Transformer Aggregation Layer ---
        # Multi-Head Self-Attention + Position-wise FFN (GELU)
        # With residual connections and Layer Normalization (paper eq. 4)
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
        seq_len = poi_ids.shape[1]

        # e_u broadcast across sequence
        u_emb = self.user_embedding(user_ids).unsqueeze(1).expand(-1, seq_len, -1)
        p_emb = self.location_embedding(poi_ids)   # (batch, seq_len, d)
        t_emb = self.time_embedding(time_ids)       # (batch, seq_len, d)

        # Combined check-in representation: e_r = e_u + e_p + e_t  (paper §4.1)
        H = u_emb + p_emb + t_emb                  # (batch, seq_len, d)

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
        probs  = torch.softmax(scores, dim=-1)            # (batch, P)
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
            new_model = STTFRecommender()
            new_model.load_state_dict(
                torch.load(model_file, weights_only=True)
            )
            new_model.eval()
            model = new_model
            last_model_load_time = time.time()
            logger.info("Loaded STTF-Recommender model successfully.")
        except Exception as e:
            logger.error(f"Failed to load model: {e}. Keeping previous model.")


# ===================== gRPC Service =====================
class PredictionServiceServicer(prediction_pb2_grpc.PredictionServiceServicer):

    def PredictNextLocation(self, request, context):
        logger.info(f"Prediction request — user: {request.user_id}, "
                    f"trajectory points: {len(request.trajectory)}")

        load_model_if_needed()

        # --- Convert GPS trajectory to check-in sequence ---
        hour_of_week = get_hour_of_week()
        checkins = gps_trajectory_to_checkins(request.trajectory, hour_of_week)
        logger.info(f"Check-in sequence length after GPS→POI snapping: {len(checkins)}")

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
                    predicted_class = int(np.argmax(probs_np))
                    confidence = float(probs_np[predicted_class])

                logger.info(
                    f"STTF inference → POI {predicted_class} "
                    f"({POIS[predicted_class]['name']}) "
                    f"confidence={confidence:.3f}"
                )
            except Exception as e:
                logger.error(f"Inference error: {e}. Using heuristic fallback.")
                predicted_class = random.choice(POI_IDS_SORTED)
                confidence = 0.4

        # --- Build response ---
        poi = POIS[predicted_class]
        response = prediction_pb2.PredictNextLocationResponse(
            predicted_poi_id   = poi["id"],
            predicted_poi_name = poi["name"],
            confidence         = confidence,
            intent_type        = "GOING_TO_LOCATION",
            target_lat         = poi["lat"],
            target_lng         = poi["lng"],
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
