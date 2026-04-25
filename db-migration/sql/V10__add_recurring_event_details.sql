-- V10: Add recurring_event_details table to distinguish each occurrence of a recurring event.
-- Messages are now scoped to a specific detail (occurrence) instead of the parent recurring event.

CREATE TABLE recurring_event_details (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    recurring_event_id UUID NOT NULL REFERENCES recurring_events(id) ON DELETE CASCADE,
    scheduled_at      TIMESTAMP NOT NULL,           -- exact start time of this occurrence
    ended_at          TIMESTAMP,                    -- filled in when END_CLASS fires
    status            VARCHAR(30) DEFAULT 'SCHEDULED'
                          CHECK (status IN ('SCHEDULED', 'ONGOING', 'COMPLETED')),
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (recurring_event_id, scheduled_at)
);

CREATE INDEX idx_re_details_recurring ON recurring_event_details (recurring_event_id);
CREATE INDEX idx_re_details_status    ON recurring_event_details (status);
CREATE INDEX idx_re_details_scheduled ON recurring_event_details (scheduled_at);

-- Re-point messages_chat.event_id → recurring_event_details instead of recurring_events.
-- Existing rows (if any) are kept with event_id set to NULL (nullable transition).
ALTER TABLE messages_chat
    DROP CONSTRAINT IF EXISTS messages_chat_event_id_fkey;

ALTER TABLE messages_chat
    ALTER COLUMN event_id DROP NOT NULL;

-- Reset existing event_ids because they refer to recurring_events, not details.
UPDATE messages_chat SET event_id = NULL;

ALTER TABLE messages_chat
    ADD CONSTRAINT messages_chat_event_id_fkey
        FOREIGN KEY (event_id) REFERENCES recurring_event_details(id) ON DELETE CASCADE;
