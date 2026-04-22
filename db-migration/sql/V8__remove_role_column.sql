-- V8: Remove role column from users table
ALTER TABLE users DROP COLUMN IF EXISTS role;
