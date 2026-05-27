-- Add missing updated_at to campus_edges to match BaseEntity
ALTER TABLE campus_edges ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
