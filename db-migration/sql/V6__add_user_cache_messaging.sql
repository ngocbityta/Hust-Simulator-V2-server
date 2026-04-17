-- Create user_cache table for Messaging Service (Event-Driven Data Replication)
CREATE TABLE IF NOT EXISTS user_cache (
    id UUID PRIMARY KEY,
    username VARCHAR(255),
    avatar TEXT,
    synced_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Index for faster lookups
CREATE INDEX IF NOT EXISTS idx_user_cache_msg_username ON user_cache(username);
