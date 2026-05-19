package com.cleancity.dto;

import com.cleancity.entity.IssueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Report Request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {

    @NotNull(message = "Issue type is required")
    private IssueType issueType;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Description is required")
    private String description;

    private String imageUrl;
}
