package com.clinical.backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinical.backend.dto.appointment.AppointmentRequest;
import com.clinical.backend.dto.appointment.AppointmentResponse;
import com.clinical.backend.dto.common.NotificationDto;
import com.clinical.backend.entity.Appointment;
import com.clinical.backend.entity.Doctor;
import com.clinical.backend.entity.Patient;
import com.clinical.backend.enums.AppointmentStatus;
import com.clinical.backend.repository.AppointmentRepository;
import com.clinical.backend.repository.DoctorRepository;
import com.clinical.backend.repository.PatientRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final EmailService emailService;
    private final SimpMessagingTemplate messagingTemplate;
    
    // Business rule configurations
    private static final int CANCELLATION_NOTICE_HOURS = 24;
    private static final int MAX_APPOINTMENTS_PER_DAY_PER_PATIENT = 1;
    
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + id));
        return toResponse(appointment);
    }
    
    @Transactional
    public AppointmentResponse createAppointment(AppointmentRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        
        // Validate business rules
        validateAppointmentCreation(patient, doctor, request.getStartTime(), request.getEndTime());
        
        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .reason(request.getReason())
                .status(AppointmentStatus.SCHEDULED)
                .build();
        
        Appointment savedAppointment = appointmentRepository.save(appointment);
        
        // Send confirmation email
        emailService.sendAppointmentConfirmation(savedAppointment);
        
        // Send WebSocket notification to patient
        sendNotificationToUser(
            patient.getEmail(),
            NotificationDto.appointmentCreated(
                patient.getEmail(),
                savedAppointment.getId(),
                patient.getFullName(),
                doctor.getUser().getFullName(),
                savedAppointment.getStartTime().toString()
            )
        );
        
        // Send WebSocket notification to doctor
        sendNotificationToUser(
            doctor.getUser().getEmail(),
            NotificationDto.appointmentCreated(
                doctor.getUser().getEmail(),
                savedAppointment.getId(),
                patient.getFullName(),
                doctor.getUser().getFullName(),
                savedAppointment.getStartTime().toString()
            )
        );
        
        return toResponse(savedAppointment);
    }
    
    @Transactional
    public AppointmentResponse cancelAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + id));
        
        // Check cancellation deadline
        validateCancellation(appointment);
        
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledAt(LocalDateTime.now());
        
        Appointment savedAppointment = appointmentRepository.save(appointment);
        
        // Send cancellation email
        emailService.sendAppointmentCancellation(savedAppointment);
        
        // Send WebSocket notification to patient
        sendNotificationToUser(
            savedAppointment.getPatient().getEmail(),
            NotificationDto.appointmentCancelled(
                savedAppointment.getPatient().getEmail(),
                savedAppointment.getId(),
                savedAppointment.getPatient().getFullName(),
                "Cancelled by request"
            )
        );
        
        // Send WebSocket notification to doctor
        sendNotificationToUser(
            savedAppointment.getDoctor().getUser().getEmail(),
            NotificationDto.appointmentCancelled(
                savedAppointment.getDoctor().getUser().getEmail(),
                savedAppointment.getId(),
                savedAppointment.getPatient().getFullName(),
                "Cancelled by request"
            )
        );
        
        return toResponse(savedAppointment);
    }
    
    @Transactional
    public AppointmentResponse updateAppointmentStatus(Long id, AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + id));
        
        appointment.setStatus(status);
        if (status == AppointmentStatus.CANCELLED) {
            appointment.setCancelledAt(LocalDateTime.now());
        }
        
        Appointment savedAppointment = appointmentRepository.save(appointment);
        
        // Send WebSocket notification for status change
        sendNotificationToUser(
            savedAppointment.getPatient().getEmail(),
            NotificationDto.appointmentStatusChanged(
                savedAppointment.getPatient().getEmail(),
                savedAppointment.getId(),
                savedAppointment.getPatient().getFullName(),
                status.name()
            )
        );
        
        sendNotificationToUser(
            savedAppointment.getDoctor().getUser().getEmail(),
            NotificationDto.appointmentStatusChanged(
                savedAppointment.getDoctor().getUser().getEmail(),
                savedAppointment.getId(),
                savedAppointment.getPatient().getFullName(),
                status.name()
            )
        );
        
        return toResponse(savedAppointment);
    }
    
    // Helper method to send WebSocket notifications
    private void sendNotificationToUser(String userEmail, NotificationDto notification) {
        try {
            messagingTemplate.convertAndSendToUser(
                userEmail,
                "/queue/notifications",
                notification
            );
        } catch (Exception e) {
            // Log error but don't fail the operation
            System.err.println("Failed to send WebSocket notification to " + userEmail + ": " + e.getMessage());
        }
    }
    
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getDoctorAppointments(Long doctorId, LocalDateTime from, LocalDateTime to) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        
        List<Appointment> appointments = appointmentRepository.findDoctorAppointmentsBetween(doctor.getId(), from, to);
        return appointments.stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getPatientAppointments(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        
        List<Appointment> appointments = appointmentRepository.findByPatient(patient);
        return appointments.stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAllAppointmentsInDateRange(LocalDateTime from, LocalDateTime to) {
        List<Appointment> appointments = appointmentRepository.findAllAppointmentsBetween(from, to);
        return appointments.stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    // Validation methods
    private void validateAppointmentCreation(Patient patient, Doctor doctor, LocalDateTime startTime, LocalDateTime endTime) {
        // Check for doctor conflicts
        List<Appointment> doctorConflicts = appointmentRepository.findDoctorAppointmentsBetween(
                doctor.getId(), startTime, endTime);
        
        if (!doctorConflicts.isEmpty()) {
            throw new RuntimeException("Doctor is not available at the requested time");
        }
        
        // Check patient's daily appointment limit
        LocalDate appointmentDate = startTime.toLocalDate();
        LocalDateTime dayStart = appointmentDate.atStartOfDay();
        LocalDateTime dayEnd = appointmentDate.plusDays(1).atStartOfDay();
        
        long patientAppointmentsToday = appointmentRepository.findByPatient(patient).stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                .filter(a -> !a.getStartTime().isBefore(dayStart) && a.getStartTime().isBefore(dayEnd))
                .count();
        
        if (patientAppointmentsToday >= MAX_APPOINTMENTS_PER_DAY_PER_PATIENT) {
            throw new RuntimeException("Patient has reached the maximum number of appointments for this day");
        }
        
        // Validate appointment time is in the future
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot book appointments in the past");
        }
        
        // Validate end time is after start time
        if (!endTime.isAfter(startTime)) {
            throw new RuntimeException("End time must be after start time");
        }
    }
    
    private void validateCancellation(Appointment appointment) {
        // Check if already cancelled
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new RuntimeException("Appointment is already cancelled");
        }
        
        // Check cancellation deadline
        long hoursUntilAppointment = ChronoUnit.HOURS.between(LocalDateTime.now(), appointment.getStartTime());
        
        if (hoursUntilAppointment < CANCELLATION_NOTICE_HOURS) {
            throw new RuntimeException(String.format(
                "Cannot cancel appointment. Minimum notice period is %d hours", 
                CANCELLATION_NOTICE_HOURS
            ));
        }
    }
    
    // Helper method for mapping
    private AppointmentResponse toResponse(Appointment appointment) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .patientId(appointment.getPatient().getId())
                .patientName(appointment.getPatient().getFullName())
                .doctorId(appointment.getDoctor().getId())
                .doctorName(appointment.getDoctor().getUser().getFullName())
                .doctorSpecialty(appointment.getDoctor().getSpecialty())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .status(appointment.getStatus().name())
                .reason(appointment.getReason())
                .notes(appointment.getNotes())
                .createdAt(appointment.getCreatedAt())
                .cancelledAt(appointment.getCancelledAt())
                .build();
    }
}
