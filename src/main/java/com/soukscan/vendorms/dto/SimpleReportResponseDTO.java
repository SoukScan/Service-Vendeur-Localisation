package com.soukscan.vendorms.dto;

/**
 * Simplified response for price reporting
 */
public class SimpleReportResponseDTO {

    private Long vendorId;
    private String shopName;
    private Double latitude;
    private Double longitude;
    private Long productId;
    private Double price;
    private Boolean isNewShop;
    private Boolean isNewProduct;
    private String message;
    private Double distanceFromUser; // Distance in meters for verification
    
    public SimpleReportResponseDTO() {}
    
    public SimpleReportResponseDTO(Long vendorId, String shopName, Double latitude, Double longitude, 
                                  Long productId, Double price, Boolean isNewShop, Boolean isNewProduct, 
                                  String message, Double distanceFromUser) {
        this.vendorId = vendorId;
        this.shopName = shopName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.productId = productId;
        this.price = price;
        this.isNewShop = isNewShop;
        this.isNewProduct = isNewProduct;
        this.message = message;
        this.distanceFromUser = distanceFromUser;
    }

    // Getters and setters
    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
    
    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    
    public Boolean getIsNewShop() { return isNewShop; }
    public void setIsNewShop(Boolean isNewShop) { this.isNewShop = isNewShop; }
    
    public Boolean getIsNewProduct() { return isNewProduct; }
    public void setIsNewProduct(Boolean isNewProduct) { this.isNewProduct = isNewProduct; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public Double getDistanceFromUser() { return distanceFromUser; }
    public void setDistanceFromUser(Double distanceFromUser) { this.distanceFromUser = distanceFromUser; }
}