-- Add missing columns to posts table
ALTER TABLE posts ADD COLUMN IF NOT EXISTS event_id UUID;
ALTER TABLE posts ADD COLUMN IF NOT EXISTS building_id UUID;
ALTER TABLE posts ADD COLUMN IF NOT EXISTS room_id UUID;
