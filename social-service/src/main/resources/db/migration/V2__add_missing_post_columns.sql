-- Add missing columns to posts table
ALTER TABLE posts ADD COLUMN event_id UUID;
ALTER TABLE posts ADD COLUMN building_id UUID;
ALTER TABLE posts ADD COLUMN room_id UUID;
