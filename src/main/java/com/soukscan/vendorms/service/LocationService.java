package com.soukscan.vendorms.service;

import com.soukscan.vendorms.dto.LocationRequestDTO;
import com.soukscan.vendorms.dto.LocationResponseDTO;
import com.soukscan.vendorms.entity.Location;
import com.soukscan.vendorms.exception.DuplicateResourceException;
import com.soukscan.vendorms.exception.ResourceNotFoundException;
import com.soukscan.vendorms.repository.LocationRepository;
import com.soukscan.vendorms.util.GeometryUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing vendor locations with geographic data.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    private final LocationRepository locationRepository;

    @Transactional
    public LocationResponseDTO createLocation(LocationRequestDTO request) {
        log.info("Création d'une nouvelle location pour vendor: {}", request.getVendorId());

        // Vérifier si le vendor existe déjà
        if (locationRepository.existsById(request.getVendorId())) {
            throw new DuplicateResourceException(
                "Une location existe déjà pour le vendor ID: " + request.getVendorId()
            );
        }

        // Valider les coordonnées
        GeometryUtil.validateCoordinates(request.getLatitude(), request.getLongitude());

        // Créer la location
        Location location = new Location();
        location.setVendorId(request.getVendorId());
        location.setName(request.getName());
        location.setAddress(request.getAddress());
        location.setGeom(GeometryUtil.createPoint(request.getLatitude(), request.getLongitude()));

        Location saved = locationRepository.save(location);
        log.info("Location créée avec succès pour vendor: {}", saved.getVendorId());

        return mapToResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<LocationResponseDTO> getAllLocations() {
        log.info("Récupération de toutes les locations");
        return locationRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LocationResponseDTO getLocationById(String vendorId) {
        log.info("Récupération de la location pour vendor: {}", vendorId);
        Location location = locationRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Location non trouvée pour vendor ID: " + vendorId
                ));
        return mapToResponseDTO(location);
    }

    @Transactional(readOnly = true)
    public List<LocationResponseDTO> findLocationsNearby(Double latitude, Double longitude, Double radiusKm) {
        log.info("Recherche de locations près de ({}, {}) dans un rayon de {} km",
                 latitude, longitude, radiusKm);

        GeometryUtil.validateCoordinates(latitude, longitude);

        double radiusMeters = radiusKm * 1000; // Convertir km en mètres
        Point centerPoint = GeometryUtil.createPoint(latitude, longitude);

        List<Location> locations = locationRepository.findLocationsWithinDistance(centerPoint, radiusMeters);

        return locations.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LocationResponseDTO> findNearestLocations(Double latitude, Double longitude, Integer limit) {
        log.info("Recherche des {} locations les plus proches de ({}, {})", limit, latitude, longitude);

        GeometryUtil.validateCoordinates(latitude, longitude);

        List<Location> locations = locationRepository.findNearestLocations(latitude, longitude, limit);

        return locations.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public LocationResponseDTO updateLocation(String vendorId, LocationRequestDTO request) {
        log.info("Mise à jour de la location pour vendor: {}", vendorId);

        Location location = locationRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Location non trouvée pour vendor ID: " + vendorId
                ));

        GeometryUtil.validateCoordinates(request.getLatitude(), request.getLongitude());

        location.setName(request.getName());
        location.setAddress(request.getAddress());
        location.setGeom(GeometryUtil.createPoint(request.getLatitude(), request.getLongitude()));

        Location updated = locationRepository.save(location);
        log.info("Location mise à jour avec succès pour vendor: {}", vendorId);

        return mapToResponseDTO(updated);
    }

    @Transactional
    public void deleteLocation(String vendorId) {
        log.info("Suppression de la location pour vendor: {}", vendorId);

        if (!locationRepository.existsById(vendorId)) {
            throw new ResourceNotFoundException("Location non trouvée pour vendor ID: " + vendorId);
        }

        locationRepository.deleteById(vendorId);
        log.info("Location supprimée avec succès pour vendor: {}", vendorId);
    }

    private LocationResponseDTO mapToResponseDTO(Location location) {
        return new LocationResponseDTO(
            location.getVendorId(),
            location.getName(),
            location.getAddress(),
            location.getLatitude(),
            location.getLongitude()
        );
    }
}

