-- V3: Align users table schema with auth-service User entity.
-- Handles existing databases where V1 was applied with the old schema.
-- Uses IF EXISTS / IF NOT EXISTS to be idempotent.

-- Add phonenumber column (primary identifier for auth)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'users' AND column_name = 'phonenumber') THEN
        ALTER TABLE users ADD COLUMN phonenumber VARCHAR UNIQUE;
    END IF;
END $$;

-- Rename password_hash → password (if old column exists)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'users' AND column_name = 'password_hash')
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns 
                       WHERE table_name = 'users' AND column_name = 'password') THEN
        ALTER TABLE users RENAME COLUMN password_hash TO password;
    END IF;
END $$;

-- Add missing columns from User entity
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'users' AND column_name = 'cover_image') THEN
        ALTER TABLE users ADD COLUMN cover_image VARCHAR;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'users' AND column_name = 'description') THEN
        ALTER TABLE users ADD COLUMN description TEXT;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'users' AND column_name = 'role') THEN
        ALTER TABLE users ADD COLUMN role VARCHAR(2) NOT NULL DEFAULT 'HV';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'users' AND column_name = 'token') THEN
        ALTER TABLE users ADD COLUMN token VARCHAR;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'users' AND column_name = 'online') THEN
        ALTER TABLE users ADD COLUMN online BOOLEAN DEFAULT FALSE;
    END IF;

    -- Rename avatar_url → avatar if needed
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'users' AND column_name = 'avatar_url')
       AND NOT EXISTS (SELECT 1 FROM information_schema.columns 
                       WHERE table_name = 'users' AND column_name = 'avatar') THEN
        ALTER TABLE users RENAME COLUMN avatar_url TO avatar;
    END IF;
END $$;

-- Add updated_at to comments table if missing
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'comments' AND column_name = 'updated_at') THEN
        ALTER TABLE comments ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
    END IF;
END $$;

-- Drop old spatial index on last_position if it exists
DROP INDEX IF EXISTS idx_users_last_position;
