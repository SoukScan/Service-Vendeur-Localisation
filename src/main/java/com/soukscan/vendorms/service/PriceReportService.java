package com.soukscan.vendorms.service;

import com.soukscan.vendorms.dto.PriceReportRequestDTO;
import com.soukscan.vendorms.dto.PriceReportResponseDTO;
import com.soukscan.vendorms.entity.Location;
import com.soukscan.vendorms.entity.PriceAvg;
import com.soukscan.vendorms.entity.PriceReport;
import com.soukscan.vendorms.exception.ResourceNotFoundException;
import com.soukscan.vendorms.repository.LocationRepository;
import com.soukscan.vendorms.repository.PriceAvgRepository;
import com.soukscan.vendorms.repository.PriceReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing price reports.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PriceReportService {

    private final PriceReportRepository priceReportRepository;
    private final LocationRepository locationRepository;
    private final PriceAvgRepository priceAvgRepository;

    @Transactional
    public PriceReportResponseDTO createPriceReport(PriceReportRequestDTO request) {
        log.info("Création d'un rapport de prix pour produit {} par vendor {}",
                 request.getProductId(), request.getVendorId());

        // Vérifier que le vendor existe
        Location location = locationRepository.findById(request.getVendorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Vendor non trouvé avec ID: " + request.getVendorId()
                ));

        // Créer le rapport de prix
        PriceReport report = new PriceReport(
            request.getProductId(),
            request.getVendorId(),
            request.getPrice()
        );

        PriceReport saved = priceReportRepository.save(report);
        log.info("Rapport de prix créé avec ID: {}", saved.getReportId());

        // Mettre à jour la moyenne des prix
        updatePriceAverage(request.getProductId());

        return mapToResponseDTO(saved, location);
    }

    @Transactional(readOnly = true)
    public List<PriceReportResponseDTO> getAllPriceReports() {
        log.info("Récupération de tous les rapports de prix");
        return priceReportRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PriceReportResponseDTO> getPriceReportsByProduct(String productId) {
        log.info("Récupération des rapports de prix pour produit: {}", productId);
        return priceReportRepository.findByProductId(productId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PriceReportResponseDTO> getPriceReportsByVendor(String vendorId) {
        log.info("Récupération des rapports de prix pour vendor: {}", vendorId);
        return priceReportRepository.findByVendorId(vendorId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PriceReportResponseDTO> getRecentPriceReports(String productId, Integer days) {
        log.info("Récupération des rapports de prix récents ({} jours) pour produit: {}", days, productId);
        OffsetDateTime since = OffsetDateTime.now().minusDays(days);
        return priceReportRepository.findRecentReportsByProduct(productId, since).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PriceReportResponseDTO> getPriceReportsNearLocation(
            String productId, Double latitude, Double longitude, Double radiusKm) {
        log.info("Recherche des prix pour produit {} près de ({}, {}) dans {} km",
                 productId, latitude, longitude, radiusKm);

        double radiusMeters = radiusKm * 1000;
        return priceReportRepository.findPriceReportsNearLocation(
            productId, latitude, longitude, radiusMeters
        ).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletePriceReport(Long reportId) {
        log.info("Suppression du rapport de prix: {}", reportId);

        PriceReport report = priceReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Rapport de prix non trouvé avec ID: " + reportId
                ));

        String productId = report.getProductId();
        priceReportRepository.deleteById(reportId);

        // Mettre à jour la moyenne après suppression
        updatePriceAverage(productId);

        log.info("Rapport de prix supprimé: {}", reportId);
    }

    /**
     * Met à jour le prix moyen pour un produit.
     */
    private void updatePriceAverage(String productId) {
        log.info("Mise à jour du prix moyen pour produit: {}", productId);

        BigDecimal avgPrice = priceReportRepository.calculateAveragePrice(productId);
        Integer count = priceReportRepository.countReportsByProduct(productId);

        if (avgPrice != null && count > 0) {
            PriceAvg priceAvg = priceAvgRepository.findByProductId(productId)
                    .orElse(new PriceAvg());

            priceAvg.setProductId(productId);
            priceAvg.setAveragePrice(avgPrice);
            priceAvg.setReportCount(count);
            priceAvg.setLastUpdated(OffsetDateTime.now());

            priceAvgRepository.save(priceAvg);
            log.info("Prix moyen mis à jour: {} (basé sur {} rapports)", avgPrice, count);
        } else if (count == 0) {
            // Si plus aucun rapport, supprimer la moyenne
            priceAvgRepository.deleteById(productId);
            log.info("Prix moyen supprimé (aucun rapport restant)");
        }
    }

    private PriceReportResponseDTO mapToResponseDTO(PriceReport report) {
        PriceReportResponseDTO dto = new PriceReportResponseDTO();
        dto.setReportId(report.getReportId());
        dto.setProductId(report.getProductId());
        dto.setVendorId(report.getVendorId());
        dto.setPrice(report.getPrice());
        dto.setReportedAt(report.getReportedAt());

        // Charger les informations du vendor si disponible
        locationRepository.findById(report.getVendorId()).ifPresent(location -> {
            dto.setVendorName(location.getName());
            dto.setLatitude(location.getLatitude());
            dto.setLongitude(location.getLongitude());
        });

        return dto;
    }

    private PriceReportResponseDTO mapToResponseDTO(PriceReport report, Location location) {
        PriceReportResponseDTO dto = mapToResponseDTO(report);
        if (location != null) {
            dto.setVendorName(location.getName());
            dto.setLatitude(location.getLatitude());
            dto.setLongitude(location.getLongitude());
        }
        return dto;
    }
}

