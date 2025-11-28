package com.soukscan.vendorms.dto;

import com.soukscan.vendorms.entity.VendorStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorResponseDTO {

    private Long id;
    private Long userId;
    private String shopName;
    private String shopAddress;
    private String description;
    private String email;
    private String phone;
    private String city;
    private String country;
    private String postalCode;
    private String taxId;
    private VendorStatus vendorStatus;
    private String shopVerificationFilePath;
    private Long verifiedByAdminId;
    private LocalDateTime verifiedAt;
    private List<Long> declaredByUserIds;
    private Boolean isActive;
    private Double rating;
    private Integer totalReviews;
    private Double latitude;
    private Double longitude;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

