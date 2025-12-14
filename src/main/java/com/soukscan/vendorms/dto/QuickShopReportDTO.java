package com.soukscan.vendorms.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuickShopReportDTO {

    @NotNull(message = "L'ID du produit est obligatoire")
    private Long productId;

    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "0.01", message = "Le prix doit être supérieur à 0")
    private Double price;

    @NotNull(message = "La latitude est obligatoire")
    @DecimalMin(value = "-90.0", message = "Latitude invalide")
    @DecimalMax(value = "90.0", message = "Latitude invalide")
    private Double latitude;

    @NotNull(message = "La longitude est obligatoire")
    @DecimalMin(value = "-180.0", message = "Longitude invalide")
    @DecimalMax(value = "180.0", message = "Longitude invalide")
    private Double longitude;

    @NotNull(message = "L'ID de l'utilisateur est obligatoire")
    private Long userId;

    // vendorId : null = créer nouveau shop, sinon = shop existant à valider
    private Long vendorId;

    @Min(value = 10, message = "Le rayon minimum est de 10 mètres")
    @Max(value = 200, message = "Le rayon maximum est de 200 mètres")
    private Integer searchRadiusMeters = 50;

    // Informations optionnelles pour un nouveau shop
    private String shopName;
    private String shopAddress;
    private String city;
    private String country;
}

