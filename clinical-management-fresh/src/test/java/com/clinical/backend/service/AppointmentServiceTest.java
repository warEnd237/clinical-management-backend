package com.clinical.backend.service;

import com.clinical.backend.dto.appointment.AppointmentRequest;
import com.clinical.backend.dto.appointment.AppointmentResponse;
import com.clinical.backend.entity.Appointment;
import com.clinical.backend.entity.Doctor;
import com.clinical.backend.entity.Patient;
import com.clinical.backend.entity.User;
import com.clinical.backend.enums.AppointmentStatus;
import com.clinical.backend.enums.UserRole;
import com.clinical.backend.repository.AppointmentRepository;
import com.clinical.backend.repository.DoctorRepository;
import com.clinical.backend.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Appointment Service Tests")
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private Patient testPatient;
    private Doctor testDoctor;
    private User testDoctorUser;
    private Appointment testAppointment;

    @BeforeEach
    void setUp() {
        // Create test doctor user
        testDoctorUser = new User();
        testDoctorUser.setId(1L);
        testDoctorUser.setEmail("doctor@test.com");
        testDoctorUser.setFullName("Dr. Test Doctor");
        testDoctorUser.setRole(UserRole.DOCTOR);

        // Create test doctor
        testDoctor = Doctor.builder()
                .id(1L)
                .user(testDoctorUser)
                .specialty("Cardiology")
                .licenseNumber("TEST123")
                .build();

        // Create test patient
        testPatient = Patient.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .phone("+123456789")
                .email("john.doe@test.com")
                .build();

        // Create test appointment
        testAppointment = Appointment.builder()
                .id(1L)
                .patient(testPatient)
                .doctor(testDoctor)
                .startTime(LocalDateTime.now().plusDays(2))
                .endTime(LocalDateTime.now().plusDays(2).plusHours(1))
                .status(AppointmentStatus.SCHEDULED)
                .reason("Regular checkup")
                .build();
    }

    @Test
    @DisplayName("Should create appointment successfully when no conflicts")
    void testCreateAppointmentSuccess() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.now().plusDays(2);
        LocalDateTime endTime = startTime.plusHours(1);

        AppointmentRequest request = new AppointmentRequest();
        request.setPatientId(1L);
        request.setDoctorId(1L);
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setReason("Regular checkup");

        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));
        when(appointmentRepository.findDoctorAppointmentsBetween(anyLong(), any(), any()))
                .thenReturn(new ArrayList<>());
        when(appointmentRepository.findByPatient(any())).thenReturn(new ArrayList<>());
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);

        // Act
        AppointmentResponse response = appointmentService.createAppointment(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Regular checkup", response.getReason());
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should throw exception when doctor has conflicting appointment")
    void testCreateAppointmentWithDoctorConflict() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.now().plusDays(2);
        LocalDateTime endTime = startTime.plusHours(1);

        AppointmentRequest request = new AppointmentRequest();
        request.setPatientId(1L);
        request.setDoctorId(1L);
        request.setStartTime(startTime);
        request.setEndTime(endTime);

        List<Appointment> conflictingAppointments = List.of(testAppointment);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));
        when(appointmentRepository.findDoctorAppointmentsBetween(anyLong(), any(), any()))
                .thenReturn(conflictingAppointments);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            appointmentService.createAppointment(request);
        });

        assertTrue(exception.getMessage().contains("not available"));
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should throw exception when patient exceeds daily appointment limit")
    void testCreateAppointmentExceedsDailyLimit() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.now().plusDays(2).with(LocalTime.of(14, 0));
        LocalDateTime endTime = startTime.plusHours(1);

        AppointmentRequest request = new AppointmentRequest();
        request.setPatientId(1L);
        request.setDoctorId(1L);
        request.setStartTime(startTime);
        request.setEndTime(endTime);

        // Patient already has appointment same day
        Appointment existingAppointment = Appointment.builder()
                .id(2L)
                .patient(testPatient)
                .doctor(testDoctor)
                .startTime(startTime.with(LocalTime.of(10, 0)))
                .endTime(startTime.with(LocalTime.of(11, 0)))
                .status(AppointmentStatus.SCHEDULED)
                .build();

        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));
        when(appointmentRepository.findDoctorAppointmentsBetween(anyLong(), any(), any()))
                .thenReturn(new ArrayList<>());
        when(appointmentRepository.findByPatient(any())).thenReturn(List.of(existingAppointment));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            appointmentService.createAppointment(request);
        });

        assertTrue(exception.getMessage().contains("maximum number of appointments"));
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should cancel appointment successfully with valid notice period")
    void testCancelAppointmentSuccess() {
        // Arrange
        testAppointment.setStartTime(LocalDateTime.now().plusDays(3)); // More than 24h away

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(testAppointment);

        // Act
        AppointmentResponse response = appointmentService.cancelAppointment(1L);

        // Assert
        assertNotNull(response);
        assertEquals(AppointmentStatus.CANCELLED.name(), response.getStatus());
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should throw exception when cancelling within 24-hour notice period")
    void testCancelAppointmentWithinNoticeP eriod() {
        // Arrange
        testAppointment.setStartTime(LocalDateTime.now().plusHours(12)); // Less than 24h

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            appointmentService.cancelAppointment(1L);
        });

        assertTrue(exception.getMessage().contains("24 hours"));
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should throw exception when patient not found")
    void testCreateAppointmentPatientNotFound() {
        // Arrange
        AppointmentRequest request = new AppointmentRequest();
        request.setPatientId(999L);
        request.setDoctorId(1L);
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));

        when(patientRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            appointmentService.createAppointment(request);
        });
    }

    @Test
    @DisplayName("Should get doctor appointments within date range")
    void testGetDoctorAppointments() {
        // Arrange
        LocalDateTime from = LocalDateTime.now();
        LocalDateTime to = LocalDateTime.now().plusDays(7);

        List<Appointment> appointments = List.of(testAppointment);

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(testDoctor));
        when(appointmentRepository.findDoctorAppointmentsBetween(1L, from, to))
                .thenReturn(appointments);

        // Act
        List<AppointmentResponse> responses = appointmentService.getDoctorAppointments(1L, from, to);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Regular checkup", responses.get(0).getReason());
    }
}
