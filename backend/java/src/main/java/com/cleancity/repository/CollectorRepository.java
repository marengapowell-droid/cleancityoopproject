package com.cleancity.repository;

import com.cleancity.entity.Collector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Collector entity
 */
@Repository
public interface CollectorRepository extends JpaRepository<Collector, UUID> {

    Optional<Collector> findByUser_Email(String email);

    @Query("SELECT c FROM Collector c WHERE c.zone = :zone AND c.isAvailable = true")
    List<Collector> findByZoneAndIsAvailable(String zone);

    @Query("SELECT c FROM Collector c WHERE c.zone = :zone ORDER BY c.completedPickups DESC")
    List<Collector> findByZoneOrderByCompletedPickupsDesc(String zone);

    @Query("SELECT c FROM Collector c WHERE c.zone = :zone AND c.isAvailable = true ORDER BY c.completedPickups DESC")
    List<Collector> findByZoneAndIsAvailableOrderByCompletedPickupsDesc(String zone);

    @Query("SELECT COUNT(c) FROM Collector c WHERE c.zone = :zone AND c.isAvailable = true")
    long countByZoneAndIsAvailable(String zone);

    @Query("SELECT c FROM Collector c WHERE c.isAvailable = true")
    List<Collector> findAllAvailable();
}
