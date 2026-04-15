-- Add missing created_at column to user_states to match JPA BaseEntity
ALTER TABLE user_states 
ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
