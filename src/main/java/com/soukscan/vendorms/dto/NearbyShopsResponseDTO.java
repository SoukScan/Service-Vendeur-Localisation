package com.soukscan.vendorms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearbyShopsResponseDTO {
    private List<NearbyShopDTO> nearbyShops;
    private int count;
    private boolean canCreateNew;
    private double searchRadiusMeters;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NearbyShopDTO {
        private Long vendorId;
        private String shopName;
        private String shopAddress;
        private String city;
        private Double latitude;
        private Double longitude;
        private Double distanceMeters;
        private boolean hasProduct;
    }
}

