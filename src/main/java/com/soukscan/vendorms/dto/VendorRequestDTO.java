package com.soukscan.vendorms.dto;

import com.soukscan.vendorms.entity.VendorStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorRequestDTO {

    // Optionnel (null si déclaré par un autre utilisateur)
    private Long userId;

    @NotBlank(message = "Le nom du shop est obligatoire")
    private String shopName;

    private String shopAddress;

    private String description;

    // Optionnel (obligatoire seulement si userId est fourni)
    @Email(message = "Format d'email invalide")
    private String email;

    private String phone;
    private String city;
    private String country;
    private String postalCode;
    private String taxId;

    // Pour les shops vérifiés
    private String shopVerificationFilePath;

    // Pour les shops déclarés par des utilisateurs (comme Waze)
    // Cet utilisateur sera ajouté à la liste des déclarants
    private Long declaredByUserId;

    // Géolocalisation
    private Double latitude;
    private Double longitude;
}

