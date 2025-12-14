package com.soukscan.vendorms.dto;

import jakarta.validation.constraints.*;

/**
 * Simplified DTO for price reporting - matches your exact requirements
 * Only requires: location (from map click), product (pre-selected), price, and optional shop name
 */
public class SimpleReportRequestDTO {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private Double price;

    @NotNull(message = "Latitude is required (from map click)")
    @DecimalMin(value = "-90.0", message = "Invalid latitude")
    @DecimalMax(value = "90.0", message = "Invalid latitude")
    private Double latitude;

    @NotNull(message = "Longitude is required (from map click)")
    @DecimalMin(value = "-180.0", message = "Invalid longitude")
    @DecimalMax(value = "180.0", message = "Invalid longitude")
    private Double longitude;

    @NotNull(message = "User ID is required")
    private Long userId;

    // Optional shop name - if not provided, system will auto-generate
    @Size(max = 100, message = "Shop name cannot exceed 100 characters")
    private String shopName;
    
    // Optional GPS accuracy for enhanced location verification
    private Double gpsAccuracyMeters;
    
    public SimpleReportRequestDTO() {}
    
    public SimpleReportRequestDTO(Long productId, Double price, Double latitude, Double longitude, Long userId, String shopName) {
        this.productId = productId;
        this.price = price;
        this.latitude = latitude;
        this.longitude = longitude;
        this.userId = userId;
        this.shopName = shopName;
    }

    // Getters and setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    
    public Double getGpsAccuracyMeters() { return gpsAccuracyMeters; }
    public void setGpsAccuracyMeters(Double gpsAccuracyMeters) { this.gpsAccuracyMeters = gpsAccuracyMeters; }
}