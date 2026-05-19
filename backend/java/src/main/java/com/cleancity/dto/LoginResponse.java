package com.cleancity.dto;

import com.cleancity.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Login Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String token;
    private String refreshToken;
    private UUID userId;
    private String email;
    private String fullName;
    private UserRole role;
    private String message;
}
