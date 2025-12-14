package com.soukscan.vendorms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

/**
 * Entity representing a vendor location with geographical coordinates.
 * Maps to the 'locations' table in the database.
 */
@Entity
@Table(name = "locations",
       indexes = {
           @Index(name = "idx_location_vendor", columnList = "vendor_id"),
           @Index(name = "idx_location_name", columnList = "name")
           // Note: Spatial index on 'geom' column should be created via SQL migration:
           // CREATE INDEX idx_locations_geom ON locations USING GIST(geom);
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    /**
     * Unique identifier for the vendor (typically UUID from the vendor service).
     */
    @Id
    @Column(name = "vendor_id", length = 50)
    private String vendorId;

    /**
     * Name of the vendor/location.
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Full address of the location.
     */
    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    /**
     * Geographical point (latitude, longitude).
     * Uses PostGIS GEOMETRY(Point, 4326) - WGS 84 spatial reference system.
     * SRID 4326 is the standard GPS coordinate system.
     */
    @Column(name = "geom", columnDefinition = "geometry(Point,4326)")
    private Point geom;

    /**
     * Latitude extracted from the Point geometry (helper for easy access).
     * Not stored in DB, calculated on-the-fly.
     */
    @Transient
    public Double getLatitude() {
        return geom != null ? geom.getY() : null;
    }

    /**
     * Longitude extracted from the Point geometry (helper for easy access).
     * Not stored in DB, calculated on-the-fly.
     */
    @Transient
    public Double getLongitude() {
        return geom != null ? geom.getX() : null;
    }
}

