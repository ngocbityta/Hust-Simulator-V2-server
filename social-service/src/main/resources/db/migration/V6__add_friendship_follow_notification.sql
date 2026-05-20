-- ============================================================
-- V6: Add Friendship, Follow tables and enhance Notifications
-- ============================================================

-- Friendships table (bidirectional, always store user_id < friend_id)
CREATE TABLE friendships (
    id         UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id    UUID        NOT NULL,
    friend_id  UUID        NOT NULL,
    status     VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    requester_id UUID      NOT NULL,   -- who sent the friend request
    created_at TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, friend_id),
    CHECK  (user_id < friend_id)
);

CREATE INDEX idx_friendships_user      ON friendships(user_id, status);
CREATE INDEX idx_friendships_friend    ON friendships(friend_id, status);
CREATE INDEX idx_friendships_requester ON friendships(requester_id);

-- Follows table (unidirectional: follower → following)
CREATE TABLE follows (
    id           UUID      PRIMARY KEY DEFAULT uuid_generate_v4(),
    follower_id  UUID      NOT NULL,
    following_id UUID      NOT NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (follower_id, following_id)
);

CREATE INDEX idx_follows_follower  ON follows(follower_id);
CREATE INDEX idx_follows_following ON follows(following_id);

-- Enhance existing notifications table
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS sender_id UUID;
CREATE INDEX IF NOT EXISTS idx_notifications_user_read ON notifications(user_id, is_read, created_at DESC);
