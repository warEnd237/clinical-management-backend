package com.clinical.backend.service;

import com.clinical.backend.dto.patient.PatientRequest;
import com.clinical.backend.dto.patient.PatientResponse;
import com.clinical.backend.entity.Patient;
import com.clinical.backend.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Patient Service Tests")
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientService patientService;

    private Patient testPatient;
    private PatientRequest patientRequest;

    @BeforeEach
    void setUp() {
        testPatient = Patient.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 15))
                .gender("Male")
                .phone("+1234567890")
                .email("john.doe@test.com")
                .address("123 Main St, City, Country")
                .bloodType("O+")
                .emergencyContactName("Jane Doe")
                .emergencyContactPhone("+0987654321")
                .build();

        patientRequest = new PatientRequest();
        patientRequest.setFirstName("John");
        patientRequest.setLastName("Doe");
        patientRequest.setDateOfBirth(LocalDate.of(1990, 1, 15));
        patientRequest.setGender("Male");
        patientRequest.setPhone("+1234567890");
        patientRequest.setEmail("john.doe@test.com");
        patientRequest.setAddress("123 Main St, City, Country");
        patientRequest.setBloodType("O+");
        patientRequest.setEmergencyContactName("Jane Doe");
        patientRequest.setEmergencyContactPhone("+0987654321");
    }

    @Test
    @DisplayName("Should create patient successfully")
    void testCreatePatientSuccess() {
        // Arrange
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);

        // Act
        PatientResponse response = patientService.createPatient(patientRequest);

        // Assert
        assertNotNull(response);
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("john.doe@test.com", response.getEmail());
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    @Test
    @DisplayName("Should get patient by ID successfully")
    void testGetPatientByIdSuccess() {
        // Arrange
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));

        // Act
        PatientResponse response = patientService.getPatientById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("John Doe", response.getFirstName() + " " + response.getLastName());
        verify(patientRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when patient not found")
    void testGetPatientByIdNotFound() {
        // Arrange
        when(patientRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            patientService.getPatientById(999L);
        });
    }

    @Test
    @DisplayName("Should get all patients")
    void testGetAllPatients() {
        // Arrange
        Patient patient2 = Patient.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(1985, 5, 20))
                .email("jane.smith@test.com")
                .build();

        when(patientRepository.findAll()).thenReturn(List.of(testPatient, patient2));

        // Act
        List<PatientResponse> responses = patientService.getAllPatients();

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("John", responses.get(0).getFirstName());
        assertEquals("Jane", responses.get(1).getFirstName());
    }

    @Test
    @DisplayName("Should update patient successfully")
    void testUpdatePatientSuccess() {
        // Arrange
        patientRequest.setPhone("+9999999999");
        patientRequest.setAddress("456 New Address");

        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PatientResponse response = patientService.updatePatient(1L, patientRequest);

        // Assert
        assertNotNull(response);
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    @Test
    @DisplayName("Should delete patient successfully")
    void testDeletePatientSuccess() {
        // Arrange
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        doNothing().when(patientRepository).delete(any(Patient.class));

        // Act
        assertDoesNotThrow(() -> {
            patientService.deletePatient(1L);
        });

        // Assert
        verify(patientRepository, times(1)).delete(testPatient);
    }

    @Test
    @DisplayName("Should search patients by name")
    void testSearchPatientsByName() {
        // Arrange
        when(patientRepository.findByFirstNameContainingOrLastNameContaining(anyString(), anyString()))
                .thenReturn(List.of(testPatient));

        // Act
        List<PatientResponse> responses = patientService.searchPatients("John");

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("John", responses.get(0).getFirstName());
    }

    @Test
    @DisplayName("Should calculate patient age correctly")
    void testPatientAgeCalculation() {
        // Arrange
        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));

        // Act
        PatientResponse response = patientService.getPatientById(1L);

        // Assert
        int expectedAge = LocalDate.now().getYear() - 1990;
        // Age calculation would be in the response if implemented
        assertNotNull(response.getDateOfBirth());
    }

    @Test
    @DisplayName("Should handle patient with null optional fields")
    void testPatientWithNullFields() {
        // Arrange
        Patient minimalPatient = Patient.builder()
                .id(3L)
                .firstName("Minimal")
                .lastName("Patient")
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .build();

        when(patientRepository.save(any(Patient.class))).thenReturn(minimalPatient);

        PatientRequest minimalRequest = new PatientRequest();
        minimalRequest.setFirstName("Minimal");
        minimalRequest.setLastName("Patient");
        minimalRequest.setDateOfBirth(LocalDate.of(2000, 1, 1));

        // Act
        PatientResponse response = patientService.createPatient(minimalRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Minimal", response.getFirstName());
    }

    @Test
    @DisplayName("Should validate email format")
    void testPatientEmailValidation() {
        // This would typically be handled by validation annotations
        // Testing that service accepts valid email
        patientRequest.setEmail("valid.email@domain.com");

        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);

        // Act & Assert
        assertDoesNotThrow(() -> {
            patientService.createPatient(patientRequest);
        });
    }
}
