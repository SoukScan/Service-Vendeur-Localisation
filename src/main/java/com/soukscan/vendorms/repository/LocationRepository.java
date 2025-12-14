package com.soukscan.vendorms.repository;

import com.soukscan.vendorms.entity.Location;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Location entities.
 * Provides spatial queries using PostGIS functions.
 */
@Repository
public interface LocationRepository extends JpaRepository<Location, String> {

    /**
     * Find a location by vendor name.
     */
    Optional<Location> findByName(String name);

    /**
     * Find locations within a certain distance (in meters) from a given point.
     * Uses PostGIS ST_DWithin function with geography for accurate distance calculation.
     *
     * @param point The reference point (center)
     * @param distanceMeters Distance in meters
     * @return List of locations within the specified distance
     */
    @Query(value = """
        SELECT * FROM locations 
        WHERE ST_DWithin(geom::geography, :point::geography, :distanceMeters)
        ORDER BY ST_Distance(geom::geography, :point::geography)
        """, nativeQuery = true)
    List<Location> findLocationsWithinDistance(
        @Param("point") Point point,
        @Param("distanceMeters") double distanceMeters
    );

    /**
     * Find locations within a certain distance and return with distance.
     * Returns additional column with calculated distance.
     *
     * @param latitude Latitude of the reference point
     * @param longitude Longitude of the reference point
     * @param distanceMeters Distance in meters
     * @return List of locations with distance
     */
    @Query(value = """
        SELECT 
            vendor_id,
            name,
            address,
            geom,
            ST_Distance(geom::geography, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography) as distance_meters
        FROM locations 
        WHERE ST_DWithin(
            geom::geography, 
            ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography, 
            :distanceMeters
        )
        ORDER BY distance_meters
        """, nativeQuery = true)
    List<Object[]> findLocationsWithinDistanceWithMetrics(
        @Param("latitude") double latitude,
        @Param("longitude") double longitude,
        @Param("distanceMeters") double distanceMeters
    );

    /**
     * Find the nearest N locations to a given point.
     *
     * @param latitude Latitude of the reference point
     * @param longitude Longitude of the reference point
     * @param limit Number of nearest locations to return
     * @return List of nearest locations
     */
    @Query(value = """
        SELECT * FROM locations 
        ORDER BY geom::geography <-> ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography
        LIMIT :limit
        """, nativeQuery = true)
    List<Location> findNearestLocations(
        @Param("latitude") double latitude,
        @Param("longitude") double longitude,
        @Param("limit") int limit
    );
}

