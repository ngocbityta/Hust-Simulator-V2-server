import json
import os
import psycopg2
from psycopg2.extras import RealDictCursor
import logging
import random
from collections import defaultdict

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
        
    X_train = []
    X_val = []
    X_test = []
    
    # We will compute global stats to simplify feature extraction for training
    # For a real system, you compute features *prior* to the timestamp, but for fast LR we use global approximations.
    
    all_pois = list(set(r['poi_id'] for r in rows))
    
    for uid, seq in users.items():
        n = len(seq)
        if n < 2: continue
        
        pref_counts = defaultdict(int)
        temp_counts = defaultdict(int)
        trans_counts = defaultdict(int)
        
        train_end = int(0.8 * n)
        val_end = int(0.9 * n)
        
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
                
                if i <= train_end:
                    # 19 Negative samples for Training
                    neg_pids = random.sample([p for p in all_pois if p != pid], min(19, len(all_pois)-1))
                    x_step = [x_pos]
                    for neg_pid in neg_pids:
                        x_step.append([
                            trans_counts.get((prev_pid, neg_pid), 0) / max(1, i),
                            temp_counts.get((hw, neg_pid), 0) / max(1, i),
                            pref_counts.get(neg_pid, 0) / max(1, i)
                        ])
                    X_train.append(x_step)
                else:
                    # ALL Negative samples for Validation & Test (Global Ranking)
                    neg_pids = [p for p in all_pois if p != pid]
                    x_step = [x_pos]
                    for neg_pid in neg_pids:
                        x_step.append([
                            trans_counts.get((prev_pid, neg_pid), 0) / max(1, i),
                            temp_counts.get((hw, neg_pid), 0) / max(1, i),
                            pref_counts.get(neg_pid, 0) / max(1, i)
                        ])
                    
                    if i <= val_end:
                        X_val.append(x_step)
                    else:
                        X_test.append(x_step)
                
            # Update running stats
            pref_counts[pid] += 1
            temp_counts[(hw, pid)] += 1
            if i > 0:
                trans_counts[(seq[i-1]['poi_id'], pid)] += 1
                
    return np.array(X_train), np.array(X_val), np.array(X_test)

from scipy.optimize import minimize

def cce_loss(weights, X):
    if len(X) == 0: return 0
    scores = np.dot(X, weights) # shape (N, M)
    scores -= np.max(scores, axis=1, keepdims=True)
    exp_scores = np.exp(scores)
    probs = exp_scores / np.sum(exp_scores, axis=1, keepdims=True)
    
    true_probs = probs[:, 0]
    eps = 1e-15
    return -np.mean(np.log(true_probs + eps))

def evaluate_metrics(weights, X):
    if len(X) == 0: return {"HR@1": 0.0, "HR@3": 0.0, "HR@5": 0.0, "MRR": 0.0}
    scores = np.dot(X, weights) # shape (N, M)
    
    # argsort sorts ascending, so we take [:, ::-1] to get descending
    sorted_indices = np.argsort(scores, axis=1)[:, ::-1]
    
    # We want to find the rank of the true item (which is at index 0 in the original array)
    ranks = np.argmax(sorted_indices == 0, axis=1)
    
    hr1 = np.mean(ranks < 1)
    hr3 = np.mean(ranks < 3)
    hr5 = np.mean(ranks < 5)
    
    # MRR calculation (rank is 0-indexed, so we add 1)
    mrr = np.mean(1.0 / (ranks + 1))
    
    return {"HR@1": hr1, "HR@3": hr3, "HR@5": hr5, "MRR": mrr}

def visualize_results(val_metrics, test_metrics, weights):
    try:
        import matplotlib.pyplot as plt
        
        fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(12, 5))
        
        # Subplot 1: Metrics Bar Chart
        labels = list(val_metrics.keys())
        val_values = [val_metrics[k] for k in labels]
        test_values = [test_metrics[k] for k in labels]
        
        x = np.arange(len(labels))
        width = 0.35
        
        ax1.bar(x - width/2, val_values, width, label='Validation', color='#4C72B0')
        ax1.bar(x + width/2, test_values, width, label='Test', color='#DD8452')
        
        ax1.set_ylabel('Score')
        ax1.set_title('Evaluation Metrics')
        ax1.set_xticks(x)
        ax1.set_xticklabels(labels)
        ax1.set_ylim(0, 1.05)
        ax1.legend()
        
        # Subplot 2: Weights Pie Chart
        w_labels = ['Transition (Alpha)', 'Temporal (Beta)', 'Preference (Gamma)']
        w_sizes = [weights['alpha'], weights['beta'], weights['gamma']]
        w_colors = ['#55A868', '#C44E52', '#8172B2']
        
        ax2.pie(w_sizes, labels=w_labels, colors=w_colors, autopct='%1.1f%%', startangle=90)
        ax2.set_title('Learned Feature Weights')
        
        plt.tight_layout()
        os.makedirs('model', exist_ok=True)
        plt.savefig('model/evaluation_results.png')
        logger.info("Visualization saved to model/evaluation_results.png")
    except ImportError:
        logger.warning("matplotlib is not installed. Skipping visualization. Run 'pip install matplotlib' to enable it.")
    except Exception as e:
        logger.warning(f"Failed to generate visualization: {e}")

def train_model():
    X_train, X_val, X_test = extract_features()
    if len(X_train) < 10:
        logger.warning("Not enough data to train.")
        return
        
    logger.info(f"Data split: Train={len(X_train)}, Val={len(X_val)}, Test={len(X_test)}")
    logger.info(f"Training CCE model on {len(X_train)} samples with 20 choices per step...")
    
    w0 = np.array([0.4, 0.4, 0.2])
    bounds = [(0, 1), (0, 1), (0, 1)]
    cons = {'type': 'eq', 'fun': lambda w: np.sum(w) - 1.0}
    
    res = minimize(cce_loss, w0, args=(X_train,), bounds=bounds, constraints=cons, method='SLSQP')
    
    coefs = res.x
    logger.info(f"Optimization Success: {res.success}, Loss: {res.fun:.4f}")
    
    if len(X_val) > 0:
        val_metrics = evaluate_metrics(coefs, X_val)
        logger.info(f"Validation Metrics: {val_metrics}")
    if len(X_test) > 0:
        test_metrics = evaluate_metrics(coefs, X_test)
        logger.info(f"Test Metrics: {test_metrics}")
        
    weights = {
        "alpha": float(coefs[0]),
        "beta": float(coefs[1]),
        "gamma": float(coefs[2])
    }
    
    # Ensure no zeroes for core features
    weights['alpha'] = max(weights['alpha'], 0.01)
    weights['beta'] = max(weights['beta'], 0.01)
    weights['gamma'] = max(weights['gamma'], 0.01)
    
    # Re-normalize to ensure sum is exactly 1.0
    total = weights['alpha'] + weights['beta'] + weights['gamma']
    weights['alpha'] /= total
    weights['beta'] /= total
    weights['gamma'] /= total
    
    logger.info(f"Learned Weights: {weights}")
    
    os.makedirs('model', exist_ok=True)
    with open('model/learned_weights.json', 'w') as f:
        json.dump(weights, f)
        
    logger.info("Saved weights to model/learned_weights.json")
    
    if len(X_val) > 0 and len(X_test) > 0:
        visualize_results(val_metrics, test_metrics, weights)

if __name__ == "__main__":
    train_model()
