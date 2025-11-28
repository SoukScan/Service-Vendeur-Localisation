package com.soukscan.vendorms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Entity representing the calculated average price for each product.
 * Maps to the 'price_avg' table in the database.
 */
@Entity
@Table(name = "price_avg")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceAvg {

    /**
     * Product ID - serves as the primary key.
     */
    @Id
    @Column(name = "product_id", length = 50)
    private String productId;

    /**
     * The calculated average price for this product.
     */
    @Column(name = "average_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal averagePrice;

    /**
     * Total count of reports used to calculate this average.
     * Helpful for debugging and metrics.
     */
    @Column(name = "report_count")
    private Integer reportCount = 0;

    /**
     * When this average was last computed/updated.
     */
    @Column(name = "last_updated", columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP")
    private OffsetDateTime lastUpdated;

    /**
     * Constructor for creating a new average price entry.
     */
    public PriceAvg(String productId, BigDecimal averagePrice, Integer reportCount) {
        this.productId = productId;
        this.averagePrice = averagePrice;
        this.reportCount = reportCount;
        this.lastUpdated = OffsetDateTime.now();
    }
}

