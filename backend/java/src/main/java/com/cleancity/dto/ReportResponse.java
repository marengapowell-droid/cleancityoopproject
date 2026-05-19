package com.cleancity.dto;

import com.cleancity.entity.IssueType;
import com.cleancity.entity.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Report Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponse {

    private UUID id;
    private UUID residentId;
    private String residentName;
    private IssueType issueType;
    private String location;
    private String description;
    private ReportStatus status;
    private String imageUrl;
    private String adminNotes;
    private UUID resolvedBy;
    private String resolvedByName;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}
