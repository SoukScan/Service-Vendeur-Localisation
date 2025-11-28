package com.soukscan.vendorms.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for creating a price report.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceReportRequestDTO {

    @NotBlank(message = "Product ID est obligatoire")
    private String productId;

    @NotBlank(message = "Vendor ID est obligatoire")
    private String vendorId;

    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix doit être supérieur à 0")
    private BigDecimal price;
}

