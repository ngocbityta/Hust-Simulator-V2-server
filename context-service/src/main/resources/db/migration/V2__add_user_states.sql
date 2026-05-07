-- User States (Added for state management)
CREATE TABLE IF NOT EXISTS user_states (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID UNIQUE NOT NULL,
    activity_state VARCHAR(50) NOT NULL DEFAULT 'OUTSIDE_MAP',
    map_id UUID,
    building_id UUID,
    room_id UUID,
    event_id UUID,
    session_data JSONB DEFAULT '{}',
    entered_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_user_states_user ON user_states (user_id);
CREATE INDEX IF NOT EXISTS idx_user_states_activity ON user_states (activity_state);
