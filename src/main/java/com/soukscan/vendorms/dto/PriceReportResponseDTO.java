package com.soukscan.vendorms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * DTO for price report response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceReportResponseDTO {

    private Long reportId;
    private String productId;
    private String vendorId;
    private String vendorName;
    private BigDecimal price;
    private OffsetDateTime reportedAt;
    private Double latitude;
    private Double longitude;
}

