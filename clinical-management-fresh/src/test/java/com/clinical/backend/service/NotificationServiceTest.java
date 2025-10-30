package com.clinical.backend.service;

import com.clinical.backend.entity.Appointment;
import com.clinical.backend.entity.Doctor;
import com.clinical.backend.entity.Notification;
import com.clinical.backend.entity.Patient;
import com.clinical.backend.entity.User;
import com.clinical.backend.enums.AppointmentStatus;
import com.clinical.backend.enums.UserRole;
import com.clinical.backend.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Notification Service Tests")
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Captor
    private ArgumentCaptor<Notification> notificationCaptor;

    private Patient testPatient;
    private Doctor testDoctor;
    private User patientUser;
    private User doctorUser;
    private Appointment testAppointment;

    @BeforeEach
    void setUp() {
        // Create patient user
        patientUser = new User();
        patientUser.setId(1L);
        patientUser.setEmail("patient@test.com");
        patientUser.setFullName("John Doe");
        patientUser.setRole(UserRole.SECRETARY); // Patient users would typically be SECRETARY role

        // Create doctor user
        doctorUser = new User();
        doctorUser.setId(2L);
        doctorUser.setEmail("doctor@test.com");
        doctorUser.setFullName("Dr. Sarah Johnson");
        doctorUser.setRole(UserRole.DOCTOR);

        // Create test patient
        testPatient = Patient.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .email("john.doe@test.com")
                .user(patientUser)
                .build();

        // Create test doctor
        testDoctor = Doctor.builder()
                .id(1L)
                .user(doctorUser)
                .specialty("Cardiology")
                .licenseNumber("DOC123")
                .build();

        // Create test appointment
        testAppointment = Appointment.builder()
                .id(1L)
                .patient(testPatient)
                .doctor(testDoctor)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .status(AppointmentStatus.SCHEDULED)
                .reason("Regular checkup")
                .build();

        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Should notify both patient and doctor when appointment created")
    void testNotifyAppointmentCreated() {
        // Act
        notificationService.notifyAppointmentCreated(testAppointment);

        // Assert
        verify(notificationRepository, times(2)).save(notificationCaptor.capture());

        var notifications = notificationCaptor.getAllValues();
        assertEquals(2, notifications.size());

        // Verify patient notification
        Notification patientNotification = notifications.stream()
                .filter(n -> n.getUser().equals(patientUser))
                .findFirst()
                .orElseThrow();

        assertEquals("APPOINTMENT_CREATED", patientNotification.getType());
        assertEquals("Appointment Scheduled", patientNotification.getTitle());
        assertTrue(patientNotification.getMessage().contains("Dr. Sarah Johnson"));
        assertEquals("/appointments/1", patientNotification.getLink());
        assertFalse(patientNotification.getIsRead());

        // Verify doctor notification
        Notification doctorNotification = notifications.stream()
                .filter(n -> n.getUser().equals(doctorUser))
                .findFirst()
                .orElseThrow();

        assertEquals("APPOINTMENT_CREATED", doctorNotification.getType());
        assertEquals("New Appointment", doctorNotification.getTitle());
        assertTrue(doctorNotification.getMessage().contains("John Doe"));
        assertEquals("/appointments/1", doctorNotification.getLink());
        assertFalse(doctorNotification.getIsRead());
    }

    @Test
    @DisplayName("Should notify both patient and doctor when appointment cancelled")
    void testNotifyAppointmentCancelled() {
        // Act
        notificationService.notifyAppointmentCancelled(testAppointment);

        // Assert
        verify(notificationRepository, times(2)).save(notificationCaptor.capture());

        var notifications = notificationCaptor.getAllValues();
        assertEquals(2, notifications.size());

        // Verify patient notification
        Notification patientNotification = notifications.stream()
                .filter(n -> n.getUser().equals(patientUser))
                .findFirst()
                .orElseThrow();

        assertEquals("APPOINTMENT_CANCELLED", patientNotification.getType());
        assertEquals("Appointment Cancelled", patientNotification.getTitle());
        assertTrue(patientNotification.getMessage().contains("has been cancelled"));
        assertEquals("/appointments", patientNotification.getLink());

        // Verify doctor notification
        Notification doctorNotification = notifications.stream()
                .filter(n -> n.getUser().equals(doctorUser))
                .findFirst()
                .orElseThrow();

        assertEquals("APPOINTMENT_CANCELLED", doctorNotification.getType());
        assertTrue(doctorNotification.getMessage().contains("John Doe"));
    }

    @Test
    @DisplayName("Should create notifications with isRead=false by default")
    void testNotificationsCreatedAsUnread() {
        // Act
        notificationService.notifyAppointmentCreated(testAppointment);

        // Assert
        verify(notificationRepository, times(2)).save(notificationCaptor.capture());

        notificationCaptor.getAllValues().forEach(notification -> {
            assertFalse(notification.getIsRead());
        });
    }

    @Test
    @DisplayName("Should include appointment link in created notifications")
    void testCreatedNotificationsIncludeAppointmentLink() {
        // Act
        notificationService.notifyAppointmentCreated(testAppointment);

        // Assert
        verify(notificationRepository, times(2)).save(notificationCaptor.capture());

        notificationCaptor.getAllValues().forEach(notification -> {
            assertEquals("/appointments/1", notification.getLink());
        });
    }

    @Test
    @DisplayName("Should include appointments root link in cancelled notifications")
    void testCancelledNotificationsIncludeAppointmentsLink() {
        // Act
        notificationService.notifyAppointmentCancelled(testAppointment);

        // Assert
        verify(notificationRepository, times(2)).save(notificationCaptor.capture());

        notificationCaptor.getAllValues().forEach(notification -> {
            assertEquals("/appointments", notification.getLink());
        });
    }

    @Test
    @DisplayName("Should include patient name in doctor notification")
    void testDoctorNotificationIncludesPatientName() {
        // Act
        notificationService.notifyAppointmentCreated(testAppointment);

        // Assert
        verify(notificationRepository, times(2)).save(notificationCaptor.capture());

        Notification doctorNotification = notificationCaptor.getAllValues().stream()
                .filter(n -> n.getUser().equals(doctorUser))
                .findFirst()
                .orElseThrow();

        assertTrue(doctorNotification.getMessage().contains("John"));
        assertTrue(doctorNotification.getMessage().contains("Doe"));
    }

    @Test
    @DisplayName("Should include doctor name in patient notification")
    void testPatientNotificationIncludesDoctorName() {
        // Act
        notificationService.notifyAppointmentCreated(testAppointment);

        // Assert
        verify(notificationRepository, times(2)).save(notificationCaptor.capture());

        Notification patientNotification = notificationCaptor.getAllValues().stream()
                .filter(n -> n.getUser().equals(patientUser))
                .findFirst()
                .orElseThrow();

        assertTrue(patientNotification.getMessage().contains("Dr. Sarah Johnson"));
    }

    @Test
    @DisplayName("Should include appointment time in notification messages")
    void testNotificationsIncludeAppointmentTime() {
        // Act
        notificationService.notifyAppointmentCreated(testAppointment);

        // Assert
        verify(notificationRepository, times(2)).save(notificationCaptor.capture());

        notificationCaptor.getAllValues().forEach(notification -> {
            assertTrue(notification.getMessage().contains(testAppointment.getStartTime().toString()));
        });
    }

    @Test
    @DisplayName("Should use correct notification types")
    void testCorrectNotificationTypes() {
        // Act - Create
        notificationService.notifyAppointmentCreated(testAppointment);

        // Assert - Create
        verify(notificationRepository, times(2)).save(notificationCaptor.capture());
        notificationCaptor.getAllValues().forEach(notification -> {
            assertEquals("APPOINTMENT_CREATED", notification.getType());
        });

        // Reset
        reset(notificationRepository);
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act - Cancel
        notificationService.notifyAppointmentCancelled(testAppointment);

        // Assert - Cancel
        verify(notificationRepository, times(2)).save(notificationCaptor.capture());
        notificationCaptor.getAllValues().forEach(notification -> {
            assertEquals("APPOINTMENT_CANCELLED", notification.getType());
        });
    }
}
