package com.soukscan.vendorms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorVerificationDTO {
    private Long vendorId;
    private Long adminId;
    private String action; // "VERIFY" ou "REJECT"
    private String comments; // Commentaires de l'admin (optionnel)
}

