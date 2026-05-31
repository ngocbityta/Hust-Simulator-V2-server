-- =============================================================================
-- V9: Refactor campus graph to OSM-style "ways" model
-- - DROP campus_edges table (replaced by campus_ways)
-- - CREATE campus_ways table (polyline paths, like OSM ways)
-- - Remove INTERSECTION nodes (no longer needed)
-- - Update campus_nodes CHECK constraint to replace INTERSECTION with CUSTOM
-- =============================================================================

-- 1. Drop old edge-based table
DROP TABLE IF EXISTS campus_edges;

-- 2. Create campus_ways table (OSM-style polyline paths)
CREATE TABLE campus_ways (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    way_type VARCHAR(50) NOT NULL CHECK (way_type IN ('ROAD', 'FOOTPATH', 'ALLEY')),
    coordinates TEXT NOT NULL,  -- JSON: [[lng, lat], [lng, lat], ...]
    distance_meters DOUBLE PRECISION,
    is_oneway BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_campus_ways_type ON campus_ways(way_type);
CREATE INDEX idx_campus_ways_active ON campus_ways(is_active);

-- 3. Remove all INTERSECTION nodes (no longer part of the model)
DELETE FROM campus_nodes WHERE node_type = 'INTERSECTION';

-- 4. Update CHECK constraint on campus_nodes to replace INTERSECTION with CUSTOM
ALTER TABLE campus_nodes DROP CONSTRAINT IF EXISTS campus_nodes_node_type_check;
ALTER TABLE campus_nodes ADD CONSTRAINT campus_nodes_node_type_check
    CHECK (node_type IN ('GATE', 'PARKING', 'CANTEEN', 'CUSTOM', 'BUILDING'));
