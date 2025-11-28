package com.soukscan.vendorms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * DTO for average price response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceAvgResponseDTO {

    private String productId;
    private BigDecimal averagePrice;
    private Integer reportCount;
    private OffsetDateTime lastUpdated;
}

