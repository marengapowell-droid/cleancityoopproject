package com.cleancity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Confirm Pickup Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmPickupRequest {

    @NotNull(message = "Pickup ID is required")
    private java.util.UUID pickupId;

    private String notes;
}
