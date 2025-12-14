package com.soukscan.vendorms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vendors",
       indexes = {
           @Index(name = "idx_vendor_user_id", columnList = "user_id"),
           @Index(name = "idx_vendor_location", columnList = "latitude, longitude"),
           @Index(name = "idx_vendor_status", columnList = "vendor_status"),
           @Index(name = "idx_vendor_active", columnList = "is_active"),
           @Index(name = "idx_vendor_city", columnList = "city"),
           @Index(name = "idx_vendor_created", columnList = "created_at")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID de l'utilisateur du microservice d'authentification (peut être null si déclaré)
    @Column(name = "user_id" , unique = true)
    private Long userId;

    // Informations du shop
    @Column(name = "shop_name", nullable = false)
    private String shopName;

    @Column(name = "shop_address")
    private String shopAddress;

    @Column(length = 2000)
    private String description;

    // Contact (peut être null si shop déclaré par un utilisateur)
    @Column(name = "email" , unique = true)
    private String email;

    private String phone;

    private String city;

    private String country;

    @Column(name = "postal_code")
    private String postalCode;

    // Vérification
    @Enumerated(EnumType.STRING)
    @Column(name = "vendor_status", nullable = false)
    private VendorStatus vendorStatus = VendorStatus.PENDING;

    @Column(name = "shop_verification_file_path")
    private String shopVerificationFilePath;

    @Column(name = "verified_by_admin_id")
    private Long verifiedByAdminId;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @ElementCollection
    @CollectionTable(name = "vendor_declarations", joinColumns = @JoinColumn(name = "vendor_id"))
    @Column(name = "user_id")
    private List<Long> declaredByUserIds = new ArrayList<>(); // Pour les shops déclarés par des utilisateurs (comme Waze)

    // Informations supplémentaires
    @Column(name = "tax_id")
    private String taxId;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "rating")
    private Double rating = 0.0;

    @Column(name = "total_reviews")
    private Integer totalReviews = 0;

    // Géolocalisation (si vous utilisez PostGIS)
    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

