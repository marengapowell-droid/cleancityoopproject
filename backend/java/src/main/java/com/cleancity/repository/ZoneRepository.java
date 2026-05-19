package com.cleancity.repository;

import com.cleancity.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Zone entity
 */
@Repository
public interface ZoneRepository extends JpaRepository<Zone, Integer> {

    Optional<Zone> findByName(String name);

    List<Zone> findByActiveTrue();

    List<Zone> findByActiveTrueOrderByNameAsc();
}
