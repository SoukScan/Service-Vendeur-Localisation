package com.soukscan.vendorms.service;

import com.soukscan.vendorms.dto.PriceAvgResponseDTO;
import com.soukscan.vendorms.entity.PriceAvg;
import com.soukscan.vendorms.exception.ResourceNotFoundException;
import com.soukscan.vendorms.repository.PriceAvgRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing average prices.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PriceAvgService {

    private final PriceAvgRepository priceAvgRepository;

    @Transactional(readOnly = true)
    public List<PriceAvgResponseDTO> getAllAveragePrices() {
        log.info("Récupération de tous les prix moyens");
        return priceAvgRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PriceAvgResponseDTO getAveragePriceByProduct(String productId) {
        log.info("Récupération du prix moyen pour produit: {}", productId);
        PriceAvg priceAvg = priceAvgRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Prix moyen non trouvé pour produit ID: " + productId
                ));
        return mapToResponseDTO(priceAvg);
    }

    @Transactional(readOnly = true)
    public List<PriceAvgResponseDTO> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.info("Recherche des produits avec prix moyen entre {} et {}", minPrice, maxPrice);
        return priceAvgRepository.findByPriceRange(minPrice, maxPrice).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PriceAvgResponseDTO> getCheapestProducts(Integer limit) {
        log.info("Récupération des {} produits les moins chers", limit);
        return priceAvgRepository.findCheapestProducts(limit).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private PriceAvgResponseDTO mapToResponseDTO(PriceAvg priceAvg) {
        return new PriceAvgResponseDTO(
            priceAvg.getProductId(),
            priceAvg.getAveragePrice(),
            priceAvg.getReportCount(),
            priceAvg.getLastUpdated()
        );
    }
}

