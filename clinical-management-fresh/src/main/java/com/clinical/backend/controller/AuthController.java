package com.clinical.backend.controller;

import com.clinical.backend.dto.auth.AuthResponse;
import com.clinical.backend.dto.auth.LoginRequest;
import com.clinical.backend.dto.auth.RefreshTokenRequest;
import com.clinical.backend.dto.common.ApiResponse;
import com.clinical.backend.security.CustomUserDetails;
import com.clinical.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Get new access token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse authResponse = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", authResponse));
    }
    
    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Invalidate refresh token")
    public ResponseEntity<ApiResponse<Void>> logout(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.ok(ApiResponse.success("Already logged out", null));
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        authService.logout(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }
}
