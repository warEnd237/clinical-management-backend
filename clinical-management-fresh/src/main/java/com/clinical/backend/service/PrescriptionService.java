package com.clinical.backend.service;

import com.clinical.backend.dto.prescription.PrescriptionRequest;
import com.clinical.backend.dto.prescription.PrescriptionResponse;
import com.clinical.backend.entity.Appointment;
import com.clinical.backend.entity.Prescription;
import com.clinical.backend.repository.AppointmentRepository;
import com.clinical.backend.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrescriptionService {
    
    private final PrescriptionRepository prescriptionRepository;
    private final AppointmentRepository appointmentRepository;
    
    @Transactional
    public PrescriptionResponse createPrescription(PrescriptionRequest request) {
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        
        Prescription prescription = Prescription.builder()
                .appointment(appointment)
                .doctor(appointment.getDoctor())
                .patient(appointment.getPatient())
                .diagnosis(request.getDiagnosis())
                .medications(request.getMedications())
                .instructions(request.getInstructions())
                .validUntil(request.getValidUntil())
                .build();
        
        Prescription savedPrescription = prescriptionRepository.save(prescription);
        
        return toResponse(savedPrescription);
    }
    
    @Transactional(readOnly = true)
    public PrescriptionResponse getPrescriptionById(Long id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescription not found"));
        
        return toResponse(prescription);
    }
    
    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getPatientPrescriptions(Long patientId) {
        return prescriptionRepository.findByPatientId(patientId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    public byte[] generatePrescriptionPdf(Long id) {
        // TODO: Implement PDF generation using PDFBox
        throw new RuntimeException("PDF generation not implemented yet");
    }
    
    private PrescriptionResponse toResponse(Prescription prescription) {
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
