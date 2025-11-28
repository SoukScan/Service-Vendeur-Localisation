package com.soukscan.vendorms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for location response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationResponseDTO {

    private String vendorId;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;

    /**
     * Distance in meters (optional, populated when searching by location).
     */
    private Double distanceMeters;

    public LocationResponseDTO(String vendorId, String name, String address, Double latitude, Double longitude) {
        this.vendorId = vendorId;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}

