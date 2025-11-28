package com.soukscan.vendorms.entity;

public enum VendorStatus {
    PENDING,     // En attente de vérification
    VERIFIED,    // Vérifié avec document par l'admin
    UNVERIFIED,  // Déclaré par un utilisateur normal (comme Waze) sans document
    REJECTED,    // Rejeté par l'admin
    SUSPENDED    // Suspendu
}

