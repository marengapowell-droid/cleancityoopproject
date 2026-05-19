package com.cleancity.repository;

import com.cleancity.entity.Resident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Resident entity
 */
@Repository
public interface ResidentRepository extends JpaRepository<Resident, UUID> {

    Optional<Resident> findByUser_Email(String email);

    @Query("SELECT r FROM Resident r WHERE r.area = :area")
    List<Resident> findByArea(String area);

    @Query("SELECT r FROM Resident r WHERE r.area = :area ORDER BY r.pickupCount DESC")
    List<Resident> findByAreaOrderByPickupCountDesc(String area);

    @Query("SELECT COUNT(r) FROM Resident r WHERE r.area = :area")
    long countByArea(String area);
}
