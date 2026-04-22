-- V7: Align schema with entities based on audit

-- 1. users table fixes
-- DB có các cột legacy bắt buộc nhưng Entity không có, gây lỗi khi insert.
ALTER TABLE users ALTER COLUMN email DROP NOT NULL;
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_username_key;
ALTER TABLE users ALTER COLUMN username DROP NOT NULL;

-- 2. events table fixes for Single Table Inheritance
-- Thêm các cột cho IndoorEvent và OutdoorEvent
ALTER TABLE events ADD COLUMN IF NOT EXISTS building_id UUID REFERENCES buildings(id) ON DELETE SET NULL;
ALTER TABLE events ADD COLUMN IF NOT EXISTS min_x DOUBLE PRECISION;
ALTER TABLE events ADD COLUMN IF NOT EXISTS min_y DOUBLE PRECISION;
ALTER TABLE events ADD COLUMN IF NOT EXISTS max_x DOUBLE PRECISION;
ALTER TABLE events ADD COLUMN IF NOT EXISTS max_y DOUBLE PRECISION;

-- 3. event_rooms table creation
-- Entity IndoorEvent sử dụng bảng event_rooms qua @ElementCollection nhưng bảng này chưa tồn tại
CREATE TABLE IF NOT EXISTS event_rooms (
    event_id UUID NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    room_id UUID NOT NULL REFERENCES rooms(id) ON DELETE CASCADE,
    PRIMARY KEY (event_id, room_id)
);

-- 4. stored_files fixes
-- Entity StoredFile kế thừa BaseEntity có trường updatedAt nhưng DB chưa có
ALTER TABLE stored_files ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
