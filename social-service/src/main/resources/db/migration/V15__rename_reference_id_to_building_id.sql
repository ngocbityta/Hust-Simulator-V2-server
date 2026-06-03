ALTER TABLE social.journey_items
RENAME COLUMN reference_id TO building_id;

ALTER TABLE social.journey_items
DROP COLUMN metadata;
