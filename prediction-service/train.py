import json
import os
import psycopg2
from psycopg2.extras import RealDictCursor
import logging
import random
from collections import defaultdict
from sklearn.linear_model import LogisticRegression
import numpy as np

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger('Trainer')

DB_HOST = os.environ.get('POSTGRES_HOST', 'localhost')
DB_USER = os.environ.get('POSTGRES_USER', 'neondb_owner')
DB_PASSWORD = os.environ.get('POSTGRES_PASSWORD', 'password')
DB_PORT = os.environ.get('POSTGRES_PORT', '5432')
DB_PREDICTION = os.environ.get('POSTGRES_DB', 'neondb')

def get_prediction_db():
    return psycopg2.connect(host=DB_HOST, database=DB_PREDICTION, user=DB_USER, password=DB_PASSWORD, port=DB_PORT)

def extract_features():
    logger.info("Extracting features from historical data...")
    conn = get_prediction_db()
    cursor = conn.cursor(cursor_factory=RealDictCursor)
    cursor.execute("SELECT user_id, poi_id, timestamp FROM prediction.checkin_sequences ORDER BY user_id, timestamp")
    rows = cursor.fetchall()
    
    # Group by user
    users = defaultdict(list)
    for r in rows:
        users[r['user_id']].append(r)
        
    X = []
    y = []
    
    # We will compute global stats to simplify feature extraction for training
    # For a real system, you compute features *prior* to the timestamp, but for fast LR we use global approximations.
    
    all_pois = list(set(r['poi_id'] for r in rows))
    
    for uid, seq in users.items():
        if len(seq) < 2: continue
        
        pref_counts = defaultdict(int)
        temp_counts = defaultdict(int)
        trans_counts = defaultdict(int)
        
        for i, pt in enumerate(seq):
            pid = pt['poi_id']
            hw = pt['timestamp'].weekday() * 24 + pt['timestamp'].hour
            
            # Predict pt[i] given history up to i-1
            if i > 0:
                prev_pid = seq[i-1]['poi_id']
                
                # Positive sample (True next location)
                x_pos = [
                    trans_counts.get((prev_pid, pid), 0) / max(1, i),
                    temp_counts.get((hw, pid), 0) / max(1, i),
                    pref_counts.get(pid, 0) / max(1, i)
                ]
                X.append(x_pos)
                y.append(1)
                
                # Negative sample (Random incorrect location)
                neg_pid = random.choice(all_pois)
                while neg_pid == pid: neg_pid = random.choice(all_pois)
                
                x_neg = [
                    trans_counts.get((prev_pid, neg_pid), 0) / max(1, i),
                    temp_counts.get((hw, neg_pid), 0) / max(1, i),
                    pref_counts.get(neg_pid, 0) / max(1, i)
                ]
                X.append(x_neg)
                y.append(0)
                
            # Update running stats
            pref_counts[pid] += 1
            temp_counts[(hw, pid)] += 1
            if i > 0:
                trans_counts[(seq[i-1]['poi_id'], pid)] += 1
                
    return np.array(X), np.array(y)

def train_model():
    X, y = extract_features()
    if len(X) < 10:
        logger.warning("Not enough data to train Logistic Regression.")
        return
        
    logger.info(f"Training Logistic Regression on {len(X)} samples...")
    clf = LogisticRegression(fit_intercept=False) # Force positive weights
    clf.fit(X, y)
    
    # LR coefficients can be negative — clamp to 0 to avoid nonsensical negative probabilities
    coefs = np.maximum(clf.coef_[0], 0)
    
    # If all coefficients are zero after clamping, use defaults
    total = np.sum(coefs)
    if total > 0:
        coefs = coefs / total
    else:
        coefs = np.array([0.4, 0.4, 0.2])
        
    weights = {
        "alpha": float(coefs[0]),
        "beta": float(coefs[1]),
        "gamma": float(coefs[2])
    }
    
    # Ensure no zeroes for core features
    if weights['alpha'] == 0: weights['alpha'] = 0.4
    if weights['beta'] == 0: weights['beta'] = 0.4
    if weights['gamma'] == 0: weights['gamma'] = 0.2
    
    logger.info(f"Learned Weights: {weights}")
    
    os.makedirs('model', exist_ok=True)
    with open('model/learned_weights.json', 'w') as f:
        json.dump(weights, f)
        
    logger.info("Saved weights to model/learned_weights.json")

if __name__ == "__main__":
    train_model()
