ALTER TABLE prediction.checkin_sequences
    ADD COLUMN IF NOT EXISTS duration_seconds INTEGER DEFAULT 0;
