package com.soukscan.vendorms.controller;

import com.soukscan.vendorms.dto.SimpleReportRequestDTO;
import com.soukscan.vendorms.dto.SimpleReportResponseDTO;
import com.soukscan.vendorms.service.SimplePriceReportService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simplified Price Reporting API
 * 
 * This controller provides a streamlined interface for price reporting that only requires:
 * - Location (from map click)
 * - Product ID (pre-selected by user)
 * - Price (entered by user)
 * - Optional shop name
 * 
 * The system automatically handles location validation to ensure users can only report
 * prices when they are physically close to the shop location.
 */
@RestController
@RequestMapping("/api/price-report")
public class SimplePriceReportController {

    private static final Logger log = LoggerFactory.getLogger(SimplePriceReportController.class);
    private final SimplePriceReportService simplePriceReportService;

    @Autowired
    public SimplePriceReportController(SimplePriceReportService simplePriceReportService) {
        this.simplePriceReportService = simplePriceReportService;
    }

    /**
     * Report a product price at a specific location
     * 
     * POST /api/price-report
     * 
     * Request body:
     * {
     *   "productId": 1,
     *   "price": 15.50,
     *   "latitude": 33.5898,
     *   "longitude": -7.6038,
     *   "userId": 123,
     *   "shopName": "Local Market" // optional
     * }
     * 
     * @param request The price report request containing location, product, price, and user info
     * @return Response with shop and product details, or error message
     */
    @PostMapping
    public ResponseEntity<?> reportPrice(@Valid @RequestBody SimpleReportRequestDTO request) {
        try {
            log.info("Received price report request for product {} at location ({}, {})", 
                    request.getProductId(), request.getLatitude(), request.getLongitude());
            
            SimpleReportResponseDTO response = simplePriceReportService.reportPrice(request);
            
            // Return 201 for new shop creation, 200 for updates
            HttpStatus status = response.getIsNewShop() ? HttpStatus.CREATED : HttpStatus.OK;
            
            log.info("Price report successful: {}", response.getMessage());
            return ResponseEntity.status(status).body(response);
            
        } catch (RuntimeException e) {
            log.error("Price report failed: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Unexpected error during price report", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal server error occurred");
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Price Reporting Service");
        return ResponseEntity.ok(response);
    }

    /**
     * Modify an existing price report from the current user
     * 
     * PUT /api/price-report/modify/{reportId}
     * 
     * Request body:
     * {
     *   "price": 16.50,
     *   "userId": 123  // Must match the original reporter
     * }
     */
    @PutMapping("/modify/{reportId}")
    public ResponseEntity<?> modifyPriceReport(
            @PathVariable Long reportId,
            @Valid @RequestBody Map<String, Object> modifyRequest) {
        try {
            Double newPrice = Double.valueOf(modifyRequest.get("price").toString());
            Long userId = Long.valueOf(modifyRequest.get("userId").toString());
            
            log.info("Received modify request for report {} by user {} with new price {}", 
                    reportId, userId, newPrice);
            
            SimpleReportResponseDTO response = simplePriceReportService.modifyPriceReport(reportId, newPrice, userId);
            
            log.info("Price report modified successfully: {}", response.getMessage());
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            log.error("Price modification failed: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Unexpected error during price modification", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal server error occurred");
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Undo (delete) a price report from the current user
     * 
     * DELETE /api/price-report/undo/{reportId}?userId=123
     * 
     * Only allows users to undo their own reports within 24 hours
     */
    @DeleteMapping("/undo/{reportId}")
    public ResponseEntity<?> undoPriceReport(
            @PathVariable Long reportId,
            @RequestParam Long userId) {
        try {
            log.info("Received undo request for report {} by user {}", reportId, userId);
            
            boolean success = simplePriceReportService.undoPriceReport(reportId, userId);
            
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Price report undone successfully");
                response.put("reportId", reportId);
                response.put("timestamp", java.time.Instant.now().toString());
                
                log.info("Price report {} undone successfully by user {}", reportId, userId);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Could not undo price report");
                errorResponse.put("timestamp", java.time.Instant.now().toString());
                
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
        } catch (RuntimeException e) {
            log.error("Undo failed: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Unexpected error during undo", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal server error occurred");
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get user's price report history
     * 
     * GET /api/price-report/my-reports?userId=123&limit=20
     */
    @GetMapping("/my-reports")
    public ResponseEntity<?> getMyReports(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "20") Integer limit) {
        try {
            log.info("Fetching reports for user {} with limit {}", userId, limit);
            
            List<SimpleReportResponseDTO> reports = simplePriceReportService.getUserReports(userId, limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("reports", reports);
            response.put("count", reports.size());
            response.put("userId", userId);
            response.put("timestamp", java.time.Instant.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error fetching user reports: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Could not fetch user reports");
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Check if user can modify/undo a specific report
     * 
     * GET /api/price-report/can-modify/{reportId}?userId=123
     */
    @GetMapping("/can-modify/{reportId}")
    public ResponseEntity<?> canModifyReport(
            @PathVariable Long reportId,
            @RequestParam Long userId) {
        try {
            boolean canModify = simplePriceReportService.canUserModifyReport(reportId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("canModify", canModify);
            response.put("reportId", reportId);
            response.put("userId", userId);
            response.put("timestamp", java.time.Instant.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error checking modification rights: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Could not check modification rights");
            errorResponse.put("timestamp", java.time.Instant.now().toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}