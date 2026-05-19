package com.cleancity.dto;

import com.cleancity.entity.PickupStatus;
import com.cleancity.entity.WasteType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Pickup Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PickupResponse {

    private UUID id;
    private UUID residentId;
    private String residentName;
    private UUID collectorId;
    private String collectorName;
    private Integer zoneId;
    private String zoneName;
    private WasteType wasteType;
    private String location;
    private LocalDate scheduledDate;
    private LocalTime scheduledTime;
    private PickupStatus status;
    private String notes;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime assignedAt;
    private LocalDateTime completedAt;
}
