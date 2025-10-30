package com.clinical.backend.service;

import com.clinical.backend.dto.prescription.PrescriptionRequest;
import com.clinical.backend.dto.prescription.PrescriptionResponse;
import com.clinical.backend.entity.Appointment;
import com.clinical.backend.entity.Doctor;
import com.clinical.backend.entity.Patient;
import com.clinical.backend.entity.Prescription;
import com.clinical.backend.entity.User;
import com.clinical.backend.enums.AppointmentStatus;
import com.clinical.backend.enums.UserRole;
import com.clinical.backend.repository.AppointmentRepository;
import com.clinical.backend.repository.PrescriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Prescription Service Tests")
class PrescriptionServiceTest {

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private PrescriptionService prescriptionService;

    private Patient testPatient;
    private Doctor testDoctor;
    private User doctorUser;
    private Appointment testAppointment;
    private Prescription testPrescription;
    private PrescriptionRequest prescriptionRequest;

    @BeforeEach
    void setUp() {
        // Create test patient
        testPatient = Patient.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(1985, 5, 20))
                .phone("+987654321")
                .email("jane.smith@test.com")
                .build();

        // Create test doctor user
        doctorUser = new User();
        doctorUser.setId(1L);;
        doctorUser.setEmail("dr.johnson@test.com");
        doctorUser.setFullName("Dr. Emily Johnson");
        doctorUser.setRole(UserRole.DOCTOR);

        // Create test doctor
        testDoctor = Doctor.builder()
                .id(1L)
                .user(doctorUser)
                .specialty("General Practice")
                .licenseNumber("DOC123")
                .build();

        // Create test appointment
        testAppointment = Appointment.builder()
                .id(1L)
                .patient(testPatient)
                .doctor(testDoctor)
                .startTime(LocalDateTime.now().minusHours(1))
                .endTime(LocalDateTime.now())
                .status(AppointmentStatus.COMPLETED)
                .reason("Follow-up visit")
                .build();

        // Create test prescription
        testPrescription = Prescription.builder()
                .id(1L)
                .appointment(testAppointment)
                .doctor(testDoctor)
                .patient(testPatient)
                .diagnosis("Hypertension")
                .medications("Lisinopril 10mg - Once daily")
                .instructions("Take with food, monitor blood pressure")
                .validUntil(LocalDate.now().plusMonths(3))
                .createdAt(LocalDateTime.now())
                .build();

        // Create test request
        prescriptionRequest = new PrescriptionRequest();
        prescriptionRequest.setAppointmentId(1L);
        prescriptionRequest.setDiagnosis("Hypertension");
        prescriptionRequest.setMedications("Lisinopril 10mg - Once daily");
        prescriptionRequest.setInstructions("Take with food, monitor blood pressure");
        prescriptionRequest.setValidUntil(LocalDate.now().plusMonths(3));
    }

    @Test
    @DisplayName("Should create prescription successfully")
    void testCreatePrescriptionSuccess() {
        // Arrange
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
        when(prescriptionRepository.save(any(Prescription.class))).thenReturn(testPrescription);

        // Act
        PrescriptionResponse response = prescriptionService.createPrescription(prescriptionRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Hypertension", response.getDiagnosis());
        assertEquals("Lisinopril 10mg - Once daily", response.getMedications());
        verify(prescriptionRepository, times(1)).save(any(Prescription.class));
    }

    @Test
    @DisplayName("Should throw exception when appointment not found")
    void testCreatePrescriptionAppointmentNotFound() {
        // Arrange
        when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());
        prescriptionRequest.setAppointmentId(999L);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            prescriptionService.createPrescription(prescriptionRequest);
        });

        verify(prescriptionRepository, never()).save(any(Prescription.class));
    }

    @Test
    @DisplayName("Should link prescription to correct doctor from appointment")
    void testPrescriptionLinkedToDoctor() {
        // Arrange
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
        when(prescriptionRepository.save(any(Prescription.class))).thenAnswer(invocation -> {
            Prescription saved = invocation.getArgument(0);
            assertEquals(testDoctor.getId(), saved.getDoctor().getId());
            return testPrescription;
        });

        // Act
        prescriptionService.createPrescription(prescriptionRequest);

        // Assert
        verify(prescriptionRepository).save(any(Prescription.class));
    }

    @Test
    @DisplayName("Should link prescription to correct patient from appointment")
    void testPrescriptionLinkedToPatient() {
        // Arrange
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
        when(prescriptionRepository.save(any(Prescription.class))).thenAnswer(invocation -> {
            Prescription saved = invocation.getArgument(0);
            assertEquals(testPatient.getId(), saved.getPatient().getId());
            return testPrescription;
        });

        // Act
        prescriptionService.createPrescription(prescriptionRequest);

        // Assert
        verify(prescriptionRepository).save(any(Prescription.class));
    }

    @Test
    @DisplayName("Should get prescription by ID successfully")
    void testGetPrescriptionByIdSuccess() {
        // Arrange
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(testPrescription));

        // Act
        PrescriptionResponse response = prescriptionService.getPrescriptionById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Hypertension", response.getDiagnosis());
        assertEquals("Dr. Emily Johnson", response.getDoctorName());
        assertEquals("Jane Smith", response.getPatientName());
    }

    @Test
    @DisplayName("Should throw exception when prescription not found by ID")
    void testGetPrescriptionByIdNotFound() {
        // Arrange
        when(prescriptionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            prescriptionService.getPrescriptionById(999L);
        });
    }

    @Test
    @DisplayName("Should get all prescriptions for a patient")
    void testGetPatientPrescriptions() {
        // Arrange
        Prescription prescription2 = Prescription.builder()
                .id(2L)
                .appointment(testAppointment)
                .doctor(testDoctor)
                .patient(testPatient)
                .diagnosis("Diabetes")
                .medications("Metformin 500mg")
                .build();

        when(prescriptionRepository.findByPatientId(1L))
                .thenReturn(Arrays.asList(testPrescription, prescription2));

        // Act
        List<PrescriptionResponse> responses = prescriptionService.getPatientPrescriptions(1L);

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("Hypertension", responses.get(0).getDiagnosis());
        assertEquals("Diabetes", responses.get(1).getDiagnosis());
    }

    @Test
    @DisplayName("Should return empty list when patient has no prescriptions")
    void testGetPatientPrescriptionsEmpty() {
        // Arrange
        when(prescriptionRepository.findByPatientId(1L)).thenReturn(Arrays.asList());

        // Act
        List<PrescriptionResponse> responses = prescriptionService.getPatientPrescriptions(1L);

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    @DisplayName("Should include all required fields in prescription")
    void testPrescriptionHasAllFields() {
        // Arrange
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
        when(prescriptionRepository.save(any(Prescription.class))).thenAnswer(invocation -> {
            Prescription saved = invocation.getArgument(0);
            assertNotNull(saved.getAppointment());
            assertNotNull(saved.getDoctor());
            assertNotNull(saved.getPatient());
            assertNotNull(saved.getDiagnosis());
            assertNotNull(saved.getMedications());
            assertNotNull(saved.getInstructions());
            assertNotNull(saved.getValidUntil());
            return testPrescription;
        });

        // Act
        prescriptionService.createPrescription(prescriptionRequest);

        // Assert
        verify(prescriptionRepository).save(any(Prescription.class));
    }

    @Test
    @DisplayName("Should throw exception for PDF generation (not implemented)")
    void testGeneratePrescriptionPdfNotImplemented() {
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            prescriptionService.generatePrescriptionPdf(1L);
        });

        assertTrue(exception.getMessage().contains("not implemented"));
    }

    @Test
    @DisplayName("Should include valid until date in response")
    void testResponseIncludesValidUntil() {
        // Arrange
        LocalDate futureDate = LocalDate.now().plusMonths(6);
        testPrescription.setValidUntil(futureDate);

        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(testPrescription));

        // Act
        PrescriptionResponse response = prescriptionService.getPrescriptionById(1L);

        // Assert
        assertNotNull(response.getValidUntil());
        assertEquals(futureDate, response.getValidUntil());
    }

    @Test
    @DisplayName("Should format patient name correctly in response")
    void testPatientNameFormattedCorrectly() {
        // Arrange
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(testPrescription));

        // Act
        PrescriptionResponse response = prescriptionService.getPrescriptionById(1L);

        // Assert
        assertEquals("Jane Smith", response.getPatientName());
    }

    @Test
    @DisplayName("Should include creation timestamp")
    void testIncludesCreatedAt() {
        // Arrange
        when(prescriptionRepository.findById(1L)).thenReturn(Optional.of(testPrescription));

        // Act
        PrescriptionResponse response = prescriptionService.getPrescriptionById(1L);

        // Assert
        assertNotNull(response.getCreatedAt());
    }
}
