CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Conversations table
CREATE TABLE conversations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    partner_a_id UUID NOT NULL, -- Logical FK to auth.users
    partner_b_id UUID NOT NULL, -- Logical FK to auth.users
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (partner_a_id, partner_b_id)
);

-- Messages table (Direct Messaging)
CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    conversation_id UUID REFERENCES conversations(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL, -- Logical FK to auth.users
    receiver_id UUID NOT NULL, -- Logical FK to auth.users
    content TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Group Chats for Events
CREATE TABLE messages_chat (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    event_id UUID, -- Logical FK to context.recurring_event_details
    sender_id UUID NOT NULL, -- Logical FK to auth.users
    type VARCHAR(20) NOT NULL DEFAULT 'text' CHECK (type IN ('text', 'file', 'image')),
    content TEXT,
    file_id UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_messages_chat_event ON messages_chat (event_id);
CREATE INDEX idx_messages_chat_sender ON messages_chat (sender_id);
CREATE INDEX idx_messages_chat_created ON messages_chat (created_at);

-- Stored Files
CREATE TABLE stored_files (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    original_name VARCHAR NOT NULL,
    file_type VARCHAR(100),
    file_url VARCHAR NOT NULL,
    file_size BIGINT,
    uploaded_by UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User Cache (Event-Driven)
CREATE TABLE user_cache (
    id UUID PRIMARY KEY,
    username VARCHAR(255),
    avatar TEXT,
    synced_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_user_cache_msg_username ON user_cache(username);
