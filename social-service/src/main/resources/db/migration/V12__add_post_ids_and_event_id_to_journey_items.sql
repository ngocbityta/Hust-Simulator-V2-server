ALTER TABLE social.journey_items
ADD COLUMN event_id UUID,
ADD COLUMN post_ids UUID[] DEFAULT '{}';
