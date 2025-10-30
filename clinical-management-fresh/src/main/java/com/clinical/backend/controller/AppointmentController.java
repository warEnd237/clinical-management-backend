package com.clinical.backend.controller;

import com.clinical.backend.dto.appointment.AppointmentRequest;
import com.clinical.backend.dto.appointment.AppointmentResponse;
import com.clinical.backend.dto.common.ApiResponse;
import com.clinical.backend.enums.AppointmentStatus;
import com.clinical.backend.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "Appointment management endpoints")
public class AppointmentController {
    
    private final AppointmentService appointmentService;
    
    @PostMapping
    @Operation(summary = "Create appointment", description = "Create a new appointment with conflict validation")
    public ResponseEntity<ApiResponse<AppointmentResponse>> createAppointment(
            @Valid @RequestBody AppointmentRequest request
    ) {
        AppointmentResponse appointment = appointmentService.createAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Appointment created successfully", appointment));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get appointment by ID", description = "Retrieve a specific appointment")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointmentById(@PathVariable Long id) {
        AppointmentResponse appointment = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(ApiResponse.success(appointment));
    }
    
    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel appointment", description = "Cancel an appointment (respects cancellation window)")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancelAppointment(@PathVariable Long id) {
        AppointmentResponse appointment = appointmentService.cancelAppointment(id);
        return ResponseEntity.ok(ApiResponse.success("Appointment cancelled successfully", appointment));
    }
    
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update appointment status", description = "Update the status of an appointment")
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam AppointmentStatus status
    ) {
        AppointmentResponse appointment = appointmentService.updateAppointmentStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Appointment status updated", appointment));
    }
    
    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "Get doctor appointments", description = "Get appointments for a specific doctor in a date range")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getDoctorAppointments(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        List<AppointmentResponse> appointments = appointmentService.getDoctorAppointments(doctorId, from, to);
        return ResponseEntity.ok(ApiResponse.success(appointments));
    }
    
    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get patient appointments", description = "Get all appointments for a specific patient")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getPatientAppointments(
            @PathVariable Long patientId
    ) {
        List<AppointmentResponse> appointments = appointmentService.getPatientAppointments(patientId);
        return ResponseEntity.ok(ApiResponse.success(appointments));
    }
}
