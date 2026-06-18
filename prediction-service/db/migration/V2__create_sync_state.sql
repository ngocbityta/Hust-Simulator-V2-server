CREATE TABLE IF NOT EXISTS prediction.sync_state (
    id VARCHAR(50) PRIMARY KEY,
    last_sync_time TIMESTAMP NOT NULL
);

-- Initialize the watermark with a very old date so the first run syncs everything available (max 3 days anyway)
INSERT INTO prediction.sync_state (id, last_sync_time) 
VALUES ('passive_collector', '2000-01-01 00:00:00')
ON CONFLICT (id) DO NOTHING;
