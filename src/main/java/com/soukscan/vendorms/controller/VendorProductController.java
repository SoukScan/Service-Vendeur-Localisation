package com.soukscan.vendorms.controller;

import com.soukscan.vendorms.client.ProductClient;
import com.soukscan.vendorms.dto.AddProductRequest;
import com.soukscan.vendorms.dto.VendorProductResponse;
import com.soukscan.vendorms.entity.VendorProduct;
import com.soukscan.vendorms.service.VendorProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vendors/{vendorId}/products")
@RequiredArgsConstructor
public class VendorProductController {

    private final VendorProductService vendorProductService;

    /**
     * Ajouter un produit du catalogue au vendor
     */
    @PostMapping
    public ResponseEntity<VendorProduct> addProductToVendor(
            @PathVariable Long vendorId,
            @RequestBody AddProductRequest request) {
        VendorProduct vendorProduct = vendorProductService.addProductToVendor(
                vendorId,
                request.getProductId(),
                request.getPrice()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(vendorProduct);
    }

    /**
     * Obtenir tous les produits d'un vendor avec détails du catalogue
     */
    @GetMapping
    public ResponseEntity<List<VendorProductResponse>> getVendorProducts(
            @PathVariable Long vendorId,
            @RequestParam(required = false, defaultValue = "true") Boolean onlyAvailable) {
        List<VendorProductResponse> products = vendorProductService.getVendorProducts(vendorId, onlyAvailable);
        return ResponseEntity.ok(products);
    }

    /**
     * Mettre à jour un produit du vendor (prix, disponibilité)
     */
    @PutMapping("/{vendorProductId}")
    public ResponseEntity<VendorProduct> updateVendorProduct(
            @PathVariable Long vendorId,
            @PathVariable Long vendorProductId,
            @RequestBody Map<String, Object> updates) {

        Double price = updates.containsKey("price") ? ((Number) updates.get("price")).doubleValue() : null;
        Boolean isAvailable = updates.containsKey("isAvailable") ? (Boolean) updates.get("isAvailable") : null;

        VendorProduct updated = vendorProductService.updateVendorProduct(
                vendorId,
                vendorProductId,
                price,
                isAvailable
        );
        return ResponseEntity.ok(updated);
    }

    /**
     * Retirer un produit du vendor
     */
    @DeleteMapping("/{vendorProductId}")
    public ResponseEntity<Map<String, String>> removeProductFromVendor(
            @PathVariable Long vendorId,
            @PathVariable Long vendorProductId) {
        vendorProductService.removeProductFromVendor(vendorId, vendorProductId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Product removed successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Obtenir le catalogue complet des produits disponibles
     */
    @GetMapping("/catalog")
    public ResponseEntity<List<Map<String, Object>>> getProductCatalog() {
        List<Map<String, Object>> catalog = vendorProductService.getProductCatalog();
        return ResponseEntity.ok(catalog);
    }

    /**
     * Rechercher dans le catalogue de produits
     */
    @GetMapping("/catalog/search")
    public ResponseEntity<List<Map<String, Object>>> searchProductCatalog(
            @RequestParam String name) {
        List<Map<String, Object>> results = vendorProductService.searchProductCatalog(name);
        return ResponseEntity.ok(results);
    }
}

