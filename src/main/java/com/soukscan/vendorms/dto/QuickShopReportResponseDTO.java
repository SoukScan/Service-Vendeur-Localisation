package com.soukscan.vendorms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuickShopReportResponseDTO {

    private Long vendorId;
    private String shopName;
    private Double latitude;
    private Double longitude;
    private Long vendorProductId;
    private Long productId;
    private Double price;
    private Boolean isNewShop;
    private Boolean isNewProduct;
    private String message;
}

