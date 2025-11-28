package com.soukscan.vendorms.controller;

import com.soukscan.vendorms.dto.VendorRequestDTO;
import com.soukscan.vendorms.dto.VendorResponseDTO;
import com.soukscan.vendorms.entity.VendorStatus;
import com.soukscan.vendorms.service.VendorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;

    @PostMapping
    public ResponseEntity<VendorResponseDTO> createVendor(@Valid @RequestBody VendorRequestDTO request) {
        VendorResponseDTO response = vendorService.createVendor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<VendorResponseDTO>> getAllVendors() {
        List<VendorResponseDTO> vendors = vendorService.getAllVendors();
        return ResponseEntity.ok(vendors);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendorResponseDTO> getVendorById(@PathVariable Long id) {
        VendorResponseDTO vendor = vendorService.getVendorById(id);
        return ResponseEntity.ok(vendor);
    }

    @GetMapping("/active")
    public ResponseEntity<List<VendorResponseDTO>> getActiveVendors() {
        List<VendorResponseDTO> vendors = vendorService.getActiveVendors();
        return ResponseEntity.ok(vendors);
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<VendorResponseDTO>> getVendorsByCity(@PathVariable String city) {
        List<VendorResponseDTO> vendors = vendorService.getVendorsByCity(city);
        return ResponseEntity.ok(vendors);
    }

    @GetMapping("/search")
    public ResponseEntity<List<VendorResponseDTO>> searchVendorsByName(@RequestParam String name) {
        List<VendorResponseDTO> vendors = vendorService.searchVendorsByName(name);
        return ResponseEntity.ok(vendors);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VendorResponseDTO> updateVendor(
            @PathVariable Long id,
            @Valid @RequestBody VendorRequestDTO request) {
        VendorResponseDTO vendor = vendorService.updateVendor(id, request);
        return ResponseEntity.ok(vendor);
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<VendorResponseDTO> toggleVendorStatus(@PathVariable Long id) {
        VendorResponseDTO vendor = vendorService.toggleVendorStatus(id);
        return ResponseEntity.ok(vendor);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVendor(@PathVariable Long id) {
        vendorService.deleteVendor(id);
        return ResponseEntity.noContent().build();
    }

    // Nouveaux endpoints pour la gestion des vendeurs

    @GetMapping("/user/{userId}")
    public ResponseEntity<VendorResponseDTO> getVendorByUserId(@PathVariable Long userId) {
        VendorResponseDTO vendor = vendorService.getVendorByUserId(userId);
        return ResponseEntity.ok(vendor);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<VendorResponseDTO>> getVendorsByStatus(@PathVariable VendorStatus status) {
        List<VendorResponseDTO> vendors = vendorService.getVendorsByStatus(status);
        return ResponseEntity.ok(vendors);
    }

    @GetMapping("/verified")
    public ResponseEntity<List<VendorResponseDTO>> getVerifiedVendors() {
        List<VendorResponseDTO> vendors = vendorService.getVerifiedVendors();
        return ResponseEntity.ok(vendors);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<VendorResponseDTO>> getPendingVendors() {
        List<VendorResponseDTO> vendors = vendorService.getPendingVendors();
        return ResponseEntity.ok(vendors);
    }

    @PatchMapping("/{id}/verify")
    public ResponseEntity<VendorResponseDTO> verifyVendor(
            @PathVariable Long id,
            @RequestParam Long adminId) {
        VendorResponseDTO vendor = vendorService.verifyVendor(id, adminId);
        return ResponseEntity.ok(vendor);
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<VendorResponseDTO> rejectVendor(
            @PathVariable Long id,
            @RequestParam Long adminId) {
        VendorResponseDTO vendor = vendorService.rejectVendor(id, adminId);
        return ResponseEntity.ok(vendor);
    }

    @PostMapping("/{vendorId}/declare")
    public ResponseEntity<VendorResponseDTO> declareExistingVendor(
            @PathVariable Long vendorId,
            @RequestParam Long userId) {
        VendorResponseDTO vendor = vendorService.declareExistingVendor(vendorId, userId);
        return ResponseEntity.ok(vendor);
    }

    @GetMapping("/declared-by/{userId}")
    public ResponseEntity<List<VendorResponseDTO>> getVendorsDeclaredByUser(@PathVariable Long userId) {
        List<VendorResponseDTO> vendors = vendorService.getVendorsDeclaredByUser(userId);
        return ResponseEntity.ok(vendors);
    }
}

