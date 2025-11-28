package com.soukscan.vendorms.service;

import com.soukscan.vendorms.client.ProductClient;
import com.soukscan.vendorms.dto.NearbyShopsResponseDTO;
import com.soukscan.vendorms.dto.QuickShopReportDTO;
import com.soukscan.vendorms.dto.QuickShopReportResponseDTO;
import com.soukscan.vendorms.entity.Vendor;
import com.soukscan.vendorms.entity.VendorProduct;
import com.soukscan.vendorms.entity.VendorStatus;
import com.soukscan.vendorms.repository.VendorProductRepository;
import com.soukscan.vendorms.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuickShopReportService {

    private final VendorRepository vendorRepository;
    private final VendorProductRepository vendorProductRepository;
    private final ProductClient productClient;

    /**
     * ÉTAPE 1 : Rechercher les shops à proximité de l'utilisateur
     * L'utilisateur DOIT appeler ceci d'abord pour voir les shops proches
     */
    @Transactional(readOnly = true)
    public NearbyShopsResponseDTO findNearbyShops(
            Long productId,
            Double userLatitude,
            Double userLongitude,
            Integer searchRadiusMeters
    ) {
        log.info("Recherche de shops à proximité - Product: {}, User: ({}, {}), Radius: {}m",
                productId, userLatitude, userLongitude, searchRadiusMeters);

        // Vérifier que le produit existe et est actif
        Map<String, Object> product = productClient.getProductById(productId);
        if (product == null) {
            throw new RuntimeException("Product with id " + productId + " not found");
        }
        Boolean isActive = (Boolean) product.get("isActive");
        if (isActive == null || !isActive) {
            throw new RuntimeException("Product with id " + productId + " is not active");
        }

        // Récupérer tous les shops
        List<Vendor> allVendors = vendorRepository.findAll();

        // Filtrer par proximité et calculer distance
        List<NearbyShopsResponseDTO.NearbyShopDTO> nearbyShops = allVendors.stream()
                .filter(vendor -> vendor.getLatitude() != null && vendor.getLongitude() != null)
                .map(vendor -> {
                    double distance = calculateDistance(
                            userLatitude, userLongitude,
                            vendor.getLatitude(), vendor.getLongitude()
                    );

                    // Vérifier si ce shop a déjà ce produit
                    boolean hasProduct = vendorProductRepository
                            .findByVendorIdAndProductId(vendor.getId(), productId)
                            .isPresent();

                    return NearbyShopsResponseDTO.NearbyShopDTO.builder()
                            .vendorId(vendor.getId())
                            .shopName(vendor.getShopName())
                            .shopAddress(vendor.getShopAddress())
                            .city(vendor.getCity())
                            .latitude(vendor.getLatitude())
                            .longitude(vendor.getLongitude())
                            .distanceMeters(distance)
                            .hasProduct(hasProduct)
                            .build();
                })
                .filter(shop -> shop.getDistanceMeters() <= searchRadiusMeters)
                .sorted((s1, s2) -> Double.compare(s1.getDistanceMeters(), s2.getDistanceMeters()))
                .collect(Collectors.toList());

        log.info("Trouvé {} shops à proximité", nearbyShops.size());

        return NearbyShopsResponseDTO.builder()
                .nearbyShops(nearbyShops)
                .count(nearbyShops.size())
                .canCreateNew(true)
                .searchRadiusMeters(searchRadiusMeters.doubleValue())
                .build();
    }

    /**
     * ÉTAPE 2 : Créer le signalement AVEC VALIDATION STRICTE DE PROXIMITÉ
     * L'utilisateur doit être physiquement proche du shop
     */
    @Transactional
    public QuickShopReportResponseDTO reportProductPrice(QuickShopReportDTO request) {
        log.info("Création signalement rapide - Product: {}, Vendor: {}, User: {}",
                request.getProductId(), request.getVendorId(), request.getUserId());

        // 1. Vérifier que le produit existe et est actif
        Map<String, Object> product = productClient.getProductById(request.getProductId());
        if (product == null) {
            throw new RuntimeException("Product with id " + request.getProductId() + " not found");
        }
        Boolean isActive = (Boolean) product.get("isActive");
        if (isActive == null || !isActive) {
            throw new RuntimeException("Product with id " + request.getProductId() + " is not active");
        }

        Vendor vendor;
        boolean isNewShop = false;
        boolean isNewProduct = false;

        if (request.getVendorId() != null) {
            // CAS 1 : Shop existant - VALIDATION STRICTE DE PROXIMITÉ
            vendor = vendorRepository.findById(request.getVendorId())
                    .orElseThrow(() -> new RuntimeException("Vendor with id " + request.getVendorId() + " not found"));

            // VALIDATION CRITIQUE : L'utilisateur DOIT être physiquement proche
            double distance = calculateDistance(
                    request.getLatitude(), request.getLongitude(),
                    vendor.getLatitude(), vendor.getLongitude()
            );

            if (distance > request.getSearchRadiusMeters()) {
                throw new RuntimeException(
                        String.format("Vous êtes trop loin du shop (%.0fm). Distance maximale autorisée : %dm. " +
                                "Vous devez être physiquement près du shop pour le déclarer.",
                                distance, request.getSearchRadiusMeters())
                );
            }

            log.info("Shop existant validé - Distance: {:.2f}m", distance);

        } else {
            // CAS 2 : Nouveau shop - Vérifier qu'il n'y a vraiment AUCUN shop proche
            List<Vendor> nearbyVendors = vendorRepository.findAll().stream()
                    .filter(v -> v.getLatitude() != null && v.getLongitude() != null)
                    .filter(v -> {
                        double d = calculateDistance(
                                request.getLatitude(), request.getLongitude(),
                                v.getLatitude(), v.getLongitude()
                        );
                        return d <= request.getSearchRadiusMeters();
                    })
                    .collect(Collectors.toList());

            if (!nearbyVendors.isEmpty()) {
                throw new RuntimeException(
                        String.format("Un ou plusieurs shops existent déjà à proximité (%d trouvé(s)). " +
                                "Veuillez d'abord utiliser l'endpoint de recherche pour les voir.",
                                nearbyVendors.size())
                );
            }

            // Créer le nouveau shop à la position de l'utilisateur
            vendor = new Vendor();
            vendor.setShopName(request.getShopName() != null ? request.getShopName() :
                    "Shop " + System.currentTimeMillis());
            vendor.setShopAddress(request.getShopAddress());
            vendor.setCity(request.getCity());
            vendor.setCountry(request.getCountry());
            vendor.setLatitude(request.getLatitude());
            vendor.setLongitude(request.getLongitude());
            vendor.setVendorStatus(VendorStatus.UNVERIFIED);
            vendor.setIsActive(true);
            vendor.setRating(0.0);
            vendor.setTotalReviews(0);

            List<Long> declarants = new ArrayList<>();
            declarants.add(request.getUserId());
            vendor.setDeclaredByUserIds(declarants);

            vendor = vendorRepository.save(vendor);
            isNewShop = true;

            log.info("Nouveau shop créé - ID: {}", vendor.getId());
        }

        // 3. Ajouter/Mettre à jour le produit
        VendorProduct vendorProduct = vendorProductRepository
                .findByVendorIdAndProductId(vendor.getId(), request.getProductId())
                .orElse(null);

        if (vendorProduct != null) {
            // Mettre à jour le prix
            vendorProduct.setPrice(request.getPrice());
            vendorProductRepository.save(vendorProduct);
        } else {
            // Ajouter le produit
            vendorProduct = new VendorProduct();
            vendorProduct.setVendorId(vendor.getId());
            vendorProduct.setProductId(request.getProductId());
            vendorProduct.setPrice(request.getPrice());
            vendorProduct.setIsAvailable(true);
            vendorProduct = vendorProductRepository.save(vendorProduct);
            isNewProduct = true;
        }

        // 4. Ajouter l'utilisateur comme déclarant si ce n'est pas déjà fait
        if (!vendor.getDeclaredByUserIds().contains(request.getUserId())) {
            vendor.getDeclaredByUserIds().add(request.getUserId());
            vendorRepository.save(vendor);
        }

        // 5. Préparer la réponse
        QuickShopReportResponseDTO response = new QuickShopReportResponseDTO();
        response.setVendorId(vendor.getId());
        response.setShopName(vendor.getShopName());
        response.setLatitude(vendor.getLatitude());
        response.setLongitude(vendor.getLongitude());
        response.setVendorProductId(vendorProduct.getId());
        response.setProductId(request.getProductId());
        response.setPrice(request.getPrice());
        response.setIsNewShop(isNewShop);
        response.setIsNewProduct(isNewProduct);

        if (isNewShop) {
            response.setMessage("Nouveau shop créé avec succès");
        } else if (isNewProduct) {
            response.setMessage("Produit ajouté au shop existant");
        } else {
            response.setMessage("Prix mis à jour pour le shop existant");
        }

        return response;
    }

    /**
     * Calcule la distance entre deux points géographiques (formule de Haversine)
     * @return Distance en mètres
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Rayon de la Terre en mètres

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Distance en mètres
    }
}

