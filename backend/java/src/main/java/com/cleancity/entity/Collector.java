package com.cleancity.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Collector Entity - Extended user data for collectors
 */
@Entity
@Table(name = "collectors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Collector {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    @Column(name = "zone", nullable = false, length = 100)
    private String zone;

    @Column(name = "vehicle_number", length = 50)
    private String vehicleNumber;

    @Column(name = "license_number", length = 50)
    private String licenseNumber;

    @Column(name = "assigned_pickups")
    @Builder.Default
    private Integer assignedPickups = 0;

    @Column(name = "completed_pickups")
    @Builder.Default
    private Integer completedPickups = 0;

    @Column(name = "rating", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "is_available")
    @Builder.Default
    private Boolean isAvailable = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
