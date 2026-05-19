package com.cleancity.service;

import com.cleancity.dto.ReportRequest;
import com.cleancity.dto.ReportResponse;
import com.cleancity.dto.ResolveReportRequest;
import com.cleancity.entity.Report;
import com.cleancity.entity.ReportStatus;
import com.cleancity.entity.Resident;
import com.cleancity.entity.User;
import com.cleancity.exception.ResourceNotFoundException;
import com.cleancity.exception.UnauthorizedException;
import com.cleancity.repository.ReportRepository;
import com.cleancity.repository.ResidentRepository;
import com.cleancity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for Report business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final ReportRepository reportRepository;
    private final ResidentRepository residentRepository;
    private final UserRepository userRepository;

    /**
     * Create a new report
     */
    @Transactional
    public ReportResponse createReport(ReportRequest request, UUID residentId) {
        log.info("Creating report for resident: {}", residentId);

        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new ResourceNotFoundException("Resident not found"));

        Report report = Report.builder()
                .resident(resident)
                .issueType(request.getIssueType())
                .location(request.getLocation())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .status(ReportStatus.PENDING)
                .build();

        Report savedReport = reportRepository.save(report);
        log.info("Report created successfully with ID: {}", savedReport.getId());

        return mapToResponse(savedReport);
    }

    /**
     * Resolve a report - Admin functionality
     */
    @Transactional
    public ReportResponse resolveReport(ResolveReportRequest request, UUID adminId) {
        log.info("Resolving report {} by admin {}", request.getReportId(), adminId);

        Report report = reportRepository.findById(request.getReportId())
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));

        // Validate report status
        if (report.getStatus() == ReportStatus.RESOLVED) {
            throw new IllegalStateException("Report is already resolved");
        }

        if (report.getStatus() == ReportStatus.REJECTED) {
            throw new IllegalStateException("Report is rejected and cannot be resolved");
        }

        // Get admin user
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        if (admin.getRole() != com.cleancity.entity.UserRole.ADMIN) {
            throw new UnauthorizedException("Only admins can resolve reports");
        }

        // Update report status to RESOLVED
        report.setStatus(ReportStatus.RESOLVED);
        report.setResolvedBy(admin);
        report.setResolvedAt(LocalDateTime.now());
        
        if (request.getAdminNotes() != null) {
            report.setAdminNotes(request.getAdminNotes());
        }

        Report savedReport = reportRepository.save(report);

        log.info("Report {} resolved successfully by admin {}", savedReport.getId(), adminId);

        return mapToResponse(savedReport);
    }

    /**
     * Reject a report - Admin functionality
     */
    @Transactional
    public ReportResponse rejectReport(UUID reportId, UUID adminId, String reason) {
        log.info("Rejecting report {} by admin {}", reportId, adminId);

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));

        if (report.getStatus() == ReportStatus.RESOLVED) {
            throw new IllegalStateException("Report is already resolved");
        }

        if (report.getStatus() == ReportStatus.REJECTED) {
            throw new IllegalStateException("Report is already rejected");
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        if (admin.getRole() != com.cleancity.entity.UserRole.ADMIN) {
            throw new UnauthorizedException("Only admins can reject reports");
        }

        report.setStatus(ReportStatus.REJECTED);
        report.setResolvedBy(admin);
        report.setAdminNotes(reason);

        Report savedReport = reportRepository.save(report);

        log.info("Report {} rejected by admin {}", reportId, adminId);

        return mapToResponse(savedReport);
    }

    /**
     * Get all reports for a resident
     */
    public List<ReportResponse> getReportsByResident(UUID residentId) {
        log.info("Fetching reports for resident: {}", residentId);
        
        List<Report> reports = reportRepository.findByResidentIdOrderByCreatedAtDesc(residentId);

        return reports.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all reports (admin only)
     */
    public List<ReportResponse> getAllReports() {
        log.info("Fetching all reports");
        
        List<Report> reports = reportRepository.findAll();

        return reports.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get reports by status (admin only)
     */
    public List<ReportResponse> getReportsByStatus(ReportStatus status) {
        log.info("Fetching reports with status: {}", status);
        
        List<Report> reports = reportRepository.findByStatusOrderByCreatedAtDesc(status);

        return reports.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get report by ID
     */
    public ReportResponse getReportById(UUID reportId) {
        log.info("Fetching report: {}", reportId);
        
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));

        return mapToResponse(report);
    }

    /**
     * Get pending reports (admin only)
     */
    public List<ReportResponse> getPendingReports() {
        log.info("Fetching pending reports");
        
        List<Report> reports = reportRepository.findByStatusOrderByCreatedAtDesc(ReportStatus.PENDING);

        return reports.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get reports resolved by a specific admin
     */
    public List<ReportResponse> getReportsResolvedByAdmin(UUID adminId) {
        log.info("Fetching reports resolved by admin: {}", adminId);
        
        List<Report> reports = reportRepository.findByResolvedByOrderByResolvedAtDesc(adminId);

        return reports.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Map Report entity to ReportResponse DTO
     */
    private ReportResponse mapToResponse(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .residentId(report.getResident().getId())
                .residentName(report.getResident().getUser().getFullName())
                .issueType(report.getIssueType())
                .location(report.getLocation())
                .description(report.getDescription())
                .status(report.getStatus())
                .imageUrl(report.getImageUrl())
                .adminNotes(report.getAdminNotes())
                .resolvedBy(report.getResolvedBy() != null ? report.getResolvedBy().getId() : null)
                .resolvedByName(report.getResolvedBy() != null ? report.getResolvedBy().getFullName() : null)
                .createdAt(report.getCreatedAt())
                .resolvedAt(report.getResolvedAt())
                .build();
    }
}
