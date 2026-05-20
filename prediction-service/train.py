"""
Training script for STTF-Recommender.
Reference: ijgi-12-00079 (ISPRS IJGI 2023)

Architecture:
  - Spatio-Temporal Embedding: user + location + time (168-dim weekly)
  - Transformer Aggregation: Multi-Head Attention h=8, L=2, GELU, ResNet+LN
  - Output: Attention Matcher + Balance Sampler (1 pos + ne=10 neg samples)
"""

import torch
torch.set_num_threads(1)
torch.set_num_interop_threads(1)
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import Dataset, DataLoader, random_split
import pandas as pd
import numpy as np
import os
import argparse
import math
import random

from poi_config import POIS, NUM_POIS, POI_IDS_SORTED

# ===================== Hyperparameters =====================
NUM_USERS   = 10000
TIME_BINS   = 168       # 7 days x 24 hours
D_MODEL     = 64
NUM_HEADS   = 8         # h = 8  (paper default)
NUM_LAYERS  = 2         # L = 2  (paper default)
DROPOUT     = 0.1
MAX_SEQ_LEN = 50
NE          = 10        # Negative samples per positive (Balance Sampler, paper §4.3)


# ===================== Dataset =====================
class CheckInDataset(Dataset):
    """
    Reads CSV produced by extract_and_preprocess.py.
    Expected columns: journey_id, step, user_id, poi_id, hour_of_week, target_poi, journey_time
    Each row is one check-in; rows are grouped by journey_id.
    """
    def __init__(self, csv_file, allowed_journeys=None, max_seq_len=MAX_SEQ_LEN):
        df = pd.read_csv(csv_file)
        if allowed_journeys is not None:
            df = df[df['journey_id'].isin(allowed_journeys)]
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
        import hashlib
        hash_int = int(hashlib.md5(str(uid).encode('utf-8')).hexdigest(), 16)
        return (hash_int % NUM_USERS) + 1   # 1-based; 0 reserved for padding

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
        time_bins=TIME_BINS, dropout=DROPOUT, max_seq_len=MAX_SEQ_LEN,
    ):
        super().__init__()
        self.d_model = d_model
        self.num_pois = num_pois

        # Tầng 1 — Spatio-Temporal Embedding
        self.user_embedding     = nn.Embedding(num_users + 1, d_model, padding_idx=0)
        self.location_embedding = nn.Embedding(num_pois  + 1, d_model, padding_idx=0)
        self.time_embedding     = nn.Embedding(time_bins,     d_model)
        self.position_embedding = nn.Embedding(max_seq_len, d_model)

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
        batch_size = poi_ids.shape[0]
        seq_len = poi_ids.shape[1]
        u_emb   = self.user_embedding(user_ids).unsqueeze(1).expand(-1, seq_len, -1)
        p_emb   = self.location_embedding(poi_ids)
        t_emb   = self.time_embedding(time_ids)

        # Positional encoding for sequence order
        positions = torch.arange(seq_len, device=poi_ids.device).unsqueeze(0).expand(batch_size, -1)
        pos_emb = self.position_embedding(positions)  # (batch, seq_len, d)

        H = u_emb + p_emb + t_emb + pos_emb            # e_r = e_u + e_p + e_t + e_pos
        H = self.transformer(H, src_key_padding_mask=mask)
        S = self.layer_norm(H[:, -1, :])                # (batch, d)
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


# ===================== Evaluation Metrics =====================
# Paper §5: Acc@K, MRR (Mean Reciprocal Rank)

def evaluate(model, dataloader):
    """
    Evaluate model on a dataset.
    Returns dict with Acc@1, Acc@3, MRR metrics.
    """
    model.eval()
    total = 0
    correct_at_1 = 0
    correct_at_3 = 0
    reciprocal_ranks = []

    # Build candidate tensor (all POIs, 1-indexed for embedding)
    all_cand = torch.tensor(
        [poi_id + 1 for poi_id in POI_IDS_SORTED], dtype=torch.long
    )  # (num_pois,)

    with torch.no_grad():
        for user_ids, poi_seqs, time_seqs, masks, targets in dataloader:
            S = model.get_trajectory_repr(user_ids, poi_seqs, time_seqs, mask=masks)

            # Expand candidates for batch: (batch, num_pois)
            batch_cand = all_cand.unsqueeze(0).expand(S.shape[0], -1)
            scores = model.attention_match(S, batch_cand)  # (batch, num_pois)

            # Rank predictions (descending score)
            _, ranked_indices = scores.sort(dim=-1, descending=True)

            for i in range(targets.shape[0]):
                target = targets[i].item()
                total += 1

                # ranked_indices[i] maps to indices into POI_IDS_SORTED
                pred_ranking = [POI_IDS_SORTED[idx] for idx in ranked_indices[i].tolist()]

                if target in pred_ranking:
                    rank = pred_ranking.index(target) + 1
                else:
                    rank = len(pred_ranking) + 1

                if rank == 1:
                    correct_at_1 += 1
                if rank <= 3:
                    correct_at_3 += 1
                reciprocal_ranks.append(1.0 / rank)

    n = max(total, 1)
    return {
        'Acc@1': correct_at_1 / n,
        'Acc@3': correct_at_3 / n,
        'MRR':   sum(reciprocal_ranks) / n,
        'total': total,
    }


# ===================== Training Loop =====================
def main():
    parser = argparse.ArgumentParser(description='Train STTF-Recommender')
    parser.add_argument('--data',   type=str, default='data/checkin_sequences.csv')
    parser.add_argument('--epochs', type=int, default=30)
    parser.add_argument('--lr',     type=float, default=1e-3)
    parser.add_argument('--batch',  type=int, default=32)
    parser.add_argument('--ne',     type=int, default=NE,
                        help='Negative samples per positive (Balance Sampler)')
    parser.add_argument('--val-ratio',  type=float, default=0.15,
                        help='Fraction of data for validation')
    parser.add_argument('--test-ratio', type=float, default=0.15,
                        help='Fraction of data for test (held-out, evaluated once)')
    args = parser.parse_args()

    print(f"Loading check-in data from {args.data} ...")
    df = pd.read_csv(args.data)

    train_journeys = []
    val_journeys   = []
    test_journeys  = []

    # Get unique journeys per user with journey_time
    journey_metadata = df[['user_id', 'journey_id', 'journey_time']].drop_duplicates()

    for user_id, group in journey_metadata.groupby('user_id'):
        sorted_group = group.sort_values('journey_time')
        j_ids = sorted_group['journey_id'].tolist()
        n = len(j_ids)

        if n < 3:
            train_journeys.extend(j_ids)
        else:
            val_idx = int(n * 0.70)
            test_idx = int(n * 0.85)
            train_journeys.extend(j_ids[:val_idx])
            val_journeys.extend(j_ids[val_idx:test_idx])
            test_journeys.extend(j_ids[test_idx:])

    train_dataset = CheckInDataset(args.data, allowed_journeys=set(train_journeys))
    val_dataset   = CheckInDataset(args.data, allowed_journeys=set(val_journeys))
    test_dataset  = CheckInDataset(args.data, allowed_journeys=set(test_journeys))

    train_size = len(train_dataset)
    val_size   = len(val_dataset)
    test_size  = len(test_dataset)

    train_loader = DataLoader(train_dataset, batch_size=args.batch, shuffle=True)
    val_loader   = DataLoader(val_dataset,   batch_size=args.batch, shuffle=False)
    test_loader  = DataLoader(test_dataset,  batch_size=args.batch, shuffle=False)
    print(f"  → {train_size} train / {val_size} val / {test_size} test samples")
    print(f"  → {NUM_POIS} POIs loaded from pois.json")

    model     = STTFRecommender()
    optimizer = optim.Adam(model.parameters(), lr=args.lr)
    scheduler = optim.lr_scheduler.StepLR(optimizer, step_size=10, gamma=0.5)

    print(f"\nStarting STTF-Recommender training "
          f"(epochs={args.epochs}, ne={args.ne}, lr={args.lr}) ...")

    best_mrr = 0.0

    for epoch in range(args.epochs):
        # --- Training ---
        model.train()
        epoch_loss = 0.0
        for user_ids, poi_seqs, time_seqs, masks, targets in train_loader:
            optimizer.zero_grad()

            # Trajectory representation S(u)
            S = model.get_trajectory_repr(user_ids, poi_seqs, time_seqs, mask=masks)

            # Balanced cross-entropy loss
            loss = balanced_cross_entropy_loss(model, S, targets, ne=args.ne)
            loss.backward()
            optimizer.step()
            epoch_loss += loss.item()

        scheduler.step()
        avg_loss = epoch_loss / max(len(train_loader), 1)

        # --- Validation ---
        metrics = evaluate(model, val_loader) if val_size > 0 else {}
        val_str = (
            f"  Val Acc@1={metrics['Acc@1']:.3f}  "
            f"Acc@3={metrics['Acc@3']:.3f}  "
            f"MRR={metrics['MRR']:.3f}"
        ) if metrics else ""

        print(f"  Epoch [{epoch+1:2d}/{args.epochs}]  "
              f"Loss: {avg_loss:.4f}  "
              f"LR: {scheduler.get_last_lr()[0]:.5f}{val_str}")

        # Save best model by MRR
        if metrics and metrics['MRR'] > best_mrr:
            best_mrr = metrics['MRR']
            os.makedirs('model', exist_ok=True)
            torch.save(model.state_dict(), 'model/sttf_model.pth')
            print(f"    ↑ New best MRR={best_mrr:.3f} — model saved.")

    # Final save (in case no val data)
    if val_size == 0:
        os.makedirs('model', exist_ok=True)
        torch.save(model.state_dict(), 'model/sttf_model.pth')

    # --- Final Evaluation on Validation ---
    if val_size > 0:
        val_final = evaluate(model, val_loader)
        print(f"\n{'='*55}")
        print(f"  Validation Results ({val_final['total']} samples):")
        print(f"    Acc@1 = {val_final['Acc@1']:.4f}")
        print(f"    Acc@3 = {val_final['Acc@3']:.4f}")
        print(f"    MRR   = {val_final['MRR']:.4f}")

    # --- Held-out Test Set (evaluated ONCE — for thesis report) ---
    if test_size > 0:
        test_final = evaluate(model, test_loader)
        print(f"\n  Test Results ({test_final['total']} samples, held-out):")
        print(f"    Acc@1 = {test_final['Acc@1']:.4f}")
        print(f"    Acc@3 = {test_final['Acc@3']:.4f}")
        print(f"    MRR   = {test_final['MRR']:.4f}")
        print(f"{'='*55}")

    print("\nModel saved → model/sttf_model.pth")

    # Create flag so hot-reload in main.py is triggered
    with open('model_updated.flag', 'w') as f:
        f.write(str(torch.tensor(1).item()))
    print("Hot-reload flag created → model_updated.flag")


if __name__ == '__main__':
    main()

