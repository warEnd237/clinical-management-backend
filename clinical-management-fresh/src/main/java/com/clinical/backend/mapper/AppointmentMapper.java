package com.clinical.backend.mapper;

import com.clinical.backend.dto.appointment.AppointmentResponse;
import com.clinical.backend.entity.Appointment;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {
    
    public AppointmentResponse toResponse(Appointment appointment) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .patientId(appointment.getPatient().getId())
                .patientName(appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName())
                .doctorId(appointment.getDoctor().getId())
                .doctorName(appointment.getDoctor().getUser().getFullName())
                .doctorSpecialty(appointment.getDoctor().getSpecialty())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .reason(appointment.getReason())
                .status(appointment.getStatus().name())
                .notes(appointment.getNotes())
                .cancelledAt(appointment.getCancelledAt())
                .createdAt(appointment.getCreatedAt())
                .build();
    }
}
