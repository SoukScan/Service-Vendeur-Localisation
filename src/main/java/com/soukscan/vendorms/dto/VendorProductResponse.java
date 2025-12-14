package com.soukscan.vendorms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorProductResponse {
    private Long vendorProductId;
    private Long vendorId;
    private Long productId;
    private String productName;
    private String productDescription;
    private String productCategory;
    private String productUnit;
    private String productImageUrl;
    private Double price;
    private Boolean isAvailable;
    private LocalDateTime addedAt;
    private LocalDateTime updatedAt;
}

