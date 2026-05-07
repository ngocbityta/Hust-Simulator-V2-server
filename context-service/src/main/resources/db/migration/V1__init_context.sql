CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Virtual Maps
CREATE TABLE virtual_maps (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR NOT NULL,
    coordinates TEXT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Buildings
CREATE TABLE buildings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR NOT NULL,
    map_id UUID NOT NULL REFERENCES virtual_maps(id) ON DELETE CASCADE,
    coordinates TEXT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_buildings_map ON buildings (map_id);

-- Rooms
CREATE TABLE rooms (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR NOT NULL,
    building_id UUID NOT NULL REFERENCES buildings(id) ON DELETE CASCADE,
    status VARCHAR(30) DEFAULT 'EMPTY',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_rooms_building ON rooms (building_id);
CREATE INDEX idx_rooms_status ON rooms(status);

-- Events (Consolidated)
CREATE TABLE events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR NOT NULL,
    description TEXT,
    map_id UUID REFERENCES virtual_maps(id) ON DELETE SET NULL,
    room_id UUID REFERENCES rooms(id) ON DELETE SET NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    type VARCHAR(31), 
    status VARCHAR(30) DEFAULT 'SCHEDULED',
    building_id UUID REFERENCES buildings(id) ON DELETE SET NULL,
    min_x DOUBLE PRECISION,
    min_y DOUBLE PRECISION,
    max_x DOUBLE PRECISION,
    max_y DOUBLE PRECISION,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_events_map ON events (map_id);

-- Event Rooms
CREATE TABLE event_rooms (
    event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    room_id UUID NOT NULL REFERENCES rooms(id) ON DELETE CASCADE,
    PRIMARY KEY (event_id, room_id)
);

-- Recurring Events
CREATE TABLE recurring_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR NOT NULL,
    description TEXT,
    map_id UUID NOT NULL REFERENCES virtual_maps(id) ON DELETE CASCADE,
    room_id UUID REFERENCES rooms(id) ON DELETE SET NULL,
    cron_expression VARCHAR NOT NULL,
    status VARCHAR(30) DEFAULT 'SCHEDULED',
    duration_minutes INTEGER DEFAULT 60,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_recurring_events_map ON recurring_events (map_id);
CREATE INDEX idx_recurring_events_status ON recurring_events(status);

-- Recurring Event Details (From V10)
CREATE TABLE recurring_event_details (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    recurring_event_id UUID NOT NULL REFERENCES recurring_events(id) ON DELETE CASCADE,
    scheduled_at      TIMESTAMP NOT NULL,
    ended_at          TIMESTAMP,
    status            VARCHAR(30) DEFAULT 'SCHEDULED' CHECK (status IN ('SCHEDULED', 'ONGOING', 'COMPLETED')),
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (recurring_event_id, scheduled_at)
);
CREATE INDEX idx_re_details_recurring ON recurring_event_details (recurring_event_id);
CREATE INDEX idx_re_details_status    ON recurring_event_details (status);
CREATE INDEX idx_re_details_scheduled ON recurring_event_details (scheduled_at);

-- Scheduled Jobs Registry
CREATE TABLE scheduled_jobs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    job_id VARCHAR NOT NULL,
    job_type VARCHAR NOT NULL,
    target_time TIMESTAMP NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'DISPATCHED', 'COMPLETED', 'FAILED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (job_id, job_type, target_time)
);
CREATE INDEX idx_scheduled_jobs_status ON scheduled_jobs (status);
CREATE INDEX idx_scheduled_jobs_target ON scheduled_jobs (target_time);
CREATE INDEX idx_scheduled_jobs_target ON scheduled_jobs (target_time);
