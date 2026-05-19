package com.cleancity.repository;

import com.cleancity.entity.User;
import com.cleancity.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isActive = true")
    java.util.List<User> findByRoleAndIsActive(UserRole role);

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isActive = true ORDER BY u.createdAt DESC")
    java.util.List<User> findByRoleOrderByCreatedAtDesc(UserRole role);
}
