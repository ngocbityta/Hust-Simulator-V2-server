-- Create user_cache table for Social Service (Event-Driven Data Replication)
CREATE TABLE IF NOT EXISTS user_cache (
    id UUID PRIMARY KEY,
    username VARCHAR(255),
    phonenumber VARCHAR(20),
    avatar TEXT,
    cover_image TEXT,
    description TEXT,
    role VARCHAR(20),
    synced_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Index for faster lookups
CREATE INDEX IF NOT EXISTS idx_user_cache_username ON user_cache(username);
