CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    phonenumber VARCHAR UNIQUE NOT NULL,
    password VARCHAR NOT NULL,
    username VARCHAR,
    avatar VARCHAR,
    cover_image VARCHAR,
    description TEXT,
    token VARCHAR,
    status VARCHAR(10) DEFAULT 'ACTIVE',
    online BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE blocks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    blocker_id UUID REFERENCES users(id) ON DELETE CASCADE,
    blocked_id UUID REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (blocker_id, blocked_id)
);

CREATE TABLE devices (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    dev_token VARCHAR,
    UNIQUE (user_id, dev_token)
);

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
