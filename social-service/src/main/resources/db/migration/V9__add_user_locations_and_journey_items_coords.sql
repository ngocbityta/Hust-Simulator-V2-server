-- Create user_locations table
CREATE TABLE user_locations (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    timestamp TIMESTAMP NOT NULL
);

-- Index for querying user locations by user and time range efficiently
CREATE INDEX idx_user_locations_user_time ON user_locations(user_id, timestamp);

-- Alter journey_items table
ALTER TABLE journey_items
ADD COLUMN latitude DOUBLE PRECISION,
ADD COLUMN longitude DOUBLE PRECISION,
ADD COLUMN start_time TIMESTAMP,
ADD COLUMN end_time TIMESTAMP;
