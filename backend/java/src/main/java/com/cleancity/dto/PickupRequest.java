package com.cleancity.dto;

import com.cleancity.entity.WasteType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Pickup Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PickupRequest {

    @NotNull(message = "Waste type is required")
    private WasteType wasteType;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Scheduled date is required")
    private LocalDate scheduledDate;

    @NotNull(message = "Scheduled time is required")
    private LocalTime scheduledTime;

    private String zone;

    private String notes;

    private String imageUrl;
}
