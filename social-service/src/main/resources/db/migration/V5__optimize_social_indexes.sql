-- Optimize post retrieval for Journey Preview, User Feed, and specialized filters
CREATE INDEX IF NOT EXISTS idx_posts_user_created ON posts(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_posts_event_created ON posts(event_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_posts_building_created ON posts(building_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_posts_room_created ON posts(room_id, created_at DESC);

-- Optimize journey listing and daily retrieval
CREATE INDEX IF NOT EXISTS idx_journeys_user_created ON journeys(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_journeys_user_date ON journeys(user_id, journey_date);

-- Optimize loading items for a specific journey
CREATE INDEX IF NOT EXISTS idx_journey_items_journey_sort ON journey_items(journey_id, sort_order);

-- Optimize comment retrieval for posts and user activity
CREATE INDEX IF NOT EXISTS idx_comments_post_created ON comments(post_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_comments_user_created ON comments(user_id, created_at DESC);

-- Optimize like counts and checks
CREATE INDEX IF NOT EXISTS idx_likes_post_id ON likes(post_id);
