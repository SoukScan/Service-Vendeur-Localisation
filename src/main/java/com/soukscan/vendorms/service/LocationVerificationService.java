package com.soukscan.vendorms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Enhanced Location Verification Service - Ensures users are physically present at reporting locations
 */
@Service
public class LocationVerificationService {

    private static final Logger log = LoggerFactory.getLogger(LocationVerificationService.class);
    
    // Configuration constants
    private static final double MAX_REPORTING_DISTANCE_METERS = 50.0;
    private static final double MIN_ACCURACY_THRESHOLD_METERS = 100.0; // GPS accuracy threshold
    private static final int EARTH_RADIUS_METERS = 6371000;

    /**
     * Verify if user is close enough to report prices at a location
     */
    public LocationVerificationResult verifyUserLocation(double userLat, double userLon, 
                                                       double shopLat, double shopLon) {
        return verifyUserLocation(userLat, userLon, shopLat, shopLon, null);
    }

    /**
     * Verify if user is close enough to report prices at a location with accuracy consideration
     */
    public LocationVerificationResult verifyUserLocation(double userLat, double userLon, 
                                                       double shopLat, double shopLon, 
                                                       Double gpsAccuracy) {
        // Calculate precise distance using Haversine formula
        double distance = calculateHaversineDistance(userLat, userLon, shopLat, shopLon);
        
        log.info("Location verification - User: ({}, {}), Shop: ({}, {}), Distance: {:.2f}m", 
                userLat, userLon, shopLat, shopLon, distance);

        // Basic distance check
        if (distance > MAX_REPORTING_DISTANCE_METERS) {
            return LocationVerificationResult.failed(
                String.format("You are %.1fm away from the shop. You must be within %.0fm to report prices.", 
                              distance, MAX_REPORTING_DISTANCE_METERS),
                distance
            );
        }

        // GPS accuracy check if provided
        if (gpsAccuracy != null && gpsAccuracy > MIN_ACCURACY_THRESHOLD_METERS) {
            return LocationVerificationResult.failed(
                String.format("GPS accuracy is too low (±%.0fm). Please ensure better GPS signal for accurate reporting.", 
                              gpsAccuracy),
                distance
            );
        }

        // Additional verification checks
        if (!isValidCoordinate(userLat, userLon) || !isValidCoordinate(shopLat, shopLon)) {
            return LocationVerificationResult.failed("Invalid coordinates provided", distance);
        }

        // Verification passed
        String message = distance <= 10 ? 
            "Location verified - You are at the shop" :
            String.format("Location verified - You are %.1fm from the shop", distance);
            
        return LocationVerificationResult.success(message, distance);
    }

    /**
     * Calculate distance between two points using Haversine formula
     * More accurate for short distances than simple coordinate math
     */
    public double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        // Convert latitude and longitude from degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // Haversine formula
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                  Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                  Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS * c;
    }

    /**
     * Validate coordinates are within valid ranges
     */
    private boolean isValidCoordinate(double lat, double lon) {
        return lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180;
    }

    /**
     * Check if location is in a suspicious area (like middle of ocean, etc.)
     * Could be enhanced with more sophisticated geo-validation
     */
    public boolean isSuspiciousLocation(double lat, double lon) {
        // Basic check for obviously invalid locations
        // This could be enhanced with geofencing against known land areas
        
        // Check if coordinates are exactly 0,0 (null island)
        if (lat == 0.0 && lon == 0.0) {
            log.warn("Suspicious location detected: 0,0 coordinates");
            return true;
        }
        
        // Add more sophisticated checks as needed
        return false;
    }

    /**
     * Get maximum allowed reporting distance
     */
    public double getMaxReportingDistance() {
        return MAX_REPORTING_DISTANCE_METERS;
    }

    /**
     * Result class for location verification
     */
    public static class LocationVerificationResult {
        private final boolean success;
        private final String message;
        private final double distance;

        private LocationVerificationResult(boolean success, String message, double distance) {
            this.success = success;
            this.message = message;
            this.distance = distance;
        }

        public static LocationVerificationResult success(String message, double distance) {
            return new LocationVerificationResult(true, message, distance);
        }

        public static LocationVerificationResult failed(String message, double distance) {
            return new LocationVerificationResult(false, message, distance);
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public double getDistance() { return distance; }
        
        public boolean isFailed() { return !success; }
    }
}