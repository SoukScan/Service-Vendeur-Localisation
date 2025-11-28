package com.soukscan.vendorms.controller;

import com.soukscan.vendorms.dto.LocationRequestDTO;
import com.soukscan.vendorms.dto.LocationResponseDTO;
import com.soukscan.vendorms.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing vendor locations with geographic data.
 */
@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    /**
     * Create a new vendor location.
     * POST /api/locations
     */
    @PostMapping
    public ResponseEntity<LocationResponseDTO> createLocation(@Valid @RequestBody LocationRequestDTO request) {
        LocationResponseDTO response = locationService.createLocation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all vendor locations.
     * GET /api/locations
     */
    @GetMapping
    public ResponseEntity<List<LocationResponseDTO>> getAllLocations() {
        List<LocationResponseDTO> locations = locationService.getAllLocations();
        return ResponseEntity.ok(locations);
    }

    /**
     * Get a specific vendor location by ID.
     * GET /api/locations/{vendorId}
     */
    @GetMapping("/{vendorId}")
    public ResponseEntity<LocationResponseDTO> getLocationById(@PathVariable String vendorId) {
        LocationResponseDTO location = locationService.getLocationById(vendorId);
        return ResponseEntity.ok(location);
    }

    /**
     * Find vendor locations near a specific coordinate within a radius.
     * GET /api/locations/nearby?latitude=33.5731&longitude=-7.5898&radius=5
     *
     * @param latitude Latitude of the center point
     * @param longitude Longitude of the center point
     * @param radius Radius in kilometers (default: 5 km)
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<LocationResponseDTO>> findLocationsNearby(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5.0") Double radius) {
        List<LocationResponseDTO> locations = locationService.findLocationsNearby(latitude, longitude, radius);
        return ResponseEntity.ok(locations);
    }

    /**
     * Find the nearest N vendor locations to a specific coordinate.
     * GET /api/locations/nearest?latitude=33.5731&longitude=-7.5898&limit=10
     *
     * @param latitude Latitude of the reference point
     * @param longitude Longitude of the reference point
     * @param limit Number of nearest locations to return (default: 10)
     */
    @GetMapping("/nearest")
    public ResponseEntity<List<LocationResponseDTO>> findNearestLocations(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10") Integer limit) {
        List<LocationResponseDTO> locations = locationService.findNearestLocations(latitude, longitude, limit);
        return ResponseEntity.ok(locations);
    }

    /**
     * Update a vendor location.
     * PUT /api/locations/{vendorId}
     */
    @PutMapping("/{vendorId}")
    public ResponseEntity<LocationResponseDTO> updateLocation(
            @PathVariable String vendorId,
            @Valid @RequestBody LocationRequestDTO request) {
        LocationResponseDTO location = locationService.updateLocation(vendorId, request);
        return ResponseEntity.ok(location);
    }

    /**
     * Delete a vendor location.
     * DELETE /api/locations/{vendorId}
     */
    @DeleteMapping("/{vendorId}")
    public ResponseEntity<Void> deleteLocation(@PathVariable String vendorId) {
        locationService.deleteLocation(vendorId);
        return ResponseEntity.noContent().build();
    }
}

