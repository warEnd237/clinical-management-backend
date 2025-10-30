package com.clinical.backend.service;

import com.clinical.backend.dto.auth.AuthResponse;
import com.clinical.backend.dto.auth.LoginRequest;
import com.clinical.backend.entity.User;
import com.clinical.backend.enums.UserRole;
import com.clinical.backend.repository.UserRepository;
import com.clinical.backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Authentication Service Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("admin@test.com");
        testUser.setPasswordHash("$2a$10$hashedpassword");
        testUser.setFullName("Test Admin");
        testUser.setRole(UserRole.ADMIN);
        testUser.setIsActive(true);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@test.com");
        loginRequest.setPassword("password123");
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void testLoginSuccess() {
        // Arrange
        String accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
        String refreshToken = "refresh_token_here";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateAccessToken(anyString(), anyString())).thenReturn(accessToken);
        when(jwtUtil.generateRefreshToken(anyString())).thenReturn(refreshToken);

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
        assertEquals("admin@test.com", response.getEmail());
        assertEquals("ADMIN", response.getRole());
        verify(authenticationManager, times(1)).authenticate(any());
    }

    @Test
    @DisplayName("Should throw exception with invalid credentials")
    void testLoginInvalidCredentials() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        verify(jwtUtil, never()).generateAccessToken(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw exception when user not found after authentication")
    void testLoginUserNotFound() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });
    }

    @Test
    @DisplayName("Should throw exception when user is inactive")
    void testLoginInactiveUser() {
        // Arrange
        testUser.setIsActive(false);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertTrue(exception.getMessage().contains("inactive") || 
                   exception.getMessage().contains("disabled"));
    }

    @Test
    @DisplayName("Should refresh access token successfully")
    void testRefreshTokenSuccess() {
        // Arrange
        String oldRefreshToken = "old_refresh_token";
        String newAccessToken = "new_access_token";
        String userEmail = "admin@test.com";

        when(jwtUtil.validateRefreshToken(oldRefreshToken)).thenReturn(true);
        when(jwtUtil.extractEmail(oldRefreshToken)).thenReturn(userEmail);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateAccessToken(userEmail, "ADMIN")).thenReturn(newAccessToken);

        // Act
        AuthResponse response = authService.refreshToken(oldRefreshToken);

        // Assert
        assertNotNull(response);
        assertEquals(newAccessToken, response.getAccessToken());
        assertEquals(userEmail, response.getEmail());
    }

    @Test
    @DisplayName("Should throw exception with invalid refresh token")
    void testRefreshTokenInvalid() {
        // Arrange
        String invalidToken = "invalid_token";

        when(jwtUtil.validateRefreshToken(invalidToken)).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            authService.refreshToken(invalidToken);
        });

        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("Should generate different roles based on user type")
    void testRoleGeneration() {
        // Test DOCTOR role
        testUser.setRole(UserRole.DOCTOR);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateAccessToken(anyString(), anyString())).thenReturn("token");
        when(jwtUtil.generateRefreshToken(anyString())).thenReturn("refresh");

        AuthResponse doctorResponse = authService.login(loginRequest);
        assertEquals("DOCTOR", doctorResponse.getRole());

        // Test SECRETARY role
        testUser.setRole(UserRole.SECRETARY);
        AuthResponse secretaryResponse = authService.login(loginRequest);
        assertEquals("SECRETARY", secretaryResponse.getRole());
    }

    @Test
    @DisplayName("Should logout successfully")
    void testLogout() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // Act
        assertDoesNotThrow(() -> {
            authService.logout(testUser);
        });

        // Assert - Logout should complete without exceptions
        // In a real implementation, you might verify token invalidation
    }
}
