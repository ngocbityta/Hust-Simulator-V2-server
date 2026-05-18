CREATE SCHEMA IF NOT EXISTS prediction;

CREATE TABLE IF NOT EXISTS prediction.checkin_sequences (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    poi_id UUID NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
