package com.cleancity.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Pickup Entity - Waste pickup requests
 */
@Entity
@Table(name = "pickups")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pickup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id", nullable = false)
    private Resident resident;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collector_id")
    private Collector collector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    @Enumerated(EnumType.STRING)
    @Column(name = "waste_type", nullable = false)
    private WasteType wasteType;

    @Column(name = "location", nullable = false, columnDefinition = "TEXT")
    private String location;

    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "scheduled_time", nullable = false)
    private LocalTime scheduledTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private PickupStatus status = PickupStatus.SCHEDULED;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "image_url")
    private String imageUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
