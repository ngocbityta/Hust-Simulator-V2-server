CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS postgis;

-- Posts table
CREATE TABLE posts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL, -- Logical FK to auth.users
    content TEXT,
    video_url VARCHAR,
    status VARCHAR DEFAULT 'ACTIVE',
    location GEOMETRY(Point, 4326),
    can_edit VARCHAR DEFAULT '1',
    can_comment VARCHAR DEFAULT '1',
    banned VARCHAR DEFAULT '0',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_posts_location ON posts USING GIST (location);

-- Post images table
CREATE TABLE post_images (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    post_id UUID REFERENCES posts(id) ON DELETE CASCADE,
    url VARCHAR,
    description TEXT
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
    user_id UUID NOT NULL, -- Logical FK to auth.users
    post_id UUID REFERENCES posts(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, post_id)
);

-- Comments table
CREATE TABLE comments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL, -- Logical FK to auth.users
    post_id UUID REFERENCES posts(id) ON DELETE CASCADE,
    content TEXT,
    score FLOAT,
    detail_mistake TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Notifications table
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
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
    user_id UUID NOT NULL,
    keyword VARCHAR,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User Cache (Event-Driven)
CREATE TABLE user_cache (
    id UUID PRIMARY KEY,
    username VARCHAR(255),
    phonenumber VARCHAR(20),
    avatar TEXT,
    cover_image TEXT,
    description TEXT,
    role VARCHAR(20),
    synced_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_user_cache_username ON user_cache(username);
