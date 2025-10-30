package com.clinical.backend.controller;

import com.clinical.backend.dto.appointment.AppointmentRequest;
import com.clinical.backend.dto.appointment.AppointmentResponse;
import com.clinical.backend.enums.AppointmentStatus;
import com.clinical.backend.service.AppointmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AppointmentController.class)
@DisplayName("Appointment Controller Integration Tests")
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppointmentService appointmentService;

    private ObjectMapper objectMapper;
    private AppointmentRequest appointmentRequest;
    private AppointmentResponse appointmentResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Setup request
        appointmentRequest = new AppointmentRequest();
        appointmentRequest.setPatientId(1L);
        appointmentRequest.setDoctorId(1L);
        appointmentRequest.setStartTime(LocalDateTime.now().plusDays(1));
        appointmentRequest.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        appointmentRequest.setReason("Regular checkup");

        // Setup response
        appointmentResponse = AppointmentResponse.builder()
                .id(1L)
                .patientId(1L)
                .patientName("John Doe")
                .doctorId(1L)
                .doctorName("Dr. Smith")
                .doctorSpecialty("Cardiology")
                .startTime(appointmentRequest.getStartTime())
                .endTime(appointmentRequest.getEndTime())
                .status("SCHEDULED")
                .reason("Regular checkup")
                .build();
    }

    @Test
    @WithMockUser(roles = "SECRETARY")
    @DisplayName("Should create appointment successfully")
    void testCreateAppointmentSuccess() throws Exception {
        // Arrange
        when(appointmentService.createAppointment(any(AppointmentRequest.class)))
                .thenReturn(appointmentResponse);

        // Act & Assert
        mockMvc.perform(post("/api/appointments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appointmentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Appointment created successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.patientName").value("John Doe"))
                .andExpect(jsonPath("$.data.doctorName").value("Dr. Smith"))
                .andExpect(jsonPath("$.data.status").value("SCHEDULED"));

        verify(appointmentService, times(1)).createAppointment(any(AppointmentRequest.class));
    }

    @Test
    @WithMockUser(roles = "SECRETARY")
    @DisplayName("Should return validation error for invalid request")
    void testCreateAppointmentInvalidRequest() throws Exception {
        // Arrange - Empty request (missing required fields)
        AppointmentRequest invalidRequest = new AppointmentRequest();

        // Act & Assert
        mockMvc.perform(post("/api/appointments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(appointmentService, never()).createAppointment(any());
    }

    @Test
    @DisplayName("Should require authentication for creating appointment")
    void testCreateAppointmentRequiresAuth() throws Exception {
        // Act & Assert - No authentication
        mockMvc.perform(post("/api/appointments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appointmentRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "SECRETARY")
    @DisplayName("Should get appointment by ID successfully")
    void testGetAppointmentByIdSuccess() throws Exception {
        // Arrange
        when(appointmentService.getAppointmentById(1L))
                .thenReturn(appointmentResponse);

        // Act & Assert
        mockMvc.perform(get("/api/appointments/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.patientName").value("John Doe"));

        verify(appointmentService, times(1)).getAppointmentById(1L);
    }

    @Test
    @WithMockUser(roles = "SECRETARY")
    @DisplayName("Should return 404 when appointment not found")
    void testGetAppointmentByIdNotFound() throws Exception {
        // Arrange
        when(appointmentService.getAppointmentById(999L))
                .thenThrow(new RuntimeException("Appointment not found"));

        // Act & Assert
        mockMvc.perform(get("/api/appointments/999")
                        .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "SECRETARY")
    @DisplayName("Should cancel appointment successfully")
    void testCancelAppointmentSuccess() throws Exception {
        // Arrange
        AppointmentResponse cancelledResponse = AppointmentResponse.builder()
                .id(1L)
                .status("CANCELLED")
                .build();

        when(appointmentService.cancelAppointment(1L))
                .thenReturn(cancelledResponse);

        // Act & Assert
        mockMvc.perform(post("/api/appointments/1/cancel")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Appointment cancelled successfully"))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));

        verify(appointmentService, times(1)).cancelAppointment(1L);
    }

    @Test
    @WithMockUser(roles = "SECRETARY")
    @DisplayName("Should update appointment status successfully")
    void testUpdateAppointmentStatusSuccess() throws Exception {
        // Arrange
        AppointmentResponse updatedResponse = AppointmentResponse.builder()
                .id(1L)
                .status("COMPLETED")
                .build();

        when(appointmentService.updateAppointmentStatus(1L, AppointmentStatus.COMPLETED))
                .thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(patch("/api/appointments/1/status")
                        .with(csrf())
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));

        verify(appointmentService, times(1))
                .updateAppointmentStatus(1L, AppointmentStatus.COMPLETED);
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    @DisplayName("Should get doctor appointments successfully")
    void testGetDoctorAppointmentsSuccess() throws Exception {
        // Arrange
        LocalDateTime from = LocalDateTime.now();
        LocalDateTime to = LocalDateTime.now().plusDays(7);

        AppointmentResponse appointment2 = AppointmentResponse.builder()
                .id(2L)
                .patientName("Jane Smith")
                .doctorId(1L)
                .status("SCHEDULED")
                .build();

        List<AppointmentResponse> appointments = Arrays.asList(appointmentResponse, appointment2);

        when(appointmentService.getDoctorAppointments(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(appointments);

        // Act & Assert
        mockMvc.perform(get("/api/appointments/doctor/1")
                        .with(csrf())
                        .param("from", from.toString())
                        .param("to", to.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].patientName").value("John Doe"))
                .andExpect(jsonPath("$.data[1].patientName").value("Jane Smith"));
    }

    @Test
    @WithMockUser(roles = "SECRETARY")
    @DisplayName("Should get patient appointments successfully")
    void testGetPatientAppointmentsSuccess() throws Exception {
        // Arrange
        List<AppointmentResponse> appointments = Arrays.asList(appointmentResponse);

        when(appointmentService.getPatientAppointments(1L))
                .thenReturn(appointments);

        // Act & Assert
        mockMvc.perform(get("/api/appointments/patient/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(1));

        verify(appointmentService, times(1)).getPatientAppointments(1L);
    }

    @Test
    @WithMockUser(roles = "SECRETARY")
    @DisplayName("Should return empty list when patient has no appointments")
    void testGetPatientAppointmentsEmpty() throws Exception {
        // Arrange
        when(appointmentService.getPatientAppointments(1L))
                .thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/appointments/patient/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    @WithMockUser(roles = "SECRETARY")
    @DisplayName("Should handle service exceptions gracefully")
    void testHandleServiceException() throws Exception {
        // Arrange
        when(appointmentService.createAppointment(any(AppointmentRequest.class)))
                .thenThrow(new RuntimeException("Doctor is not available"));

        // Act & Assert
        mockMvc.perform(post("/api/appointments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appointmentRequest)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "SECRETARY")
    @DisplayName("Should validate required fields in appointment request")
    void testValidateRequiredFields() throws Exception {
        // Arrange - Request with null patientId
        appointmentRequest.setPatientId(null);

        // Act & Assert
        mockMvc.perform(post("/api/appointments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appointmentRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "SECRETARY")
    @DisplayName("Should accept valid appointment time range")
    void testValidTimeRange() throws Exception {
        // Arrange
        when(appointmentService.createAppointment(any(AppointmentRequest.class)))
                .thenReturn(appointmentResponse);

        // Act & Assert
        mockMvc.perform(post("/api/appointments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appointmentRequest)))
                .andExpect(status().isCreated());
    }
}
