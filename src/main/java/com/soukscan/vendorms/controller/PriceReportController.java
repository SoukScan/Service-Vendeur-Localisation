package com.soukscan.vendorms.controller;

import com.soukscan.vendorms.dto.PriceReportRequestDTO;
import com.soukscan.vendorms.dto.PriceReportResponseDTO;
import com.soukscan.vendorms.service.PriceReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing price reports.
 */
@RestController
@RequestMapping("/api/price-reports")
@RequiredArgsConstructor
public class PriceReportController {

    private final PriceReportService priceReportService;

    /**
     * Create a new price report.
     * POST /api/price-reports
     */
    @PostMapping
    public ResponseEntity<PriceReportResponseDTO> createPriceReport(
            @Valid @RequestBody PriceReportRequestDTO request) {
        PriceReportResponseDTO response = priceReportService.createPriceReport(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all price reports.
     * GET /api/price-reports
     */
    @GetMapping
    public ResponseEntity<List<PriceReportResponseDTO>> getAllPriceReports() {
        List<PriceReportResponseDTO> reports = priceReportService.getAllPriceReports();
        return ResponseEntity.ok(reports);
    }

    /**
     * Get price reports for a specific product.
     * GET /api/price-reports/product/{productId}
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<PriceReportResponseDTO>> getPriceReportsByProduct(
            @PathVariable String productId) {
        List<PriceReportResponseDTO> reports = priceReportService.getPriceReportsByProduct(productId);
        return ResponseEntity.ok(reports);
    }

    /**
     * Get price reports from a specific vendor.
     * GET /api/price-reports/vendor/{vendorId}
     */
    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<PriceReportResponseDTO>> getPriceReportsByVendor(
            @PathVariable String vendorId) {
        List<PriceReportResponseDTO> reports = priceReportService.getPriceReportsByVendor(vendorId);
        return ResponseEntity.ok(reports);
    }

    /**
     * Get recent price reports for a product.
     * GET /api/price-reports/product/{productId}/recent?days=7
     *
     * @param productId Product ID
     * @param days Number of days to look back (default: 7)
     */
    @GetMapping("/product/{productId}/recent")
    public ResponseEntity<List<PriceReportResponseDTO>> getRecentPriceReports(
            @PathVariable String productId,
            @RequestParam(defaultValue = "7") Integer days) {
        List<PriceReportResponseDTO> reports = priceReportService.getRecentPriceReports(productId, days);
        return ResponseEntity.ok(reports);
    }

    /**
     * Get price reports for a product near a specific location.
     * GET /api/price-reports/product/{productId}/nearby?latitude=33.5731&longitude=-7.5898&radius=10
     *
     * @param productId Product ID
     * @param latitude Latitude of the reference point
     * @param longitude Longitude of the reference point
     * @param radius Radius in kilometers (default: 10 km)
     */
    @GetMapping("/product/{productId}/nearby")
    public ResponseEntity<List<PriceReportResponseDTO>> getPriceReportsNearLocation(
            @PathVariable String productId,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "10.0") Double radius) {
        List<PriceReportResponseDTO> reports = priceReportService.getPriceReportsNearLocation(
            productId, latitude, longitude, radius
        );
        return ResponseEntity.ok(reports);
    }

    /**
     * Delete a price report.
     * DELETE /api/price-reports/{reportId}
     */
    @DeleteMapping("/{reportId}")
    public ResponseEntity<Void> deletePriceReport(@PathVariable Long reportId) {
        priceReportService.deletePriceReport(reportId);
        return ResponseEntity.noContent().build();
    }
}

