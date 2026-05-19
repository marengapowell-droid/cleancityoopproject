package com.cleancity.controller;

import com.cleancity.dto.ApiResponse;
import com.cleancity.dto.LoginRequest;
import com.cleancity.dto.LoginResponse;
import com.cleancity.entity.User;
import com.cleancity.entity.UserRole;
import com.cleancity.repository.UserRepository;
import com.cleancity.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Authentication API endpoints
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication APIs")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        
        log.info("POST /auth/login - Login attempt for email: {}", request.getEmail());
        
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!user.getIsActive()) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("Account is inactive"));
            }

            String token = tokenProvider.generateToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(user.getId());

            LoginResponse response = LoginResponse.builder()
                    .token(token)
                    .refreshToken(refreshToken)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .role(user.getRole())
                    .message("Login successful")
                    .build();

            log.info("Login successful for user: {}", user.getEmail());

            return ResponseEntity.ok(ApiResponse.success("Login successful", response));

        } catch (Exception e) {
            log.error("Login failed for email: {}", request.getEmail(), e);
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid email or password"));
        }
    }

    @PostMapping("/register/resident")
    @Operation(summary = "Register as resident", description = "Create a new resident account")
    public ResponseEntity<ApiResponse<LoginResponse>> registerResident(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String area) {
        
        log.info("POST /auth/register/resident - Registration attempt for email: {}", email);
        
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Email already registered"));
        }

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .fullName(fullName)
                .phone(phone)
                .role(UserRole.RESIDENT)
                .isActive(true)
                .isVerified(false)
                .build();

        User savedUser = userRepository.save(user);

        // Create resident profile
        // Note: You would need to inject ResidentRepository and create the resident entity here

        log.info("Resident registration successful for email: {}", email);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", null));
    }

    @PostMapping("/register/collector")
    @Operation(summary = "Register as collector", description = "Create a new collector account")
    public ResponseEntity<ApiResponse<LoginResponse>> registerCollector(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false) String phone,
            @RequestParam String zone) {
        
        log.info("POST /auth/register/collector - Registration attempt for email: {}", email);
        
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Email already registered"));
        }

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .fullName(fullName)
                .phone(phone)
                .role(UserRole.COLLECTOR)
                .isActive(true)
                .isVerified(false)
                .build();

        User savedUser = userRepository.save(user);

        // Create collector profile
        // Note: You would need to inject CollectorRepository and create the collector entity here

        log.info("Collector registration successful for email: {}", email);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", null));
    }
}
