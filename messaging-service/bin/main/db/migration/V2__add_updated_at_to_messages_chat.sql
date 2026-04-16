-- messaging-service owns this migration.
-- Adds updated_at column to messages_chat table (messaging domain).
-- NOTE: This column was previously added by context-service/V2 migration in an earlier deployment.
-- We use DO $$ ... IF NOT EXISTS to make this idempotent.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'messages_chat' AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE messages_chat ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
    END IF;
END $$;
