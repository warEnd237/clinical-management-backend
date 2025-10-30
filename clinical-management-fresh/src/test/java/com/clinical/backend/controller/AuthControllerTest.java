package com.clinical.backend.controller;

import com.clinical.backend.dto.auth.AuthResponse;
import com.clinical.backend.dto.auth.LoginRequest;
import com.clinical.backend.dto.auth.RefreshTokenRequest;
import com.clinical.backend.entity.User;
import com.clinical.backend.enums.UserRole;
import com.clinical.backend.security.CustomUserDetails;
import com.clinical.backend.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@DisplayName("Auth Controller Integration Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    private ObjectMapper objectMapper;
    private LoginRequest loginRequest;
    private RefreshTokenRequest refreshTokenRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        // Setup login request
        loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@test.com");
        loginRequest.setPassword("Admin@123");

        // Setup refresh token request
        refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken("valid-refresh-token");

        // Setup auth response
        authResponse = AuthResponse.builder()
                .accessToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
                .refreshToken("refresh-token-here")
                .user(AuthResponse.UserInfo.builder()
                        .id(1L)
                        .email("admin@test.com")
                        .fullName("Admin User")
                        .role("ADMIN")
                        .build())
                .build();
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void testLoginSuccess() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andExpect(jsonPath("$.data.user.id").value(1))
                .andExpect(jsonPath("$.data.user.fullName").value("Admin User"))
                .andExpect(jsonPath("$.data.user.role").value("ADMIN"));

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should return error for invalid credentials")
    void testLoginInvalidCredentials() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isInternalServerError());

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("Should return validation error for empty email")
    void testLoginEmptyEmail() throws Exception {
        // Arrange
        loginRequest.setEmail("");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any());
    }

    @Test
    @DisplayName("Should return validation error for empty password")
    void testLoginEmptyPassword() throws Exception {
        // Arrange
        loginRequest.setPassword("");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any());
    }

    @Test
    @DisplayName("Should refresh token successfully")
    void testRefreshTokenSuccess() throws Exception {
        // Arrange
        AuthResponse refreshedResponse = AuthResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .user(AuthResponse.UserInfo.builder()
                        .id(1L)
                        .build())
                .build();

        when(authService.refreshToken(any(RefreshTokenRequest.class)))
                .thenReturn(refreshedResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Token refreshed"))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("new-refresh-token"));

        verify(authService, times(1)).refreshToken(any(RefreshTokenRequest.class));
    }

    @Test
    @DisplayName("Should return error for invalid refresh token")
    void testRefreshTokenInvalid() throws Exception {
        // Arrange
        when(authService.refreshToken(any(RefreshTokenRequest.class)))
                .thenThrow(new RuntimeException("Invalid refresh token"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should logout successfully when authenticated")
    void testLogoutSuccess() throws Exception {
        // Arrange
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("admin@test.com");
        testUser.setFullName("Admin User");
        testUser.setRole(UserRole.ADMIN);

        CustomUserDetails userDetails = CustomUserDetails.fromUser(testUser);

        doNothing().when(authService).logout(anyLong());

        // Act & Assert
        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf())
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logout successful"));

        verify(authService, times(1)).logout(1L);
    }

    @Test
    @DisplayName("Should handle logout when not authenticated")
    void testLogoutNotAuthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Already logged out"));

        verify(authService, never()).logout(anyLong());
    }

    @Test
    @DisplayName("Should require CSRF token for login")
    void testLoginRequiresCsrf() throws Exception {
        // Act & Assert - Without CSRF token
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should accept valid email format")
    void testValidEmailFormat() throws Exception {
        // Arrange
        loginRequest.setEmail("valid.email@example.com");
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return different user roles correctly")
    void testDifferentUserRoles() throws Exception {
        // Test DOCTOR role
        AuthResponse doctorResponse = AuthResponse.builder()
                .accessToken("doctor-token")
                .user(AuthResponse.UserInfo.builder()
                        .role("DOCTOR")
                        .build())
                .build();

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(doctorResponse);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.role").value("DOCTOR"));

        // Test SECRETARY role
        AuthResponse secretaryResponse = AuthResponse.builder()
                .accessToken("secretary-token")
                .user(AuthResponse.UserInfo.builder()
                        .role("SECRETARY")
                        .build())
                .build();

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(secretaryResponse);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.role").value("SECRETARY"));
    }
}
