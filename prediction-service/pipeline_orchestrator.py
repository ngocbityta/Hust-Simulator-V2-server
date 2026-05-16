import schedule
import time
import subprocess
import logging
import os
from datetime import datetime

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
logger = logging.getLogger('PipelineOrchestrator')

def run_pipeline():
    logger.info("Starting scheduled AI pipeline check...")
    
    # 1. Check and Extract Data
    extract_result = subprocess.run(['python', 'extract_and_preprocess.py'], capture_output=True, text=True)
    logger.info(f"Extract Output:\n{extract_result.stdout}")
    
    if extract_result.returncode != 0:
        logger.error(f"Extraction failed: {extract_result.stderr}")
        return
        
    if "Threshold not met" in extract_result.stdout or "No rows extracted" in extract_result.stdout:
        logger.info("No new data to train on. Pipeline check complete.")
        return
        
    # 2. Train Model
    logger.info("New data extracted. Starting training...")
    train_result = subprocess.run(['python', 'train.py', '--data', 'data/real_trajectories.csv'], capture_output=True, text=True)
    logger.info(f"Train Output:\n{train_result.stdout}")
    
    if train_result.returncode != 0:
        logger.error(f"Training failed: {train_result.stderr}")
        return
        
    # 3. Notify main.py to hot-reload
    logger.info("Training successful. Updating model flag...")
    with open('model_updated.flag', 'w') as f:
        f.write(str(datetime.now().timestamp()))
        
    logger.info("Pipeline run complete.")

def main():
    logger.info("Pipeline Orchestrator started. Scheduling check every 1 hour.")
    # For demo purposes, we can run it every 10 minutes or use run_pipeline() immediately
    schedule.every(1).hours.do(run_pipeline)
    
    # Run once at startup
    run_pipeline()
    
    while True:
        schedule.run_pending()
        time.sleep(60)

if __name__ == "__main__":
    main()
