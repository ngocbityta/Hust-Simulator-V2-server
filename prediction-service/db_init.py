import os
import sys
import logging
from yoyo import read_migrations, get_backend

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger("DBMigration")

DB_HOST     = os.environ.get('POSTGRES_HOST',     'localhost')
DB_NAME     = os.environ.get('POSTGRES_DB',       'neondb')
DB_USER     = os.environ.get('POSTGRES_USER',     'neondb_owner')
DB_PASSWORD = os.environ.get('POSTGRES_PASSWORD', 'password')
DB_PORT     = os.environ.get('POSTGRES_PORT',     '5432')
DB_SSLMODE  = os.environ.get('POSTGRES_SSL',      'disable')

MIGRATIONS_DIR = os.path.join(os.path.dirname(__file__), 'db', 'migration')

def run_migrations():
    logger.info("Starting Pythonic Database Migrations using yoyo-migrations...")
    
    # Construct standard connection string for Yoyo
    db_url = f"postgresql://{DB_USER}:{DB_PASSWORD}@{DB_HOST}:{DB_PORT}/{DB_NAME}?sslmode={DB_SSLMODE}"
    
    try:
        # Get Yoyo backend and isolate migration history inside the 'prediction' schema
        backend = get_backend(db_url, migration_table='yoyo_migration_prediction')
        
        # Read raw SQL migrations
        migrations = read_migrations(MIGRATIONS_DIR)
        logger.info(f"Loaded {len(migrations)} migrations from {MIGRATIONS_DIR}")
        
        # Apply pending migrations programmatically with a lock
        with backend.lock():
            backend.apply_migrations(backend.to_apply(migrations))
            
        logger.info("Database migrations completed successfully.")
    except Exception as e:
        logger.error(f"Migration process encountered a critical error: {e}")
        sys.exit(1)

if __name__ == '__main__':
    run_migrations()
