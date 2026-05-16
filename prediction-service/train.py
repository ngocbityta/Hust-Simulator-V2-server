"""
Training script for STTF-Recommender.
Reference: ijgi-12-00079 (ISPRS IJGI 2023)

Architecture:
  - Spatio-Temporal Embedding: user + location + time (168-dim weekly)
  - Transformer Aggregation: Multi-Head Attention h=8, L=2, GELU, ResNet+LN
  - Output: Attention Matcher + Balance Sampler (1 pos + ne=10 neg samples)
"""

import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import Dataset, DataLoader
import pandas as pd
import numpy as np
import os
import argparse
import math
import random

# ===================== Hyperparameters =====================
NUM_POIS    = 4
NUM_USERS   = 10000
TIME_BINS   = 168       # 7 days x 24 hours
D_MODEL     = 64
NUM_HEADS   = 8         # h = 8  (paper default)
NUM_LAYERS  = 2         # L = 2  (paper default)
DROPOUT     = 0.1
MAX_SEQ_LEN = 50
NE          = 10        # Negative samples per positive (Balance Sampler, paper §4.3)

POIS = {
    0: {"id": "c1_building",  "name": "Tòa C1",                 "lat": 21.006, "lng": 105.843},
    1: {"id": "d3_building",  "name": "Tòa D3",                 "lat": 21.004, "lng": 105.845},
    2: {"id": "library_tqb", "name": "Thư viện Tạ Quang Bửu",  "lat": 21.005, "lng": 105.844},
    3: {"id": "canteen_b",   "name": "Căng tin B",              "lat": 21.003, "lng": 105.842},
}
POI_IDS = list(POIS.keys())   # [0, 1, 2, 3]


# ===================== Dataset =====================
class CheckInDataset(Dataset):
    """
    Reads CSV produced by extract_and_preprocess.py.
    Expected columns: journey_id, step, user_id, poi_id, hour_of_week, target_poi
    Each row is one check-in; rows are grouped by journey_id.
    """
    def __init__(self, csv_file, max_seq_len=MAX_SEQ_LEN):
        df = pd.read_csv(csv_file)
        self.max_seq_len = max_seq_len
        self.num_pois = NUM_POIS

        self.samples = []   # list of (user_id_int, poi_seq, time_seq, target_poi, mask)

        for journey_id, group in df.groupby('journey_id'):
            group = group.sort_values('step')

            # Build input sequence: all check-ins except the last
            seq = list(zip(
                group['poi_id'].tolist(),
                group['hour_of_week'].tolist()
            ))
            target = int(group['target_poi'].iloc[-1])

            if len(seq) < 2:
                continue

            # Use all but last as input, last is target
            input_seq = seq[:-1]

            # Truncate or pad to max_seq_len
            input_seq = input_seq[-max_seq_len:]
            seq_len   = len(input_seq)
            pad_len   = max_seq_len - seq_len

            poi_seq  = [0] * pad_len + [c[0] + 1 for c in input_seq]  # 0=padding
            time_seq = [0] * pad_len + [c[1]     for c in input_seq]
            mask     = [True]  * pad_len + [False] * seq_len           # True=padded

            user_int = self._hash_user(str(group['user_id'].iloc[0]))

            self.samples.append({
                'user_id' : user_int,
                'poi_seq' : poi_seq,
                'time_seq': time_seq,
                'mask'    : mask,
                'target'  : target,
            })

    def _hash_user(self, uid: str) -> int:
        return (hash(uid) % NUM_USERS) + 1   # 1-based; 0 reserved for padding

    def __len__(self):
        return len(self.samples)

    def __getitem__(self, idx):
        s = self.samples[idx]
        return (
            torch.tensor(s['user_id'],  dtype=torch.long),
            torch.tensor(s['poi_seq'],  dtype=torch.long),
            torch.tensor(s['time_seq'], dtype=torch.long),
            torch.tensor(s['mask'],     dtype=torch.bool),
            torch.tensor(s['target'],   dtype=torch.long),
        )


# ===================== STTF-Recommender Model =====================
class STTFRecommender(nn.Module):
    """
    STTF-Recommender as described in ijgi-12-00079, Section 4.
    Three layers:
      1. Spatio-Temporal Embedding:  e_r = e_u + e_p + e_t
      2. Transformer Aggregation:    Multi-Head Self-Attention (h=8, L=2) + PFFN
      3. Output:                     Attention Matcher (returns trajectory repr S)
    """
    def __init__(
        self, num_users=NUM_USERS, num_pois=NUM_POIS,
        d_model=D_MODEL, num_heads=NUM_HEADS, num_layers=NUM_LAYERS,
        time_bins=TIME_BINS, dropout=DROPOUT,
    ):
        super().__init__()
        self.d_model = d_model
        self.num_pois = num_pois

        # Tầng 1 — Spatio-Temporal Embedding
        self.user_embedding     = nn.Embedding(num_users + 1, d_model, padding_idx=0)
        self.location_embedding = nn.Embedding(num_pois  + 1, d_model, padding_idx=0)
        self.time_embedding     = nn.Embedding(time_bins,     d_model)

        # Tầng 2 — Transformer Aggregation (paper eq. 4)
        encoder_layer = nn.TransformerEncoderLayer(
            d_model=d_model, nhead=num_heads,
            dim_feedforward=d_model * 4,
            dropout=dropout, activation='gelu', batch_first=True,
        )
        self.transformer = nn.TransformerEncoder(encoder_layer, num_layers=num_layers)
        self.layer_norm  = nn.LayerNorm(d_model)

    def get_trajectory_repr(self, user_ids, poi_ids, time_ids, mask=None):
        """Returns S(u) = H^L — trajectory summary, shape (batch, d_model)."""
        seq_len = poi_ids.shape[1]
        u_emb   = self.user_embedding(user_ids).unsqueeze(1).expand(-1, seq_len, -1)
        p_emb   = self.location_embedding(poi_ids)
        t_emb   = self.time_embedding(time_ids)
        H = u_emb + p_emb + t_emb                          # e_r = e_u + e_p + e_t
        H = self.transformer(H, src_key_padding_mask=mask)
        S = self.layer_norm(H[:, -1, :])                   # (batch, d)
        return S

    def attention_match(self, S, candidate_ids):
        """
        Attention Matcher (paper §4.3, eq. 5-6):
          A(u) = Matching(E(p), S(u)) = Sum(softmax(E(p) * S(u)^T / sqrt(d)))
        Args:
            S             : (batch, d) trajectory repr
            candidate_ids : (batch, num_candidates) POI IDs (1-indexed)
        Returns:
            scores: (batch, num_candidates) — unnormalised logits
        """
        # E(p): (batch, num_cand, d)
        E = self.location_embedding(candidate_ids)
        # Scaled dot-product: (batch, 1, d) x (batch, d, num_cand) → (batch, num_cand)
        scores = torch.bmm(S.unsqueeze(1), E.transpose(1, 2)).squeeze(1)
        scores = scores / math.sqrt(self.d_model)
        return scores


# ===================== Balanced Sampler Loss =====================
def balanced_cross_entropy_loss(model, S, target_poi_ids, ne=NE):
    """
    Balance Sampler (paper §4.3, eq. 7):
    For each sample, use 1 positive + ne random negative locations.
    Loss = -log σ(a_pos) - Σ log(1 - σ(a_neg))
    """
    batch_size = S.shape[0]
    total_loss = torch.tensor(0.0, requires_grad=True)

    all_poi_set = set(range(1, NUM_POIS + 1))   # 1-indexed (0=padding)

    for i in range(batch_size):
        pos_id = target_poi_ids[i].item() + 1   # convert to 1-indexed embedding

        # Sample ne negatives (excluding positive)
        neg_pool = list(all_poi_set - {pos_id})
        neg_ids  = random.sample(neg_pool, min(ne, len(neg_pool)))

        # Build candidate tensor: [pos, neg1, neg2, ..., neg_ne]
        cand_ids = torch.tensor([pos_id] + neg_ids, dtype=torch.long)
        cand_ids = cand_ids.unsqueeze(0)        # (1, ne+1)

        # Attention scores for candidates
        scores = model.attention_match(S[i:i+1], cand_ids)  # (1, ne+1)
        probs  = torch.sigmoid(scores[0])                    # (ne+1,)

        # Loss: pos at index 0
        pos_loss = -torch.log(probs[0] + 1e-8)
        neg_loss = -torch.sum(torch.log(1.0 - probs[1:] + 1e-8))
        total_loss = total_loss + pos_loss + neg_loss

    return total_loss / batch_size


# ===================== Training Loop =====================
def main():
    parser = argparse.ArgumentParser(description='Train STTF-Recommender')
    parser.add_argument('--data',   type=str, default='data/checkin_sequences.csv')
    parser.add_argument('--epochs', type=int, default=30)
    parser.add_argument('--lr',     type=float, default=1e-3)
    parser.add_argument('--batch',  type=int, default=32)
    parser.add_argument('--ne',     type=int, default=NE,
                        help='Negative samples per positive (Balance Sampler)')
    args = parser.parse_args()

    print(f"Loading check-in data from {args.data} ...")
    dataset    = CheckInDataset(args.data)
    dataloader = DataLoader(dataset, batch_size=args.batch, shuffle=True)
    print(f"  → {len(dataset)} training samples")

    model     = STTFRecommender()
    optimizer = optim.Adam(model.parameters(), lr=args.lr)
    scheduler = optim.lr_scheduler.StepLR(optimizer, step_size=10, gamma=0.5)

    print(f"\nStarting STTF-Recommender training "
          f"(epochs={args.epochs}, ne={args.ne}, lr={args.lr}) ...")

    for epoch in range(args.epochs):
        model.train()
        epoch_loss = 0.0
        for user_ids, poi_seqs, time_seqs, masks, targets in dataloader:
            optimizer.zero_grad()

            # Trajectory representation S(u)
            S = model.get_trajectory_repr(user_ids, poi_seqs, time_seqs, mask=masks)

            # Balanced cross-entropy loss
            loss = balanced_cross_entropy_loss(model, S, targets, ne=args.ne)
            loss.backward()
            optimizer.step()
            epoch_loss += loss.item()

        scheduler.step()
        avg_loss = epoch_loss / max(len(dataloader), 1)
        print(f"  Epoch [{epoch+1:2d}/{args.epochs}]  Loss: {avg_loss:.4f}  "
              f"LR: {scheduler.get_last_lr()[0]:.5f}")

    # Save model
    os.makedirs('model', exist_ok=True)
    torch.save(model.state_dict(), 'model/sttf_model.pth')
    print("\nModel saved → model/sttf_model.pth")

    # Create flag so hot-reload in main.py is triggered
    with open('model_updated.flag', 'w') as f:
        f.write(str(torch.tensor(1).item()))
    print("Hot-reload flag created → model_updated.flag")


if __name__ == '__main__':
    main()
