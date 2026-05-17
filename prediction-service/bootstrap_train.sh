#!/bin/bash
# ============================================================
# Bootstrap: Generate synthetic data + Train STTF-Recommender
# Run this once for cold-start model initialization.
# ============================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

MODEL_FILE="model/sttf_model.pth"

echo "========================================"
echo "  STTF-Recommender Bootstrap Pipeline"
echo "========================================"

# Step 1: Generate synthetic trajectory data
echo ""
echo "[1/3] Generating synthetic trajectory data..."
python3 generate_synthetic_data.py \
    --num-users 200 \
    --journeys-per-user 15 \
    --seed 42

# Step 2: Train model
echo ""
echo "[2/3] Training STTF-Recommender model..."
python3 train.py \
    --data data/checkin_sequences.csv \
    --epochs 30 \
    --batch 32 \
    --lr 0.001

# Step 3: Verify
echo ""
echo "[3/3] Verifying..."
if [ -f "$MODEL_FILE" ]; then
    SIZE=$(du -h "$MODEL_FILE" | cut -f1)
    echo "  ✓ Model created: $MODEL_FILE ($SIZE)"
    echo ""
    echo "Bootstrap complete! The prediction-service can now serve predictions."
else
    echo "  ✗ ERROR: Model file not found at $MODEL_FILE"
    exit 1
fi
