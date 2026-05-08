-- Create journeys table
CREATE TABLE journeys (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    title VARCHAR(255),
    description TEXT,
    journey_date DATE NOT NULL,
    video_url VARCHAR(255),
    music_url VARCHAR(255),
    template_id VARCHAR(50),
    status VARCHAR(20) DEFAULT 'DRAFT',
    visibility VARCHAR(20) DEFAULT 'PUBLIC',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for fast lookup by user and date
CREATE INDEX idx_journeys_user_date ON journeys(user_id, journey_date);

-- Create journey_items table
CREATE TABLE journey_items (
    id UUID PRIMARY KEY,
    journey_id UUID REFERENCES journeys(id) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL,
    reference_id UUID,
    media_url TEXT,
    content TEXT,
    timestamp TIMESTAMP NOT NULL,
    sort_order INT NOT NULL,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for ordering items within a journey
CREATE INDEX idx_journey_items_journey_order ON journey_items(journey_id, sort_order);
