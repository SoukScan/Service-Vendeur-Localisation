package com.soukscan.vendorms.service;

import com.soukscan.vendorms.dto.VendorRequestDTO;
import com.soukscan.vendorms.dto.VendorResponseDTO;
import com.soukscan.vendorms.entity.Vendor;
import com.soukscan.vendorms.entity.VendorStatus;
import com.soukscan.vendorms.exception.DuplicateResourceException;
import com.soukscan.vendorms.exception.ResourceNotFoundException;
import com.soukscan.vendorms.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VendorService {

    private final VendorRepository vendorRepository;

    @Transactional
    public VendorResponseDTO createVendor(VendorRequestDTO request) {
        log.info("Création d'un nouveau vendeur: {}", request.getShopName());

        // Validation : userId OU declaredByUserId obligatoire
        if (request.getUserId() == null && request.getDeclaredByUserId() == null) {
            throw new IllegalArgumentException(
                "Vous devez fournir soit userId (propriétaire) soit declaredByUserId (déclarant)"
            );
        }

        // Si c'est un propriétaire (userId fourni), email ET phone obligatoires
        if (request.getUserId() != null) {
            if (request.getEmail() == null || request.getEmail().isBlank()) {
                throw new IllegalArgumentException(
                    "L'email est obligatoire quand le propriétaire crée son shop"
                );
            }
            if (request.getPhone() == null || request.getPhone().isBlank()) {
                throw new IllegalArgumentException(
                    "Le téléphone est obligatoire quand le propriétaire crée son shop"
                );
            }

            // Vérifier si l'userId existe déjà
            if (vendorRepository.existsByUserId(request.getUserId())) {
                throw new DuplicateResourceException("Un vendeur existe déjà pour cet utilisateur ID: " + request.getUserId());
            }

            // Vérifier si l'email existe déjà
            if (vendorRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Un vendeur avec cet email existe déjà: " + request.getEmail());
            }
        }

        // Vérifier si un shop similaire existe déjà (pour les déclarations)
        if (request.getDeclaredByUserId() != null && request.getLatitude() != null && request.getLongitude() != null) {
            List<Vendor> similarShops = vendorRepository.findSimilarShops(
                request.getShopName(),
                request.getLatitude(),
                request.getLongitude()
            );

            // Si un shop similaire existe, ajouter l'utilisateur aux déclarants
            if (!similarShops.isEmpty()) {
                Vendor existingShop = similarShops.get(0);

                // Vérifier si l'utilisateur n'a pas déjà déclaré ce shop
                if (!existingShop.getDeclaredByUserIds().contains(request.getDeclaredByUserId())) {
                    existingShop.getDeclaredByUserIds().add(request.getDeclaredByUserId());

                    // Mettre à jour les infos manquantes si elles sont fournies
                    if (request.getEmail() != null && existingShop.getEmail() == null) {
                        existingShop.setEmail(request.getEmail());
                    }
                    if (request.getPhone() != null && existingShop.getPhone() == null) {
                        existingShop.setPhone(request.getPhone());
                    }
                    if (request.getDescription() != null && existingShop.getDescription() == null) {
                        existingShop.setDescription(request.getDescription());
                    }

                    Vendor updatedShop = vendorRepository.save(existingShop);
                    log.info("Shop existant mis à jour. Utilisateur ID: {} ajouté aux déclarants. Total: {}",
                        request.getDeclaredByUserId(), updatedShop.getDeclaredByUserIds().size());
                    return mapToResponseDTO(updatedShop);
                } else {
                    log.warn("L'utilisateur ID: {} a déjà déclaré ce shop", request.getDeclaredByUserId());
                    return mapToResponseDTO(existingShop);
                }
            }
        }

        Vendor vendor = new Vendor();
        vendor.setUserId(request.getUserId());
        vendor.setShopName(request.getShopName());
        vendor.setShopAddress(request.getShopAddress());
        vendor.setDescription(request.getDescription());
        vendor.setEmail(request.getEmail());
        vendor.setPhone(request.getPhone());
        vendor.setCity(request.getCity());
        vendor.setCountry(request.getCountry());
        vendor.setPostalCode(request.getPostalCode());
        vendor.setTaxId(request.getTaxId());
        vendor.setShopVerificationFilePath(request.getShopVerificationFilePath());
        vendor.setLatitude(request.getLatitude());
        vendor.setLongitude(request.getLongitude());
        vendor.setIsActive(true);

        // Déterminer le statut initial
        if (request.getDeclaredByUserId() != null) {
            // Shop déclaré par un utilisateur normal (comme Waze)
            vendor.setVendorStatus(VendorStatus.UNVERIFIED);
            vendor.getDeclaredByUserIds().add(request.getDeclaredByUserId());
            log.info("Shop déclaré par l'utilisateur ID: {}", request.getDeclaredByUserId());
        } else if (request.getShopVerificationFilePath() != null && !request.getShopVerificationFilePath().isEmpty()) {
            // Shop créé par le vendeur avec document, en attente de vérification
            vendor.setVendorStatus(VendorStatus.PENDING);
            log.info("Shop créé avec document de vérification, en attente d'approbation admin");
        } else {
            // Shop créé sans document
            vendor.setVendorStatus(VendorStatus.UNVERIFIED);
        }

        Vendor savedVendor = vendorRepository.save(vendor);
        log.info("Vendeur créé avec succès avec l'ID: {} et statut: {}", savedVendor.getId(), savedVendor.getVendorStatus());

        return mapToResponseDTO(savedVendor);
    }

    @Transactional(readOnly = true)
    public List<VendorResponseDTO> getAllVendors() {
        log.info("Récupération de tous les vendeurs");
        return vendorRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VendorResponseDTO getVendorById(Long id) {
        log.info("Récupération du vendeur avec l'ID: {}", id);
        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendeur non trouvé avec l'ID: " + id));
        return mapToResponseDTO(vendor);
    }

    @Transactional(readOnly = true)
    public List<VendorResponseDTO> getActiveVendors() {
        log.info("Récupération des vendeurs actifs");
        return vendorRepository.findByIsActive(true).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VendorResponseDTO> getVendorsByCity(String city) {
        log.info("Récupération des vendeurs pour la ville: {}", city);
        return vendorRepository.findByCity(city).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VendorResponseDTO> searchVendorsByName(String name) {
        log.info("Recherche des vendeurs avec le nom: {}", name);
        return vendorRepository.findByShopNameContainingIgnoreCase(name).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VendorResponseDTO> getVendorsByStatus(VendorStatus status) {
        log.info("Récupération des vendeurs avec le statut: {}", status);
        return vendorRepository.findByVendorStatus(status).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VendorResponseDTO> getVerifiedVendors() {
        log.info("Récupération des vendeurs vérifiés");
        return vendorRepository.findByVendorStatus(VendorStatus.VERIFIED).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VendorResponseDTO> getPendingVendors() {
        log.info("Récupération des vendeurs en attente de vérification");
        return vendorRepository.findByVendorStatus(VendorStatus.PENDING).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VendorResponseDTO getVendorByUserId(Long userId) {
        log.info("Récupération du vendeur pour l'utilisateur ID: {}", userId);
        Vendor vendor = vendorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun vendeur trouvé pour l'utilisateur ID: " + userId));
        return mapToResponseDTO(vendor);
    }

    @Transactional(readOnly = true)
    public List<VendorResponseDTO> getVendorsDeclaredByUser(Long userId) {
        log.info("Récupération des vendors déclarés par l'utilisateur ID: {}", userId);
        return vendorRepository.findVendorsDeclaredByUser(userId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public VendorResponseDTO verifyVendor(Long id, Long adminId) {
        log.info("Vérification du vendeur ID: {} par l'admin ID: {}", id, adminId);

        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendeur non trouvé avec l'ID: " + id));

        vendor.setVendorStatus(VendorStatus.VERIFIED);
        vendor.setVerifiedByAdminId(adminId);
        vendor.setVerifiedAt(LocalDateTime.now());

        Vendor verifiedVendor = vendorRepository.save(vendor);
        log.info("Vendeur vérifié avec succès: {}", verifiedVendor.getId());

        return mapToResponseDTO(verifiedVendor);
    }

    @Transactional
    public VendorResponseDTO rejectVendor(Long id, Long adminId) {
        log.info("Rejet du vendeur ID: {} par l'admin ID: {}", id, adminId);

        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendeur non trouvé avec l'ID: " + id));

        vendor.setVendorStatus(VendorStatus.REJECTED);
        vendor.setVerifiedByAdminId(adminId);
        vendor.setIsActive(false);

        Vendor rejectedVendor = vendorRepository.save(vendor);
        log.info("Vendeur rejeté: {}", rejectedVendor.getId());

        return mapToResponseDTO(rejectedVendor);
    }

    @Transactional
    public VendorResponseDTO updateVendor(Long id, VendorRequestDTO request) {
        log.info("Mise à jour du vendeur avec l'ID: {}", id);

        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendeur non trouvé avec l'ID: " + id));

        // Vérifier si l'email est déjà utilisé par un autre vendeur
        if (request.getEmail() != null && vendor.getEmail() != null &&
            !vendor.getEmail().equals(request.getEmail()) &&
            vendorRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Un vendeur avec cet email existe déjà: " + request.getEmail());
        }

        // Si nouvel email fourni et ancien email était null
        if (request.getEmail() != null && vendor.getEmail() == null &&
            vendorRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Un vendeur avec cet email existe déjà: " + request.getEmail());
        }

        vendor.setShopName(request.getShopName());
        vendor.setShopAddress(request.getShopAddress());
        vendor.setDescription(request.getDescription());
        vendor.setEmail(request.getEmail());
        vendor.setPhone(request.getPhone());
        vendor.setCity(request.getCity());
        vendor.setCountry(request.getCountry());
        vendor.setPostalCode(request.getPostalCode());
        vendor.setTaxId(request.getTaxId());
        vendor.setLatitude(request.getLatitude());
        vendor.setLongitude(request.getLongitude());

        Vendor updatedVendor = vendorRepository.save(vendor);
        log.info("Vendeur mis à jour avec succès avec l'ID: {}", updatedVendor.getId());

        return mapToResponseDTO(updatedVendor);
    }

    @Transactional
    public void deleteVendor(Long id) {
        log.info("Suppression du vendeur avec l'ID: {}", id);

        if (!vendorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vendeur non trouvé avec l'ID: " + id);
        }

        vendorRepository.deleteById(id);
        log.info("Vendeur supprimé avec succès avec l'ID: {}", id);
    }

    @Transactional
    public VendorResponseDTO toggleVendorStatus(Long id) {
        log.info("Basculer le statut du vendeur avec l'ID: {}", id);

        Vendor vendor = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendeur non trouvé avec l'ID: " + id));

        vendor.setIsActive(!vendor.getIsActive());
        Vendor updatedVendor = vendorRepository.save(vendor);

        log.info("Statut du vendeur basculé avec succès. Nouveau statut: {}", updatedVendor.getIsActive());

        return mapToResponseDTO(updatedVendor);
    }

    @Transactional
    public VendorResponseDTO declareExistingVendor(Long vendorId, Long userId) {
        log.info("L'utilisateur ID: {} déclare le shop ID: {}", userId, vendorId);

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop non trouvé avec l'ID: " + vendorId));

        // Vérifier si l'utilisateur a déjà déclaré ce shop
        if (vendor.getDeclaredByUserIds().contains(userId)) {
            log.warn("L'utilisateur ID: {} a déjà déclaré ce shop", userId);
            throw new DuplicateResourceException("Vous avez déjà déclaré ce shop");
        }

        // Ajouter l'utilisateur à la liste des déclarants
        vendor.getDeclaredByUserIds().add(userId);
        Vendor updatedVendor = vendorRepository.save(vendor);

        log.info("Shop déclaré avec succès. Nombre total de déclarants: {}", updatedVendor.getDeclaredByUserIds().size());

        return mapToResponseDTO(updatedVendor);
    }

    private VendorResponseDTO mapToResponseDTO(Vendor vendor) {
        VendorResponseDTO dto = new VendorResponseDTO();
        dto.setId(vendor.getId());
        dto.setUserId(vendor.getUserId());
        dto.setShopName(vendor.getShopName());
        dto.setShopAddress(vendor.getShopAddress());
        dto.setDescription(vendor.getDescription());
        dto.setEmail(vendor.getEmail());
        dto.setPhone(vendor.getPhone());
        dto.setCity(vendor.getCity());
        dto.setCountry(vendor.getCountry());
        dto.setPostalCode(vendor.getPostalCode());
        dto.setTaxId(vendor.getTaxId());
        dto.setVendorStatus(vendor.getVendorStatus());
        dto.setShopVerificationFilePath(vendor.getShopVerificationFilePath());
        dto.setVerifiedByAdminId(vendor.getVerifiedByAdminId());
        dto.setVerifiedAt(vendor.getVerifiedAt());
        dto.setDeclaredByUserIds(vendor.getDeclaredByUserIds());
        dto.setIsActive(vendor.getIsActive());
        dto.setRating(vendor.getRating());
        dto.setTotalReviews(vendor.getTotalReviews());
        dto.setLatitude(vendor.getLatitude());
        dto.setLongitude(vendor.getLongitude());
        dto.setCreatedAt(vendor.getCreatedAt());
        dto.setUpdatedAt(vendor.getUpdatedAt());
        return dto;
    }
}

