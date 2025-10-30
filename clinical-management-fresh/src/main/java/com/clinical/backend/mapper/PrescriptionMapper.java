package com.clinical.backend.mapper;

import com.clinical.backend.dto.prescription.PrescriptionResponse;
import com.clinical.backend.entity.Prescription;
import org.springframework.stereotype.Component;

@Component
public class PrescriptionMapper {
    
    public PrescriptionResponse toResponse(Prescription prescription) {
        return PrescriptionResponse.builder()
                .id(prescription.getId())
                .appointmentId(prescription.getAppointment().getId())
                .doctorId(prescription.getDoctor().getId())
                .doctorName(prescription.getDoctor().getUser().getFullName())
                .patientId(prescription.getPatient().getId())
                .patientName(prescription.getPatient().getFirstName() + " " + prescription.getPatient().getLastName())
                .diagnosis(prescription.getDiagnosis())
                .medications(prescription.getMedications())
                .instructions(prescription.getInstructions())
                .validUntil(prescription.getValidUntil())
                .pdfPath(prescription.getPdfPath())
                .createdAt(prescription.getCreatedAt())
                .build();
    }
}
