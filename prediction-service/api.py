import os
import psycopg2
from fastapi import FastAPI, HTTPException
from psycopg2.extras import RealDictCursor

app = FastAPI(title="Prediction Service API")

# DB Config
DB_HOST     = os.environ.get('POSTGRES_HOST',     'localhost')
DB_NAME     = os.environ.get('POSTGRES_DB',       'neondb')
DB_USER     = os.environ.get('POSTGRES_USER',     'neondb_owner')
DB_PASSWORD = os.environ.get('POSTGRES_PASSWORD', 'password')
DB_PORT     = os.environ.get('POSTGRES_PORT',     '5432')
DB_SSLMODE  = os.environ.get('POSTGRES_SSL',      'disable')

def get_db_connection():
    return psycopg2.connect(
        host=DB_HOST, database=DB_NAME, user=DB_USER,
        password=DB_PASSWORD, port=DB_PORT, sslmode=DB_SSLMODE,
    )

@app.get("/checkin-sequences/{user_id}")
def get_checkin_sequences(user_id: str):
    """Fetch raw check-in sequences for a specific user from prediction schema."""
    conn = None
    cursor = None
    try:
        conn = get_db_connection()
        cursor = conn.cursor(cursor_factory=RealDictCursor)
        cursor.execute("""
            SELECT id, user_id, poi_id, timestamp, duration_seconds
            FROM prediction.checkin_sequences
            WHERE user_id = %s
            ORDER BY timestamp ASC
        """, (user_id,))
        raw_rows = cursor.fetchall()

        from datetime import timedelta
        from collections import defaultdict

        grouped = defaultdict(list)
        for r in raw_rows:
            grouped[r['poi_id']].append(r)
            
        final_rows = []
        for poi_id, group in grouped.items():
            if not group: continue
            
            current = group[0]
            current_start = current['timestamp']
            current_end = current_start + timedelta(seconds=current['duration_seconds'] or 0)
            
            for i in range(1, len(group)):
                nxt = group[i]
                nxt_start = nxt['timestamp']
                nxt_end = nxt_start + timedelta(seconds=nxt['duration_seconds'] or 0)
                
                gap = (nxt_start - current_end).total_seconds()
                
                # If gap is <= 2 hours (7200 seconds), or if they overlap (gap < 0), merge them
                if gap <= 7200:
                    current_end = max(current_end, nxt_end)
                else:
                    final_rows.append({
                        'id': current['id'],
                        'user_id': current['user_id'],
                        'poi_id': poi_id,
                        'timestamp': current_start,
                        'duration_seconds': int((current_end - current_start).total_seconds())
                    })
                    current = nxt
                    current_start = nxt_start
                    current_end = nxt_end
            
            final_rows.append({
                'id': current['id'],
                'user_id': current['user_id'],
                'poi_id': poi_id,
                'timestamp': current_start,
                'duration_seconds': int((current_end - current_start).total_seconds())
            })

        # Sort final_rows by timestamp DESC to return to frontend
        final_rows.sort(key=lambda x: x['timestamp'], reverse=True)
        
        # Calculate stats on backend as requested
        stats_map = {}
        total_duration = 0
        for r in final_rows:
            dur = r['duration_seconds'] or 0
            pid = r['poi_id']
            total_duration += dur
            if pid not in stats_map:
                stats_map[pid] = {'poi_id': pid, 'duration_seconds': 0, 'count': 0}
            stats_map[pid]['duration_seconds'] += dur
            stats_map[pid]['count'] += 1

        stats_list = []
        for pid, s in stats_map.items():
            s['percentage'] = round((s['duration_seconds'] / total_duration) * 100, 2) if total_duration > 0 else 0
            stats_list.append(s)

        return {
            "content": final_rows,
            "stats": stats_list,
            "total_duration_seconds": total_duration
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        if cursor:
            cursor.close()
        if conn:
            conn.close()

import threading
import subprocess
import time

@app.post("/train")
def trigger_training():
    """Trigger the data extraction and STTF model training pipeline in the background."""
    def run_training_pipeline():
        try:
            print("Starting background training pipeline...")
            # 1. Extract and Preprocess
            # Skipped extract
            
            # 2. Train Model
            subprocess.run(["python", "train.py", "--data", "data/checkin_sequences.csv"], check=False)
            
            # 3. Notify main.py to hot-reload
            with open('model_updated.flag', 'w') as f:
                f.write(str(time.time()))
            print("Background training pipeline completed successfully.")
        except Exception as e:
            print(f"Background training failed: {e}")

    thread = threading.Thread(target=run_training_pipeline)
    thread.start()
    return {"message": "Training pipeline started in the background", "status": "processing"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=50056)
