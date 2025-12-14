package com.soukscan.vendorms.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * Utility class for creating and manipulating geographic points.
 */
public class GeometryUtil {

    /**
     * SRID 4326 = WGS 84 (standard GPS coordinate system).
     */
    public static final int WGS84_SRID = 4326;

    /**
     * GeometryFactory configured for WGS 84.
     */
    private static final GeometryFactory geometryFactory =
        new GeometryFactory(new PrecisionModel(), WGS84_SRID);

    /**
     * Creates a Point from latitude and longitude.
     *
     * @param latitude Latitude (-90 to 90)
     * @param longitude Longitude (-180 to 180)
     * @return Point with SRID 4326
     */
    public static Point createPoint(double latitude, double longitude) {
        // Note: In PostGIS, Points are stored as (longitude, latitude) = (X, Y)
        // But we commonly think of coordinates as (latitude, longitude)
        Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        point.setSRID(WGS84_SRID);
        return point;
    }

    /**
     * Extracts latitude from a Point.
     *
     * @param point The geographic point
     * @return Latitude (Y coordinate)
     */
    public static Double getLatitude(Point point) {
        return point != null ? point.getY() : null;
    }

    /**
     * Extracts longitude from a Point.
     *
     * @param point The geographic point
     * @return Longitude (X coordinate)
     */
    public static Double getLongitude(Point point) {
        return point != null ? point.getX() : null;
    }

    /**
     * Validates latitude value.
     *
     * @param latitude Latitude to validate
     * @return true if valid
     */
    public static boolean isValidLatitude(double latitude) {
        return latitude >= -90.0 && latitude <= 90.0;
    }

    /**
     * Validates longitude value.
     *
     * @param longitude Longitude to validate
     * @return true if valid
     */
    public static boolean isValidLongitude(double longitude) {
        return longitude >= -180.0 && longitude <= 180.0;
    }

    /**
     * Validates both latitude and longitude.
     *
     * @param latitude Latitude to validate
     * @param longitude Longitude to validate
     * @throws IllegalArgumentException if coordinates are invalid
     */
    public static void validateCoordinates(double latitude, double longitude) {
        if (!isValidLatitude(latitude)) {
            throw new IllegalArgumentException(
                "Latitude invalide: " + latitude + ". Doit être entre -90 et 90."
            );
        }
        if (!isValidLongitude(longitude)) {
            throw new IllegalArgumentException(
                "Longitude invalide: " + longitude + ". Doit être entre -180 et 180."
            );
        }
    }

    /**
     * Calculates approximate distance between two points using Haversine formula.
     * This is a simplified version; PostGIS ST_Distance is more accurate.
     *
     * @param lat1 Latitude of point 1
     * @param lon1 Longitude of point 1
     * @param lat2 Latitude of point 2
     * @param lon2 Longitude of point 2
     * @return Distance in meters
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371000; // meters

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}

