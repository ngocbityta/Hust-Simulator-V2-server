import schedule
import time
import subprocess
import logging
import sys
from datetime import datetime

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
logger = logging.getLogger('PipelineOrchestrator')

def run_passive_collection():
    logger.info("Running scheduled passive trajectory collection from Redis...")
    result = subprocess.run([sys.executable, 'passive_collector.py'], capture_output=True, text=True)
    logger.info(f"Passive Collector Output:\n{result.stdout}")
    if result.returncode != 0:
        logger.error(f"Passive collection failed: {result.stderr}")

def run_pipeline():
    logger.info("Starting scheduled AI pipeline check...")
    
    # 0. Sync POIs from Database (using context.buildings)
    logger.info("Checking for POI updates from database...")
    sync_result = subprocess.run([sys.executable, 'sync_pois.py'], capture_output=True, text=True)
    
    if sync_result.returncode == 1:
        logger.info("POIs have changed. Re-running Logistic Regression...")
        train_result = subprocess.run([sys.executable, 'train.py'], capture_output=True, text=True)
        logger.info(f"Train Output:\n{train_result.stdout}")
        return
    elif sync_result.returncode != 0:
        logger.error(f"POI Sync failed: {sync_result.stderr}")
        return
        
    # 2. Train Model
    logger.info("Running scheduled Logistic Regression weight update...")
    train_result = subprocess.run([sys.executable, 'train.py'], capture_output=True, text=True)
    logger.info(f"Train Output:\n{train_result.stdout}")
    
    if train_result.returncode != 0:
        logger.error(f"Training failed: {train_result.stderr}")
        return
        
    logger.info("Pipeline run complete.")

def main():
    logger.info("Pipeline Orchestrator starting...")
    
    # Run DB migration first
    logger.info("Initializing prediction database schema...")
    db_result = subprocess.run([sys.executable, 'db_init.py'], capture_output=True, text=True)
    logger.info(f"DB Init Output:\n{db_result.stdout}")
    if db_result.returncode != 0:
        logger.error(f"Database initialization failed: {db_result.stderr}")
        sys.exit(1)
        
    # Schedule passive collection every 3 hours
    schedule.every(3).hours.do(run_passive_collection)
    
    # Schedule full ETL & Training pipeline every 1 hour
    schedule.every(1).hours.do(run_pipeline)
    
    # Run once at startup
    run_passive_collection()
    run_pipeline()
    
    logger.info("Pipeline Orchestrator scheduled loops started.")
    while True:
        schedule.run_pending()
        time.sleep(60)

if __name__ == "__main__":
    main()
