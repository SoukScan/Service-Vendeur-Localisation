package com.soukscan.vendorms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "vendor_products", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"vendor_id", "product_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendorProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vendor_id", nullable = false)
    private Long vendorId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "price")
    private Double price;


    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    @CreationTimestamp
    @Column(name = "added_at", updatable = false)
    private LocalDateTime addedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

