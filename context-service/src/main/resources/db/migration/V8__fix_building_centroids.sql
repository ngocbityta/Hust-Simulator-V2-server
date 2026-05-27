-- Use PostGIS to calculate true geometric centroid of buildings
-- The coordinates column stores a JSON array of coordinates representing a polygon ring.
-- To make it a valid GeoJSON Polygon, we wrap it inside an outer array.

CREATE EXTENSION IF NOT EXISTS postgis;

UPDATE context.buildings
SET centroid_lat = ST_Y(ST_Centroid(ST_GeomFromGeoJSON('{"type":"Polygon", "coordinates":[' || coordinates::text || ']}'))),
    centroid_lng = ST_X(ST_Centroid(ST_GeomFromGeoJSON('{"type":"Polygon", "coordinates":[' || coordinates::text || ']}')))
WHERE coordinates IS NOT NULL;
