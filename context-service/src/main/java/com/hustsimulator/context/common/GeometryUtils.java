package com.hustsimulator.context.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import java.util.List;

/**
 * Utility class for spatial operations using JTS Topology Suite.
 */
public class GeometryUtils {

    private static final GeometryFactory geometryFactory = new GeometryFactory();

    /**
     * Converts a list of coordinates (x, y) into a JTS Polygon.
     * Ensures the polygon is closed as required by JTS.
     */
    public static Polygon createPolygon(List<double[]> points) {
        if (points == null || points.size() < 3) {
            throw new IllegalArgumentException("A polygon requires at least 3 points");
        }

        // Check if the polygon is closed (first and last point are the same)
        boolean isClosed = points.get(0).length == 2 && 
                          points.get(points.size() - 1).length == 2 &&
                          points.get(0)[0] == points.get(points.size() - 1)[0] && 
                          points.get(0)[1] == points.get(points.size() - 1)[1];

        Coordinate[] coordinates = new Coordinate[isClosed ? points.size() : points.size() + 1];
        for (int i = 0; i < points.size(); i++) {
            coordinates[i] = new Coordinate(points.get(i)[0], points.get(i)[1]);
        }

        if (!isClosed) {
            coordinates[coordinates.length - 1] = new Coordinate(points.get(0)[0], points.get(0)[1]);
        }

        return geometryFactory.createPolygon(coordinates);
    }

    /**
     * Checks if a point (x, y) is inside a polygon defined by a list of points.
     */
    public static boolean isPointInPolygon(double x, double y, List<double[]> polygonPoints) {
        if (polygonPoints == null || polygonPoints.isEmpty()) {
            return false;
        }
        
        Polygon polygon = createPolygon(polygonPoints);
        Point point = geometryFactory.createPoint(new Coordinate(x, y));
        
        return polygon.covers(point);
    }
    
    /**
     * Checks if a point (x, y) is inside a pre-built JTS Polygon.
     */
    public static boolean isPointInPolygon(double x, double y, Polygon polygon) {
        if (polygon == null) {
            return false;
        }
        Point point = geometryFactory.createPoint(new Coordinate(x, y));
        return polygon.covers(point);
    }

    /**
     * Centralized JSON coordinate deserialization.
     */
    public static List<double[]> deserializePoints(String json, ObjectMapper objectMapper) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<double[]>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize coordinates: " + e.getMessage(), e);
        }
    }
}
