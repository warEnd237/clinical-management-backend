package com.clinical.backend.controller;

import com.clinical.backend.dto.appointment.AppointmentResponse;
import com.clinical.backend.dto.common.ApiResponse;
import com.clinical.backend.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
@Tag(name = "Calendar", description = "Calendar view endpoints")
public class CalendarController {
    
    private final AppointmentService appointmentService;
    
    @GetMapping
    @Operation(summary = "Get calendar appointments", description = "Get appointments for calendar view with optional doctor filter")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getCalendarAppointments(
            @RequestParam(required = false) Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        List<AppointmentResponse> appointments;
        
        if (doctorId != null) {
            // Return appointments for specific doctor
            appointments = appointmentService.getDoctorAppointments(doctorId, from, to);
        } else {
            // Return all appointments in the date range (admin view)
            appointments = appointmentService.getAllAppointmentsInDateRange(from, to);
        }
        
        return ResponseEntity.ok(ApiResponse.success(appointments));
    }
}
