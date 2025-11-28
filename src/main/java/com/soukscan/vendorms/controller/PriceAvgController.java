package com.soukscan.vendorms.controller;

import com.soukscan.vendorms.dto.PriceAvgResponseDTO;
import com.soukscan.vendorms.service.PriceAvgService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST Controller for managing average prices.
 */
@RestController
@RequestMapping("/api/price-averages")
@RequiredArgsConstructor
public class PriceAvgController {

    private final PriceAvgService priceAvgService;

    /**
     * Get all average prices.
     * GET /api/price-averages
     */
    @GetMapping
    public ResponseEntity<List<PriceAvgResponseDTO>> getAllAveragePrices() {
        List<PriceAvgResponseDTO> averages = priceAvgService.getAllAveragePrices();
        return ResponseEntity.ok(averages);
    }

    /**
     * Get average price for a specific product.
     * GET /api/price-averages/{productId}
     */
    @GetMapping("/{productId}")
    public ResponseEntity<PriceAvgResponseDTO> getAveragePriceByProduct(@PathVariable String productId) {
        PriceAvgResponseDTO average = priceAvgService.getAveragePriceByProduct(productId);
        return ResponseEntity.ok(average);
    }

    /**
     * Get products within a price range.
     * GET /api/price-averages/range?min=10.00&max=50.00
     */
    @GetMapping("/range")
    public ResponseEntity<List<PriceAvgResponseDTO>> getProductsByPriceRange(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max) {
        List<PriceAvgResponseDTO> averages = priceAvgService.getProductsByPriceRange(min, max);
        return ResponseEntity.ok(averages);
    }

    /**
     * Get the cheapest products.
     * GET /api/price-averages/cheapest?limit=10
     *
     * @param limit Number of products to return (default: 10)
     */
    @GetMapping("/cheapest")
    public ResponseEntity<List<PriceAvgResponseDTO>> getCheapestProducts(
            @RequestParam(defaultValue = "10") Integer limit) {
        List<PriceAvgResponseDTO> averages = priceAvgService.getCheapestProducts(limit);
        return ResponseEntity.ok(averages);
    }
}

