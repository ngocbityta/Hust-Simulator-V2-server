package com.hustsimulator.context.common;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.triangulate.polygon.PolygonTriangulator;

import java.util.ArrayList;
import java.util.List;

public class GeometryUtils {

    private static final GeometryFactory geometryFactory = new GeometryFactory();

    /**
     * Converts a list of coordinates (x, y) into a JTS Polygon.
     * Ensure the start and end coordinates are the same to close the linear ring.
     */
    public static Polygon createPolygon(List<double[]> points) {
        if (points == null || points.size() < 3) {
            throw new IllegalArgumentException("A polygon requires at least 3 points");
        }
        
        // JTS requires the linear ring to be closed (first and last point are identical)
        boolean isClosed = false;
        if (points.get(0)[0] == points.get(points.size() - 1)[0] &&
            points.get(0)[1] == points.get(points.size() - 1)[1]) {
            isClosed = true;
        }

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
     * Splits a Polygon into multiple Convex Polygons (Triangles) using Ear-Clipping.
     * Triangles are inherently convex.
     */
    public static List<Polygon> splitIntoConvexPolygons(Polygon polygon) {
        List<Polygon> result = new ArrayList<>();
        // PolygonTriangulator triangulates the interior of a polygon
        org.locationtech.jts.geom.Geometry geom = PolygonTriangulator.triangulate(polygon);
        
        for (int i = 0; i < geom.getNumGeometries(); i++) {
            org.locationtech.jts.geom.Geometry childGeom = geom.getGeometryN(i);
            if (childGeom instanceof Polygon) {
                result.add((Polygon) childGeom);
            }
        }
        return result;
    }

    /**
     * Checks if a point (x,y) resides inside any of the given polygons.
     */
    public static boolean isPointInsideAnyPolygon(double x, double y, List<Polygon> polygons) {
        Point pt = geometryFactory.createPoint(new Coordinate(x, y));
        for (Polygon poly : polygons) {
            if (poly.covers(pt)) {
                return true;
            }
        }
        return false;
    }
}
