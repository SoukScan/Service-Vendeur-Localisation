package com.soukscan.vendorms.service;

import com.soukscan.vendorms.client.ProductClient;
import com.soukscan.vendorms.dto.VendorProductResponse;
import com.soukscan.vendorms.entity.VendorProduct;
import com.soukscan.vendorms.repository.VendorProductRepository;
import com.soukscan.vendorms.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class VendorProductService {

    private final VendorProductRepository vendorProductRepository;
    private final VendorRepository vendorRepository;
    private final ProductClient productClient;

    @Transactional
    public VendorProduct addProductToVendor(Long vendorId, Long productId, Double price) {
        // Vérifier que le vendor existe
        if (!vendorRepository.existsById(vendorId)) {
            throw new RuntimeException("Vendor with id " + vendorId + " not found");
        }

        // Vérifier que le produit existe dans le catalogue
        Map<String, Object> product = productClient.getProductById(productId);
        if (product == null) {
            throw new RuntimeException("Product with id " + productId + " not found in catalog");
        }

        // Vérifier que le produit est actif
        Boolean isActive = (Boolean) product.get("isActive");
        if (isActive == null || !isActive) {
            throw new RuntimeException("Product with id " + productId + " is not active");
        }

        // Vérifier si le produit n'est pas déjà ajouté
        if (vendorProductRepository.existsByVendorIdAndProductId(vendorId, productId)) {
            throw new RuntimeException("Product already added to this vendor");
        }

        // Créer l'association
        VendorProduct vendorProduct = new VendorProduct();
        vendorProduct.setVendorId(vendorId);
        vendorProduct.setProductId(productId);
        vendorProduct.setPrice(price);
        vendorProduct.setIsAvailable(true);

        log.info("Adding product {} to vendor {}", productId, vendorId);
        return vendorProductRepository.save(vendorProduct);
    }

    @Transactional
    public VendorProduct updateVendorProduct(Long vendorId, Long vendorProductId, Double price, Boolean isAvailable) {
        VendorProduct vendorProduct = vendorProductRepository.findById(vendorProductId)
                .orElseThrow(() -> new RuntimeException("VendorProduct not found"));

        if (!vendorProduct.getVendorId().equals(vendorId)) {
            throw new RuntimeException("Unauthorized: This product does not belong to vendor " + vendorId);
        }

        if (price != null) {
            vendorProduct.setPrice(price);
        }
        if (isAvailable != null) {
            vendorProduct.setIsAvailable(isAvailable);
        }

        log.info("Updating vendor product {} for vendor {}", vendorProductId, vendorId);
        return vendorProductRepository.save(vendorProduct);
    }

    public List<VendorProductResponse> getVendorProducts(Long vendorId, Boolean onlyAvailable) {
        List<VendorProduct> vendorProducts = onlyAvailable
                ? vendorProductRepository.findByVendorIdAndIsAvailableTrue(vendorId)
                : vendorProductRepository.findByVendorId(vendorId);

        List<VendorProductResponse> responses = new ArrayList<>();

        for (VendorProduct vp : vendorProducts) {
            try {
                Map<String, Object> product = productClient.getProductById(vp.getProductId());

                // Vérifier que le produit est actif
                Boolean isActive = (Boolean) product.get("isActive");
                if (isActive == null || !isActive) {
                    log.warn("Product {} is not active, skipping", vp.getProductId());
                    continue;
                }

                VendorProductResponse response = new VendorProductResponse();
                response.setVendorProductId(vp.getId());
                response.setVendorId(vp.getVendorId());
                response.setProductId(vp.getProductId());
                response.setProductName((String) product.get("name"));
                response.setProductDescription((String) product.get("description"));
                response.setProductCategory((String) product.get("category"));
                response.setProductUnit((String) product.get("unit"));
                response.setProductImageUrl((String) product.get("imageUrl"));
                response.setPrice(vp.getPrice());
                response.setIsAvailable(vp.getIsAvailable());
                response.setAddedAt(vp.getAddedAt());
                response.setUpdatedAt(vp.getUpdatedAt());

                responses.add(response);
            } catch (Exception e) {
                log.error("Error fetching product details for product {}: {}", vp.getProductId(), e.getMessage());
                // Continue avec les autres produits même si un produit échoue
            }
        }

        return responses;
    }

    @Transactional
    public void removeProductFromVendor(Long vendorId, Long vendorProductId) {
        VendorProduct vendorProduct = vendorProductRepository.findById(vendorProductId)
                .orElseThrow(() -> new RuntimeException("VendorProduct not found"));

        if (!vendorProduct.getVendorId().equals(vendorId)) {
            throw new RuntimeException("Unauthorized: This product does not belong to vendor " + vendorId);
        }

        log.info("Removing product {} from vendor {}", vendorProductId, vendorId);
        vendorProductRepository.delete(vendorProduct);
    }

    public List<Map<String, Object>> getProductCatalog() {
        List<Map<String, Object>> allProducts = productClient.getAllProducts();
        // Filtrer seulement les produits actifs
        return allProducts.stream()
                .filter(product -> {
                    Boolean isActive = (Boolean) product.get("isActive");
                    return isActive != null && isActive;
                })
                .toList();
    }

    public List<Map<String, Object>> searchProductCatalog(String name) {
        List<Map<String, Object>> results = productClient.searchProducts(name);
        // Filtrer seulement les produits actifs
        return results.stream()
                .filter(product -> {
                    Boolean isActive = (Boolean) product.get("isActive");
                    return isActive != null && isActive;
                })
                .toList();
    }
}

