package com.clinical.backend.service;

import com.clinical.backend.entity.AuditLog;
import com.clinical.backend.entity.User;
import com.clinical.backend.enums.UserRole;
import com.clinical.backend.repository.AuditLogRepository;
import com.clinical.backend.repository.UserRepository;
import com.clinical.backend.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Audit Service Tests")
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuditService auditService;

    private User testUser;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("admin@test.com");
        testUser.setFullName("Admin User");
        testUser.setRole(UserRole.ADMIN);
        testUser.setPasswordHash("hashed-password");
        testUser.setIsActive(true);

        // Create user details
        userDetails = CustomUserDetails.fromUser(testUser);

        // Setup security context
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Should log access action successfully")
    void testLogAccessSuccess() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        auditService.logAccess("Patient", 123L, "READ");

        // Assert
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("Should log create action successfully")
    void testLogCreateSuccess() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog saved = invocation.getArgument(0);
            assert saved.getAction().equals("CREATE");
            assert saved.getEntityType().equals("Appointment");
            assert saved.getEntityId().equals(456L);
            return saved;
        });

        // Act
        auditService.logCreate("Appointment", 456L);

        // Assert
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("Should log update action successfully")
    void testLogUpdateSuccess() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog saved = invocation.getArgument(0);
            assert saved.getAction().equals("UPDATE");
            return saved;
        });

        // Act
        auditService.logUpdate("Patient", 789L);

        // Assert
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("Should log delete action successfully")
    void testLogDeleteSuccess() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog saved = invocation.getArgument(0);
            assert saved.getAction().equals("DELETE");
            return saved;
        });

        // Act
        auditService.logDelete("Invoice", 999L);

        // Assert
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("Should not throw exception when authentication is null")
    void testLogWithNullAuthentication() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act & Assert - Should not throw exception
        auditService.logCreate("Patient", 1L);

        // No audit log should be saved
        verify(auditLogRepository, never()).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("Should not throw exception when principal is not CustomUserDetails")
    void testLogWithInvalidPrincipal() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("not-a-user-details-object");

        // Act & Assert - Should not throw exception
        auditService.logCreate("Patient", 1L);

        // No audit log should be saved
        verify(auditLogRepository, never()).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("Should handle user not found gracefully")
    void testLogWithUserNotFound() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert - Should not throw exception
        auditService.logCreate("Patient", 1L);

        // Audit log is still created with null user
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("Should log different entity types correctly")
    void testLogDifferentEntityTypes() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        auditService.logCreate("Patient", 1L);
        auditService.logCreate("Appointment", 2L);
        auditService.logCreate("Invoice", 3L);
        auditService.logCreate("Prescription", 4L);

        // Assert
        verify(auditLogRepository, times(4)).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("Should create audit log with correct action values")
    void testAuditLogActionValues() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Test CREATE
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog saved = invocation.getArgument(0);
            assert saved.getAction().equals("CREATE");
            return saved;
        });
        auditService.logCreate("Test", 1L);

        // Test UPDATE
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog saved = invocation.getArgument(0);
            assert saved.getAction().equals("UPDATE");
            return saved;
        });
        auditService.logUpdate("Test", 1L);

        // Test DELETE
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog saved = invocation.getArgument(0);
            assert saved.getAction().equals("DELETE");
            return saved;
        });
        auditService.logDelete("Test", 1L);

        // Assert all were called
        verify(auditLogRepository, times(3)).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("Should handle exceptions silently without disrupting flow")
    void testExceptionHandling() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findById(anyLong())).thenThrow(new RuntimeException("Database error"));

        // Act & Assert - Should not throw exception
        auditService.logCreate("Patient", 1L);

        // No exception should be thrown to caller
        verify(userRepository, times(1)).findById(anyLong());
    }
}
