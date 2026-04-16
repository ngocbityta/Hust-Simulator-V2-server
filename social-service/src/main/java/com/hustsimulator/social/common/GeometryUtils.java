package com.hustsimulator.social.common;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * Utility class for spatial operations using JTS Topology Suite.
 */
public class GeometryUtils {

    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    /**
     * Creates a JTS Point from latitude and longitude.
     */
    public static Point createPoint(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }
        return geometryFactory.createPoint(new Coordinate(longitude, latitude));
    }
}
