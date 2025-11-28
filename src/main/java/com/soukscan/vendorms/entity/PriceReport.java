package com.soukscan.vendorms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Entity representing individual price reports from vendors for products.
 * Maps to the 'price_reports' table in the database.
 */
@Entity
@Table(name = "price_reports",
       indexes = {
           @Index(name = "idx_product_id", columnList = "product_id"),
           @Index(name = "idx_vendor_id", columnList = "vendor_id")
       },
       uniqueConstraints = {
           @UniqueConstraint(
               name = "unique_product_vendor_time",
               columnNames = {"product_id", "vendor_id", "reported_at"}
           )
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceReport {

    /**
     * Unique identifier for this price report.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    /**
     * Product ID from the Product Microservice.
     * No strict foreign key across service boundaries.
     */
    @Column(name = "product_id", nullable = false, length = 50)
    private String productId;

    /**
     * Vendor ID - references the Location entity.
     */
    @Column(name = "vendor_id", nullable = false, length = 50)
    private String vendorId;

    /**
     * Reference to the Location entity (optional, for convenience).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", insertable = false, updatable = false)
    private Location location;

    /**
     * The reported price value.
     */
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Timestamp when the price was reported.
     * Uses TIMESTAMP WITH TIME ZONE for proper timezone handling.
     */
    @Column(name = "reported_at", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private OffsetDateTime reportedAt;

    /**
     * Constructor for creating a new price report.
     */
    public PriceReport(String productId, String vendorId, BigDecimal price) {
        this.productId = productId;
        this.vendorId = vendorId;
        this.price = price;
        this.reportedAt = OffsetDateTime.now();
    }
}

