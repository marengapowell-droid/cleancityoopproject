package com.cleancity.controller;

import com.cleancity.dto.ApiResponse;
import com.cleancity.dto.ReportRequest;
import com.cleancity.dto.ReportResponse;
import com.cleancity.dto.ResolveReportRequest;
import com.cleancity.entity.ReportStatus;
import com.cleancity.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller for Report API endpoints
 */
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reports", description = "Report management APIs")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @PreAuthorize("hasRole('RESIDENT')")
    @Operation(summary = "Create a new report", description = "Residents can create issue reports")
    public ResponseEntity<ApiResponse<ReportResponse>> createReport(
            @Valid @RequestBody ReportRequest request,
            Authentication authentication) {
        
        UUID userId = UUID.fromString(authentication.getName());
        log.info("POST /reports - Creating report for user: {}", userId);
        
        ReportResponse response = reportService.createReport(request, userId);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Report created successfully", response));
    }

    @PostMapping("/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Resolve a report", description = "Admins can resolve reports")
    public ResponseEntity<ApiResponse<ReportResponse>> resolveReport(
            @Valid @RequestBody ResolveReportRequest request,
            Authentication authentication) {
        
        UUID adminId = UUID.fromString(authentication.getName());
        log.info("POST /reports/resolve - Resolving report {} by admin {}", 
                request.getReportId(), adminId);
        
        ReportResponse response = reportService.resolveReport(request, adminId);
        
        return ResponseEntity
                .ok(ApiResponse.success("Report resolved successfully", response));
    }

    @PatchMapping("/{reportId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reject a report", description = "Admins can reject reports")
    public ResponseEntity<ApiResponse<ReportResponse>> rejectReport(
            @Parameter(description = "Report ID") @PathVariable UUID reportId,
            @RequestParam(required = false) String reason,
            Authentication authentication) {
        
        UUID adminId = UUID.fromString(authentication.getName());
        log.info("PATCH /reports/{}/reject - Rejecting report by admin {}", reportId, adminId);
        
        ReportResponse response = reportService.rejectReport(reportId, adminId, reason);
        
        return ResponseEntity
                .ok(ApiResponse.success("Report rejected successfully", response));
    }

    @GetMapping("/my-reports")
    @PreAuthorize("hasRole('RESIDENT')")
    @Operation(summary = "Get my reports", description = "Residents can view their own reports")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getMyReports(
            Authentication authentication) {
        
        UUID userId = UUID.fromString(authentication.getName());
        log.info("GET /reports/my-reports - Fetching reports for user: {}", userId);
        
        List<ReportResponse> reports = reportService.getReportsByResident(userId);
        
        return ResponseEntity
                .ok(ApiResponse.success(reports));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get pending reports", description = "Admins can view pending reports")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getPendingReports() {
        
        log.info("GET /reports/pending - Fetching pending reports");
        
        List<ReportResponse> reports = reportService.getPendingReports();
        
        return ResponseEntity
                .ok(ApiResponse.success(reports));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get reports by status", description = "Admins can filter reports by status")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getReportsByStatus(
            @Parameter(description = "Report status") @PathVariable ReportStatus status) {
        
        log.info("GET /reports/status/{} - Fetching reports with status: {}", status, status);
        
        List<ReportResponse> reports = reportService.getReportsByStatus(status);
        
        return ResponseEntity
                .ok(ApiResponse.success(reports));
    }

    @GetMapping("/resolved-by-me")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get reports resolved by me", description = "Admins can view reports they resolved")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getReportsResolvedByMe(
            Authentication authentication) {
        
        UUID adminId = UUID.fromString(authentication.getName());
        log.info("GET /reports/resolved-by-me - Fetching reports resolved by admin: {}", adminId);
        
        List<ReportResponse> reports = reportService.getReportsResolvedByAdmin(adminId);
        
        return ResponseEntity
                .ok(ApiResponse.success(reports));
    }

    @GetMapping("/{reportId}")
    @Operation(summary = "Get report by ID", description = "Get details of a specific report")
    public ResponseEntity<ApiResponse<ReportResponse>> getReportById(
            @Parameter(description = "Report ID") @PathVariable UUID reportId) {
        
        log.info("GET /reports/{} - Fetching report details", reportId);
        
        ReportResponse report = reportService.getReportById(reportId);
        
        return ResponseEntity
                .ok(ApiResponse.success(report));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all reports", description = "Admins can view all reports")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getAllReports() {
        
        log.info("GET /reports - Fetching all reports");
        
        List<ReportResponse> reports = reportService.getAllReports();
        
        return ResponseEntity
                .ok(ApiResponse.success(reports));
    }
}
