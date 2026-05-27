-- Campus Graph: Nodes represent intersections, gates, parking areas, canteens, etc.
-- Edges represent walkable paths between nodes.

-- 1. Add centroid columns to buildings table (computed from polygon, stored for fast access)
ALTER TABLE buildings ADD COLUMN centroid_lat DOUBLE PRECISION;
ALTER TABLE buildings ADD COLUMN centroid_lng DOUBLE PRECISION;

-- 2. Populate centroids from existing polygon coordinates using a PL/pgSQL function
-- This computes the arithmetic mean of all polygon vertices (simple centroid approximation)
DO $$
DECLARE
    b RECORD;
    coords JSONB;
    point JSONB;
    sum_lat DOUBLE PRECISION;
    sum_lng DOUBLE PRECISION;
    num_points INTEGER;
BEGIN
    FOR b IN SELECT id, coordinates FROM buildings WHERE coordinates IS NOT NULL LOOP
        BEGIN
            coords := b.coordinates::JSONB;
            sum_lat := 0;
            sum_lng := 0;
            num_points := 0;

            FOR point IN SELECT * FROM jsonb_array_elements(coords) LOOP
                -- coordinates are stored as [lng, lat]
                sum_lng := sum_lng + (point->>0)::DOUBLE PRECISION;
                sum_lat := sum_lat + (point->>1)::DOUBLE PRECISION;
                num_points := num_points + 1;
            END LOOP;

            IF num_points > 0 THEN
                UPDATE buildings
                SET centroid_lat = sum_lat / num_points,
                    centroid_lng = sum_lng / num_points
                WHERE id = b.id;
            END IF;
        EXCEPTION WHEN OTHERS THEN
            RAISE NOTICE 'Skipping building % due to parse error', b.id;
        END;
    END LOOP;
END $$;

-- 3. Campus nodes table
CREATE TABLE campus_nodes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL UNIQUE,
    node_type VARCHAR(50) NOT NULL CHECK (node_type IN ('GATE', 'PARKING', 'CANTEEN', 'INTERSECTION', 'BUILDING')),
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    building_id UUID REFERENCES buildings(id) ON DELETE SET NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_campus_nodes_type ON campus_nodes(node_type);
CREATE INDEX idx_campus_nodes_building ON campus_nodes(building_id);

-- 4. Campus edges table
CREATE TABLE campus_edges (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    from_node_id UUID NOT NULL REFERENCES campus_nodes(id) ON DELETE CASCADE,
    to_node_id UUID NOT NULL REFERENCES campus_nodes(id) ON DELETE CASCADE,
    distance_meters DOUBLE PRECISION,
    is_bidirectional BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(from_node_id, to_node_id)
);

CREATE INDEX idx_campus_edges_from ON campus_edges(from_node_id);
CREATE INDEX idx_campus_edges_to ON campus_edges(to_node_id);

-- 5. Seed infrastructure nodes: Gates
INSERT INTO campus_nodes (name, node_type, latitude, longitude) VALUES
    ('Cổng Đại Cồ Việt', 'GATE', 21.0073, 105.8445),
    ('Cổng Giải Phóng', 'GATE', 21.0045, 105.8418),
    ('Cổng Trần Đại Nghĩa', 'GATE', 21.0028, 105.8470);

-- 6. Seed infrastructure nodes: Parking areas
INSERT INTO campus_nodes (name, node_type, latitude, longitude) VALUES
    ('Nhà xe C', 'PARKING', 21.0063, 105.8435),
    ('Nhà xe D', 'PARKING', 21.0042, 105.8430),
    ('Nhà xe KTX', 'PARKING', 21.0058, 105.8465);

-- 7. Seed infrastructure nodes: Canteens (using centroids from buildings)
INSERT INTO campus_nodes (name, node_type, latitude, longitude, building_id)
SELECT b.name, 'CANTEEN', b.centroid_lat, b.centroid_lng, b.id
FROM buildings b
WHERE b.name IN ('Nhà ăn A1-5', 'Căng tin B5-9')
  AND b.centroid_lat IS NOT NULL;

-- Also add Căn tin C7 linked to building C7
INSERT INTO campus_nodes (name, node_type, latitude, longitude, building_id)
SELECT 'Căn tin C7', 'CANTEEN', b.centroid_lat, b.centroid_lng, b.id
FROM buildings b
WHERE b.name = 'C7'
  AND b.centroid_lat IS NOT NULL;

-- 8. Seed infrastructure nodes: Intersections
INSERT INTO campus_nodes (name, node_type, latitude, longitude) VALUES
    ('Ngã tư C1-C2', 'INTERSECTION', 21.0068, 105.8435),
    ('Ngã tư Thư viện - D3', 'INTERSECTION', 21.0045, 105.8445),
    ('Ngã tư B6 - KTX', 'INTERSECTION', 21.0060, 105.8465),
    ('Ngã tư Cổng Parabol', 'INTERSECTION', 21.0044, 105.8425),
    ('Ngã tư Khu A', 'INTERSECTION', 21.0030, 105.8468);

-- Helper function for Haversine distance in meters
CREATE OR REPLACE FUNCTION haversine_meters(lat1 DOUBLE PRECISION, lng1 DOUBLE PRECISION, lat2 DOUBLE PRECISION, lng2 DOUBLE PRECISION)
RETURNS DOUBLE PRECISION AS $$
BEGIN
    RETURN 6371000 * 2 * ASIN(SQRT(
        POWER(SIN(RADIANS(lat2 - lat1) / 2), 2) +
        COS(RADIANS(lat1)) * COS(RADIANS(lat2)) *
        POWER(SIN(RADIANS(lng2 - lng1) / 2), 2)
    ));
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- 9. Seed edges: Gate -> nearest Intersection
INSERT INTO campus_edges (from_node_id, to_node_id, distance_meters)
SELECT a.id, b.id, haversine_meters(a.latitude, a.longitude, b.latitude, b.longitude)
FROM campus_nodes a, campus_nodes b
WHERE (a.name = 'Cổng Đại Cồ Việt' AND b.name = 'Ngã tư C1-C2')
   OR (a.name = 'Cổng Giải Phóng' AND b.name = 'Ngã tư Cổng Parabol')
   OR (a.name = 'Cổng Trần Đại Nghĩa' AND b.name = 'Ngã tư Khu A');

-- 10. Seed edges: Intersection <-> Intersection
INSERT INTO campus_edges (from_node_id, to_node_id, distance_meters)
SELECT a.id, b.id, haversine_meters(a.latitude, a.longitude, b.latitude, b.longitude)
FROM campus_nodes a, campus_nodes b
WHERE (a.name = 'Ngã tư C1-C2' AND b.name = 'Ngã tư Thư viện - D3')
   OR (a.name = 'Ngã tư C1-C2' AND b.name = 'Ngã tư Cổng Parabol')
   OR (a.name = 'Ngã tư Thư viện - D3' AND b.name = 'Ngã tư B6 - KTX')
   OR (a.name = 'Ngã tư Thư viện - D3' AND b.name = 'Ngã tư Cổng Parabol')
   OR (a.name = 'Ngã tư Thư viện - D3' AND b.name = 'Ngã tư Khu A')
   OR (a.name = 'Ngã tư B6 - KTX' AND b.name = 'Ngã tư Khu A');

-- 11. Seed edges: Parking -> nearest Intersection
INSERT INTO campus_edges (from_node_id, to_node_id, distance_meters)
SELECT a.id, b.id, haversine_meters(a.latitude, a.longitude, b.latitude, b.longitude)
FROM campus_nodes a, campus_nodes b
WHERE (a.name = 'Nhà xe C' AND b.name = 'Ngã tư C1-C2')
   OR (a.name = 'Nhà xe D' AND b.name = 'Ngã tư Cổng Parabol')
   OR (a.name = 'Nhà xe KTX' AND b.name = 'Ngã tư B6 - KTX');

-- 12. Seed edges: Canteen -> nearest Intersection
INSERT INTO campus_edges (from_node_id, to_node_id, distance_meters)
SELECT a.id, b.id, haversine_meters(a.latitude, a.longitude, b.latitude, b.longitude)
FROM campus_nodes a, campus_nodes b
WHERE (a.name = 'Nhà ăn A1-5' AND b.name = 'Ngã tư Thư viện - D3')
   OR (a.name = 'Nhà ăn A1-5' AND b.name = 'Ngã tư Khu A')
   OR (a.name = 'Căn tin C7' AND b.name = 'Ngã tư C1-C2')
   OR (a.name = 'Căn tin C7' AND b.name = 'Ngã tư Thư viện - D3')
   OR (a.name = 'Căng tin B5-9' AND b.name = 'Ngã tư B6 - KTX');
