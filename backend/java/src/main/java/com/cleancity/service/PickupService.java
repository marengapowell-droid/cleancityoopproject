package com.cleancity.service;

import com.cleancity.dto.ConfirmPickupRequest;
import com.cleancity.dto.PickupRequest;
import com.cleancity.dto.PickupResponse;
import com.cleancity.entity.*;
import com.cleancity.exception.ResourceNotFoundException;
import com.cleancity.exception.UnauthorizedException;
import com.cleancity.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for Pickup business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PickupService {

    private final PickupRepository pickupRepository;
    private final ResidentRepository residentRepository;
    private final CollectorRepository collectorRepository;
    private final ZoneRepository zoneRepository;

    /**
     * Create a new pickup request
     */
    @Transactional
    public PickupResponse createPickup(PickupRequest request, UUID residentId) {
        log.info("Creating pickup request for resident: {}", residentId);

        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new ResourceNotFoundException("Resident not found"));

        Zone zone = null;
        if (request.getZone() != null) {
            zone = zoneRepository.findByName(request.getZone())
                    .orElse(null);
        }

        Pickup pickup = Pickup.builder()
                .resident(resident)
                .zone(zone)
                .wasteType(request.getWasteType())
                .location(request.getLocation())
                .scheduledDate(request.getScheduledDate())
                .scheduledTime(request.getScheduledTime())
                .status(PickupStatus.SCHEDULED)
                .notes(request.getNotes())
                .imageUrl(request.getImageUrl())
                .build();

        Pickup savedPickup = pickupRepository.save(pickup);
        log.info("Pickup created successfully with ID: {}", savedPickup.getId());

        return mapToResponse(savedPickup);
    }

    /**
     * Confirm/Complete a pickup - CRITICAL FIX FOR PICKUP CONFIRMATION ISSUE
     */
    @Transactional
    public PickupResponse confirmPickup(ConfirmPickupRequest request, UUID collectorId) {
        log.info("Confirming pickup {} by collector {}", request.getPickupId(), collectorId);

        Pickup pickup = pickupRepository.findById(request.getPickupId())
                .orElseThrow(() -> new ResourceNotFoundException("Pickup not found"));

        // Validate pickup status
        if (pickup.getStatus() == PickupStatus.COMPLETED) {
            throw new IllegalStateException("Pickup is already completed");
        }

        if (pickup.getStatus() == PickupStatus.CANCELLED) {
            throw new IllegalStateException("Pickup is cancelled and cannot be confirmed");
        }

        // Get collector
        Collector collector = collectorRepository.findById(collectorId)
                .orElseThrow(() -> new ResourceNotFoundException("Collector not found"));

        // Verify pickup is assigned to this collector
        if (pickup.getCollector() != null && !pickup.getCollector().getId().equals(collectorId)) {
            throw new UnauthorizedException("This pickup is not assigned to you");
        }

        // Update pickup status to COMPLETED
        pickup.setStatus(PickupStatus.COMPLETED);
        pickup.setCollector(collector);
        pickup.setCompletedAt(LocalDateTime.now());
        
        if (request.getNotes() != null) {
            pickup.setNotes(request.getNotes());
        }

        Pickup savedPickup = pickupRepository.save(pickup);
        
        // Update collector statistics
        collector.setCompletedPickups(collector.getCompletedPickups() + 1);
        collectorRepository.save(collector);

        log.info("Pickup {} confirmed successfully by collector {}", savedPickup.getId(), collectorId);

        return mapToResponse(savedPickup);
    }

    /**
     * Assign a pickup to a collector
     */
    @Transactional
    public PickupResponse assignPickup(UUID pickupId, UUID collectorId) {
        log.info("Assigning pickup {} to collector {}", pickupId, collectorId);

        Pickup pickup = pickupRepository.findById(pickupId)
                .orElseThrow(() -> new ResourceNotFoundException("Pickup not found"));

        if (pickup.getStatus() != PickupStatus.SCHEDULED) {
            throw new IllegalStateException("Pickup can only be assigned when in SCHEDULED status");
        }

        Collector collector = collectorRepository.findById(collectorId)
                .orElseThrow(() -> new ResourceNotFoundException("Collector not found"));

        pickup.setCollector(collector);
        pickup.setStatus(PickupStatus.IN_PROGRESS);
        pickup.setAssignedAt(LocalDateTime.now());

        Pickup savedPickup = pickupRepository.save(pickup);
        
        // Update collector statistics
        collector.setAssignedPickups(collector.getAssignedPickups() + 1);
        collectorRepository.save(collector);

        log.info("Pickup {} assigned to collector {}", pickupId, collectorId);

        return mapToResponse(savedPickup);
    }

    /**
     * Get all pickups for a resident
     */
    public List<PickupResponse> getPickupsByResident(UUID residentId) {
        log.info("Fetching pickups for resident: {}", residentId);
        
        List<Pickup> pickups = pickupRepository.findByResidentEmailOrderByCreatedAtDesc(
                residentRepository.findById(residentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Resident not found"))
                        .getUser()
                        .getEmail()
        );

        return pickups.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all pickups assigned to a collector
     */
    public List<PickupResponse> getPickupsByCollector(UUID collectorId) {
        log.info("Fetching pickups for collector: {}", collectorId);
        
        List<Pickup> pickups = pickupRepository.findByCollectorIdOrderByScheduledDateAsc(collectorId);

        return pickups.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get pickup by ID
     */
    public PickupResponse getPickupById(UUID pickupId) {
        log.info("Fetching pickup: {}", pickupId);
        
        Pickup pickup = pickupRepository.findById(pickupId)
                .orElseThrow(() -> new ResourceNotFoundException("Pickup not found"));

        return mapToResponse(pickup);
    }

    /**
     * Get all pickups (admin only)
     */
    public List<PickupResponse> getAllPickups() {
        log.info("Fetching all pickups");
        
        List<Pickup> pickups = pickupRepository.findAll();

        return pickups.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get pickups by status
     */
    public List<PickupResponse> getPickupsByStatus(PickupStatus status) {
        log.info("Fetching pickups with status: {}", status);
        
        List<Pickup> pickups = pickupRepository.findByStatusOrderByScheduledDateAsc(status);

        return pickups.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Cancel a pickup
     */
    @Transactional
    public PickupResponse cancelPickup(UUID pickupId, UUID userId) {
        log.info("Cancelling pickup: {}", pickupId);

        Pickup pickup = pickupRepository.findById(pickupId)
                .orElseThrow(() -> new ResourceNotFoundException("Pickup not found"));

        if (pickup.getStatus() == PickupStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed pickup");
        }

        pickup.setStatus(PickupStatus.CANCELLED);
        Pickup savedPickup = pickupRepository.save(pickup);

        log.info("Pickup {} cancelled successfully", pickupId);

        return mapToResponse(savedPickup);
    }

    /**
     * Map Pickup entity to PickupResponse DTO
     */
    private PickupResponse mapToResponse(Pickup pickup) {
        return PickupResponse.builder()
                .id(pickup.getId())
                .residentId(pickup.getResident().getId())
                .residentName(pickup.getResident().getUser().getFullName())
                .collectorId(pickup.getCollector() != null ? pickup.getCollector().getId() : null)
                .collectorName(pickup.getCollector() != null ? pickup.getCollector().getUser().getFullName() : null)
                .zoneId(pickup.getZone() != null ? pickup.getZone().getId() : null)
                .zoneName(pickup.getZone() != null ? pickup.getZone().getName() : null)
                .wasteType(pickup.getWasteType())
                .location(pickup.getLocation())
                .scheduledDate(pickup.getScheduledDate())
                .scheduledTime(pickup.getScheduledTime())
                .status(pickup.getStatus())
                .notes(pickup.getNotes())
                .imageUrl(pickup.getImageUrl())
                .createdAt(pickup.getCreatedAt())
                .assignedAt(pickup.getAssignedAt())
                .completedAt(pickup.getCompletedAt())
                .build();
    }
}
