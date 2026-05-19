package com.cleancity.controller;

import com.cleancity.dto.ApiResponse;
import com.cleancity.dto.ConfirmPickupRequest;
import com.cleancity.dto.PickupRequest;
import com.cleancity.dto.PickupResponse;
import com.cleancity.entity.PickupStatus;
import com.cleancity.service.PickupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller for Pickup API endpoints
 */
@RestController
@RequestMapping("/pickups")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pickups", description = "Pickup management APIs")
@SecurityRequirement(name = "bearerAuth")
public class PickupController {

    private final PickupService pickupService;

    @PostMapping
    @PreAuthorize("hasRole('RESIDENT')")
    @Operation(summary = "Create a new pickup request", description = "Residents can create pickup requests")
    public ResponseEntity<ApiResponse<PickupResponse>> createPickup(
            @Valid @RequestBody PickupRequest request,
            Authentication authentication) {
        
        UUID userId = UUID.fromString(authentication.getName());
        log.info("POST /pickups - Creating pickup for user: {}", userId);
        
        PickupResponse response = pickupService.createPickup(request, userId);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Pickup created successfully", response));
    }

    @PostMapping("/confirm")
    @PreAuthorize("hasRole('COLLECTOR') or hasRole('ADMIN')")
    @Operation(summary = "Confirm/Complete a pickup", description = "Collectors and admins can confirm pickups")
    public ResponseEntity<ApiResponse<PickupResponse>> confirmPickup(
            @Valid @RequestBody ConfirmPickupRequest request,
            Authentication authentication) {
        
        UUID collectorId = UUID.fromString(authentication.getName());
        log.info("POST /pickups/confirm - Confirming pickup {} by collector {}", 
                request.getPickupId(), collectorId);
        
        PickupResponse response = pickupService.confirmPickup(request, collectorId);
        
        return ResponseEntity
                .ok(ApiResponse.success("Pickup confirmed successfully", response));
    }

    @PatchMapping("/{pickupId}/assign/{collectorId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign pickup to collector", description = "Admins can assign pickups to collectors")
    public ResponseEntity<ApiResponse<PickupResponse>> assignPickup(
            @Parameter(description = "Pickup ID") @PathVariable UUID pickupId,
            @Parameter(description = "Collector ID") @PathVariable UUID collectorId) {
        
        log.info("PATCH /pickups/{}/assign/{} - Assigning pickup to collector", pickupId, collectorId);
        
        PickupResponse response = pickupService.assignPickup(pickupId, collectorId);
        
        return ResponseEntity
                .ok(ApiResponse.success("Pickup assigned successfully", response));
    }

    @GetMapping("/my-pickups")
    @PreAuthorize("hasRole('RESIDENT')")
    @Operation(summary = "Get my pickups", description = "Residents can view their own pickups")
    public ResponseEntity<ApiResponse<List<PickupResponse>>> getMyPickups(
            Authentication authentication) {
        
        UUID userId = UUID.fromString(authentication.getName());
        log.info("GET /pickups/my-pickups - Fetching pickups for user: {}", userId);
        
        List<PickupResponse> pickups = pickupService.getPickupsByResident(userId);
        
        return ResponseEntity
                .ok(ApiResponse.success(pickups));
    }

    @GetMapping("/assigned")
    @PreAuthorize("hasRole('COLLECTOR')")
    @Operation(summary = "Get assigned pickups", description = "Collectors can view their assigned pickups")
    public ResponseEntity<ApiResponse<List<PickupResponse>>> getAssignedPickups(
            Authentication authentication) {
        
        UUID collectorId = UUID.fromString(authentication.getName());
        log.info("GET /pickups/assigned - Fetching pickups for collector: {}", collectorId);
        
        List<PickupResponse> pickups = pickupService.getPickupsByCollector(collectorId);
        
        return ResponseEntity
                .ok(ApiResponse.success(pickups));
    }

    @GetMapping("/{pickupId}")
    @Operation(summary = "Get pickup by ID", description = "Get details of a specific pickup")
    public ResponseEntity<ApiResponse<PickupResponse>> getPickupById(
            @Parameter(description = "Pickup ID") @PathVariable UUID pickupId) {
        
        log.info("GET /pickups/{} - Fetching pickup details", pickupId);
        
        PickupResponse pickup = pickupService.getPickupById(pickupId);
        
        return ResponseEntity
                .ok(ApiResponse.success(pickup));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all pickups", description = "Admins can view all pickups")
    public ResponseEntity<ApiResponse<List<PickupResponse>>> getAllPickups() {
        
        log.info("GET /pickups - Fetching all pickups");
        
        List<PickupResponse> pickups = pickupService.getAllPickups();
        
        return ResponseEntity
                .ok(ApiResponse.success(pickups));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get pickups by status", description = "Admins can filter pickups by status")
    public ResponseEntity<ApiResponse<List<PickupResponse>>> getPickupsByStatus(
            @Parameter(description = "Pickup status") @PathVariable PickupStatus status) {
        
        log.info("GET /pickups/status/{} - Fetching pickups with status: {}", status, status);
        
        List<PickupResponse> pickups = pickupService.getPickupsByStatus(status);
        
        return ResponseEntity
                .ok(ApiResponse.success(pickups));
    }

    @PatchMapping("/{pickupId}/cancel")
    @PreAuthorize("hasRole('RESIDENT') or hasRole('ADMIN')")
    @Operation(summary = "Cancel a pickup", description = "Residents and admins can cancel pickups")
    public ResponseEntity<ApiResponse<PickupResponse>> cancelPickup(
            @Parameter(description = "Pickup ID") @PathVariable UUID pickupId,
            Authentication authentication) {
        
        UUID userId = UUID.fromString(authentication.getName());
        log.info("PATCH /pickups/{}/cancel - Cancelling pickup by user: {}", pickupId, userId);
        
        PickupResponse response = pickupService.cancelPickup(pickupId, userId);
        
        return ResponseEntity
                .ok(ApiResponse.success("Pickup cancelled successfully", response));
    }
}
