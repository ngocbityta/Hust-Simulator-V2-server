-- Remove redundant is_active columns from rooms and recurring_events
-- These tables now rely on the 'status' column for state management

ALTER TABLE rooms DROP COLUMN is_active;
ALTER TABLE recurring_events DROP COLUMN is_active;
