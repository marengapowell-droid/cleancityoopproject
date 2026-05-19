package com.cleancity.repository;

import com.cleancity.entity.Pickup;
import com.cleancity.entity.PickupStatus;
import com.cleancity.entity.WasteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Pickup entity
 */
@Repository
public interface PickupRepository extends JpaRepository<Pickup, UUID> {

    @Query("SELECT p FROM Pickup p WHERE p.resident.user.email = :email ORDER BY p.createdAt DESC")
    List<Pickup> findByResidentEmailOrderByCreatedAtDesc(String email);

    @Query("SELECT p FROM Pickup p WHERE p.collector.user.email = :email ORDER BY p.scheduledDate ASC, p.scheduledTime ASC")
    List<Pickup> findByCollectorEmailOrderByScheduledDateAsc(String email);

    @Query("SELECT p FROM Pickup p WHERE p.collector.id = :collectorId ORDER BY p.scheduledDate ASC, p.scheduledTime ASC")
    List<Pickup> findByCollectorIdOrderByScheduledDateAsc(UUID collectorId);

    @Query("SELECT p FROM Pickup p WHERE p.status = :status ORDER BY p.scheduledDate ASC, p.scheduledTime ASC")
    List<Pickup> findByStatusOrderByScheduledDateAsc(PickupStatus status);

    @Query("SELECT p FROM Pickup p WHERE p.status = :status AND p.scheduledDate = :date")
    List<Pickup> findByStatusAndScheduledDate(PickupStatus status, LocalDate date);

    @Query("SELECT p FROM Pickup p WHERE p.zone.id = :zoneId ORDER BY p.scheduledDate ASC, p.scheduledTime ASC")
    List<Pickup> findByZoneIdOrderByScheduledDateAsc(Integer zoneId);

    @Query("SELECT p FROM Pickup p WHERE p.status = :status AND p.collector IS NULL ORDER BY p.scheduledDate ASC, p.scheduledTime ASC")
    List<Pickup> findUnassignedPickupsByStatusOrderByScheduledDateAsc(PickupStatus status);

    @Query("SELECT COUNT(p) FROM Pickup p WHERE p.resident.id = :residentId AND p.status = :status")
    long countByResidentIdAndStatus(UUID residentId, PickupStatus status);

    @Query("SELECT COUNT(p) FROM Pickup p WHERE p.collector.id = :collectorId AND p.status = :status")
    long countByCollectorIdAndStatus(UUID collectorId, PickupStatus status);

    @Query("SELECT p FROM Pickup p WHERE p.wasteType = :wasteType ORDER BY p.createdAt DESC")
    List<Pickup> findByWasteTypeOrderByCreatedAtDesc(WasteType wasteType);

    @Query("SELECT COUNT(p) FROM Pickup p WHERE p.status = :status")
    long countByStatus(PickupStatus status);
}
