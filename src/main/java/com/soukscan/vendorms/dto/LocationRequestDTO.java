package com.soukscan.vendorms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating a vendor location.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationRequestDTO {

    @NotBlank(message = "Vendor ID est obligatoire")
    private String vendorId;

    @NotBlank(message = "Le nom est obligatoire")
    private String name;

    private String address;

    @NotNull(message = "La latitude est obligatoire")
    private Double latitude;

    @NotNull(message = "La longitude est obligatoire")
    private Double longitude;
}

