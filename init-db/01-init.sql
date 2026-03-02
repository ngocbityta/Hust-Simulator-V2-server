-- Enable PostGIS extension
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- HustSimulator V2 - Database Schema
-- ============================================

-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    phonenumber VARCHAR UNIQUE NOT NULL,
    password VARCHAR NOT NULL,

    username VARCHAR,
    avatar VARCHAR,
    cover_image VARCHAR,
    description TEXT,

    role VARCHAR(2) NOT NULL CHECK (role IN ('HV', 'GV')),

    token VARCHAR,

    status VARCHAR(10) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'LOCKED')),

    online BOOLEAN DEFAULT FALSE,

    -- PostGIS: last known position of the user on campus
    last_position GEOMETRY(POINT, 4326),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Blocks table
CREATE TABLE blocks (
    blocker_id UUID REFERENCES users(id) ON DELETE CASCADE,
    blocked_id UUID REFERENCES users(id) ON DELETE CASCADE,
    PRIMARY KEY (blocker_id, blocked_id)
);

-- Enrollments table
CREATE TABLE enrollments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    student_id UUID REFERENCES users(id) ON DELETE CASCADE,
    teacher_id UUID REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(10) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Posts table
CREATE TABLE posts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    author_id UUID REFERENCES users(id) ON DELETE CASCADE,
    described TEXT,
    course_id UUID,
    exercise_id UUID,
    time_series_poses JSONB,
    can_comment BOOLEAN DEFAULT TRUE,
    can_edit BOOLEAN DEFAULT TRUE,
    is_banned BOOLEAN DEFAULT FALSE,
    -- PostGIS: location where post was created
    location GEOMETRY(POINT, 4326),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Reports table
CREATE TABLE reports (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    post_id UUID REFERENCES posts(id) ON DELETE CASCADE,
    subject VARCHAR,
    details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Post videos table
CREATE TABLE post_videos (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    post_id UUID REFERENCES posts(id) ON DELETE CASCADE,
    url VARCHAR,
    thumb VARCHAR
);

-- Likes table
CREATE TABLE likes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    post_id UUID REFERENCES posts(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, post_id)
);

-- Comments table
CREATE TABLE comments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    post_id UUID REFERENCES posts(id) ON DELETE CASCADE,
    content TEXT,
    score FLOAT,
    detail_mistake TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Conversations table
CREATE TABLE conversations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    partner_a_id UUID REFERENCES users(id) ON DELETE CASCADE,
    partner_b_id UUID REFERENCES users(id) ON DELETE CASCADE,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (partner_a_id, partner_b_id)
);

-- Messages table
CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    conversation_id UUID REFERENCES conversations(id) ON DELETE CASCADE,
    sender_id UUID REFERENCES users(id) ON DELETE CASCADE,
    receiver_id UUID REFERENCES users(id) ON DELETE CASCADE,
    content TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Devices table
CREATE TABLE devices (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    dev_token VARCHAR,
    UNIQUE (user_id, dev_token)
);

-- Push settings table
CREATE TABLE push_settings (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    like_comment BOOLEAN DEFAULT TRUE,
    from_friends BOOLEAN DEFAULT TRUE,
    requested_friend BOOLEAN DEFAULT TRUE,
    suggested_friend BOOLEAN DEFAULT TRUE,
    birthday BOOLEAN DEFAULT TRUE,
    video BOOLEAN DEFAULT TRUE,
    report BOOLEAN DEFAULT TRUE,
    sound_on BOOLEAN DEFAULT TRUE,
    notification_on BOOLEAN DEFAULT TRUE,
    vibrant_on BOOLEAN DEFAULT TRUE,
    led_on BOOLEAN DEFAULT TRUE
);

-- Notifications table
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR,
    object_id UUID,
    title VARCHAR,
    avatar VARCHAR,
    group_type INT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Search history table
CREATE TABLE search_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    keyword VARCHAR,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- Spatial Indexes for PostGIS
-- ============================================
CREATE INDEX idx_users_last_position ON users USING GIST (last_position);
CREATE INDEX idx_posts_location ON posts USING GIST (location);

-- ============================================
-- Campus Zones (for spatial triggers / AOI)
-- ============================================
CREATE TABLE campus_zones (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR NOT NULL,
    type VARCHAR NOT NULL, -- BUILDING, CLASSROOM, LIBRARY, PARKING, EVENT_AREA, etc.
    boundary GEOMETRY(POLYGON, 4326) NOT NULL,
    center GEOMETRY(POINT, 4326),
    radius FLOAT, -- approximate radius in meters
    metadata JSONB DEFAULT '{}',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_campus_zones_boundary ON campus_zones USING GIST (boundary);
CREATE INDEX idx_campus_zones_center ON campus_zones USING GIST (center);
