package com.cleancity.repository;

import com.cleancity.entity.Report;
import com.cleancity.entity.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Report entity
 */
@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {

    @Query("SELECT r FROM Report r WHERE r.resident.user.email = :email ORDER BY r.createdAt DESC")
    List<Report> findByResidentEmailOrderByCreatedAtDesc(String email);

    @Query("SELECT r FROM Report r WHERE r.status = :status ORDER BY r.createdAt DESC")
    List<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status);

    @Query("SELECT r FROM Report r WHERE r.resident.id = :residentId ORDER BY r.createdAt DESC")
    List<Report> findByResidentIdOrderByCreatedAtDesc(UUID residentId);

    @Query("SELECT COUNT(r) FROM Report r WHERE r.resident.id = :residentId AND r.status = :status")
    long countByResidentIdAndStatus(UUID residentId, ReportStatus status);

    @Query("SELECT COUNT(r) FROM Report r WHERE r.status = :status")
    long countByStatus(ReportStatus status);

    @Query("SELECT r FROM Report r WHERE r.resolvedBy.id = :userId ORDER BY r.resolvedAt DESC")
    List<Report> findByResolvedByOrderByResolvedAtDesc(UUID userId);
}
