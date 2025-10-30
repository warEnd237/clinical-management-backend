package com.clinical.backend.controller;

import com.clinical.backend.dto.patient.PatientRequest;
import com.clinical.backend.dto.patient.PatientResponse;
import com.clinical.backend.service.PatientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PatientController.class)
@DisplayName("Patient Controller Integration Tests")
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientService patientService;

    private ObjectMapper objectMapper;
    private PatientRequest patientRequest;
    private PatientResponse patientResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Setup request
        patientRequest = new PatientRequest();
        patientRequest.setFirstName("John");
        patientRequest.setLastName("Doe");
        patientRequest.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patientRequest.setGender("Male");
        patientRequest.setPhone("+1234567890");
        patientRequest.setEmail("john.doe@test.com");
        patientRequest.setAddress("123 Main St");

        // Setup response
        patientResponse = PatientResponse.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender("Male")
                .phone("+1234567890")
                .email("john.doe@test.com")
                .address("123 Main St")
                .build();
    }

    @Test
    @WithMockUser(roles = "SECRETARY")
    @DisplayName("Should get all patients with pagination")
    void testGetAllPatientsSuccess() throws Exception {
        // Arrange
        PatientResponse patient2 = PatientResponse.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .build();

        Page<PatientResponse> page = new PageImpl<>(Arrays.asList(patientResponse, patient2));

        when(patientService.getAllPatients(any(Pageable.class)))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/patients")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.content[0].firstName").value("John"))
                .andExpect(jsonPath("$.data.content[1].firstName").value("Jane"));
    }

    @Test
    @WithMockUser(roles = "SECRETARY")
    @DisplayName("Should get patient by ID successfully")
    void testGetPatientByIdSuccess() throws Exception {
        // Arrange
        when(patientService.getPatientById(1L))
                .thenReturn(patientResponse);

        // Act & Assert
        mockMvc.perform(get("/api/patients/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.lastName").value("Doe"))
                .andExpect(jsonPath("$.data.email").value("john.doe@test.com"));

        verify(patientService, times(1)).getPatientById(1L);
    }

    @Test
    @WithMockUser(roles = "SECRETARY")
    @DisplayName("Should search patients by query")
    void testSearchPatientsSuccess() throws Exception {
        // Arrange
        Page<PatientResponse> page = new PageImpl<>(Arrays.asList(patientResponse));

        when(patientService.searchPatients(anyString(), any(Pageable.class)))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/patients/search")
                        .with(csrf())
                        .param("query", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].firstName").value("John"));

        verify(patientService, times(1)).searchPatients(eq("John"), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "SECRETARY")
    @DisplayName("Should create patient successfully")
    void testCreatePatientSuccess() throws Exception {
        // Arrange
        when(patientService.createPatient(any(PatientRequest.class)))
                .thenReturn(patientResponse);

        // Act & Assert
        mockMvc.perform(post("/api/patients")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patientRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Patient created successfully"))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.email").value("john.doe@test.com"));

        verify(patientService, times(1)).createPatient(any(PatientRequest.class));
    }

    @Test
    @WithMockUser(roles = "SECRETARY")
    @DisplayName("Should return validation error for invalid patient request")
    void testCreatePatientInvalidRequest() throws Exception {
        // Arrange - Empty request
        PatientRequest invalidRequest = new PatientRequest();

        // Act & Assert
        mockMvc.perform(post("/api/patients")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(patientService, never()).createPatient(any());
    }

    @Test
    @WithMockUser(roles = "SECRETARY")
    @DisplayName("Should update patient successfully")
    void testUpdatePatientSuccess() throws Exception {
        // Arrange
        PatientResponse updatedResponse = PatientResponse.builder()
                .id(1L)
                .firstName("John")
                .lastName("Updated")
                .email("updated@test.com")
                .build();

        when(patientService.updatePatient(anyLong(), any(PatientRequest.class)))
                .thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/patients/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patientRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Patient updated successfully"))
                .andExpect(jsonPath("$.data.lastName").value("Updated"));

        verify(patientService, times(1)).updatePatient(eq(1L), any(PatientRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should delete patient successfully as admin")
    void testDeletePatientSuccessAsAdmin() throws Exception {
        // Arrange
        doNothing().when(patientService).deletePatient(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/patients/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Patient deleted successfully"));

        verify(patientService, times(1)).deletePatient(1L);
    }

    @Test
    @WithMockUser(roles = "SECRETARY")
    @DisplayName("Should deny delete patient for non-admin")
    void testDeletePatientDeniedForNonAdmin() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/patients/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(patientService, never()).deletePatient(anyLong());
    }

    @Test
    @DisplayName("Should require authentication for all endpoints")
    void testRequireAuthentication() throws Exception {
        // Act & Assert - Get all
        mockMvc.perform(get("/api/patients")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        // Get by ID
        mockMvc.perform(get("/api/patients/1")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        // Create
        mockMvc.perform(post("/api/patients")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patientRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "SECRETARY")
    @DisplayName("Should handle patient not found exception")
    void testPatientNotFound() throws Exception {
        // Arrange
        when(patientService.getPatientById(999L))
                .thenThrow(new RuntimeException("Patient not found"));

        // Act & Assert
        mockMvc.perform(get("/api/patients/999")
                        .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "SECRETARY")
    @DisplayName("Should return empty page when no patients found")
    void testGetAllPatientsEmpty() throws Exception {
        // Arrange
        Page<PatientResponse> emptyPage = new PageImpl<>(Arrays.asList());

        when(patientService.getAllPatients(any(Pageable.class)))
                .thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/patients")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(0)));
    }

    @Test
    @WithMockUser(roles = "SECRETARY")
    @DisplayName("Should handle pagination parameters correctly")
    void testPaginationParameters() throws Exception {
        // Arrange
        Page<PatientResponse> page = new PageImpl<>(Arrays.asList(patientResponse));
        when(patientService.getAllPatients(any(Pageable.class)))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/patients")
                        .with(csrf())
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "firstName,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
