package com.soukscan.vendorms.controller;

import com.soukscan.vendorms.dto.NearbyShopsResponseDTO;
import com.soukscan.vendorms.dto.QuickShopReportDTO;
import com.soukscan.vendorms.dto.QuickShopReportResponseDTO;
import com.soukscan.vendorms.service.QuickShopReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/quick-report")
public class QuickShopReportController {

    private final QuickShopReportService quickShopReportService;

    public QuickShopReportController(QuickShopReportService quickShopReportService) {
        this.quickShopReportService = quickShopReportService;
    }

    /**
     * ÉTAPE 1 : Rechercher les shops à proximité de l'utilisateur
     * L'utilisateur doit appeler ceci d'abord pour voir les shops proches
     *
     * GET /api/quick-report/nearby-shops?productId=1&lat=33.5898&lon=-7.6038&radius=50
     */
    @GetMapping("/nearby-shops")
    public ResponseEntity<?> findNearbyShops(
            @RequestParam Long productId,
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam(defaultValue = "50") Integer radius
    ) {
        try {
            NearbyShopsResponseDTO response = quickShopReportService.findNearbyShops(
                    productId, lat, lon, radius
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * ÉTAPE 2 : Créer le signalement avec validation stricte de proximité
     * L'utilisateur doit être physiquement proche du shop pour le déclarer
     *
     * POST /api/quick-report
     * Body: { "productId": 1, "price": 15.5, "lat": 33.589, "lon": -7.603, "userId": 101, "vendorId": 16 }
     */
    @PostMapping
    public ResponseEntity<?> reportProductPrice(
            @Valid @RequestBody QuickShopReportDTO request) {
        try {
            QuickShopReportResponseDTO response = quickShopReportService.reportProductPrice(request);
            HttpStatus status = response.getIsNewShop() ? HttpStatus.CREATED : HttpStatus.OK;
            return ResponseEntity.status(status).body(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}

