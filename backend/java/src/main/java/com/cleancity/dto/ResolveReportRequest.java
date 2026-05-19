package com.cleancity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Resolve Report Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResolveReportRequest {

    @NotNull(message = "Report ID is required")
    private java.util.UUID reportId;

    private String adminNotes;
}
