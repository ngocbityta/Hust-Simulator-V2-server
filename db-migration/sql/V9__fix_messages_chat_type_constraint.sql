-- V9: Update messages_chat type check constraint to match Java MessageType enum

-- Drop the default to avoid default constraint issues if we alter type
ALTER TABLE messages_chat ALTER COLUMN type DROP DEFAULT;

-- Drop the existing check constraint
ALTER TABLE messages_chat DROP CONSTRAINT IF EXISTS messages_chat_type_check;

-- Update any existing lowercase values to uppercase to match the Java Enum MessageType
UPDATE messages_chat SET type = UPPER(type) WHERE type IN ('text', 'file', 'image');

-- Add the new check constraint with uppercase values
ALTER TABLE messages_chat ADD CONSTRAINT messages_chat_type_check CHECK (type IN ('TEXT', 'FILE', 'IMAGE'));

-- Set the default value to the uppercase 'TEXT'
ALTER TABLE messages_chat ALTER COLUMN type SET DEFAULT 'TEXT';
