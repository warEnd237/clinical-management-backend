package com.clinical.backend.service;

import com.clinical.backend.entity.Appointment;
import com.clinical.backend.entity.Notification;
import com.clinical.backend.entity.User;
import com.clinical.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    
    @Transactional
    public void notifyAppointmentCreated(Appointment appointment) {
        // Notify patient
        createNotification(
            appointment.getPatient().getUser(),
            "APPOINTMENT_CREATED",
            "Appointment Scheduled",
            "Your appointment with Dr. " + appointment.getDoctor().getUser().getFullName() + 
            " has been scheduled for " + appointment.getStartTime(),
            "/appointments/" + appointment.getId()
        );
        
        // Notify doctor
        createNotification(
            appointment.getDoctor().getUser(),
            "APPOINTMENT_CREATED",
            "New Appointment",
            "You have a new appointment with " + appointment.getPatient().getFirstName() + 
            " " + appointment.getPatient().getLastName() + " on " + appointment.getStartTime(),
            "/appointments/" + appointment.getId()
        );
    }
    
    @Transactional
    public void notifyAppointmentCancelled(Appointment appointment) {
        // Notify patient
        createNotification(
            appointment.getPatient().getUser(),
            "APPOINTMENT_CANCELLED",
            "Appointment Cancelled",
            "Your appointment scheduled for " + appointment.getStartTime() + " has been cancelled",
            "/appointments"
        );
        
        // Notify doctor
        createNotification(
            appointment.getDoctor().getUser(),
            "APPOINTMENT_CANCELLED",
            "Appointment Cancelled",
            "Appointment with " + appointment.getPatient().getFirstName() + 
            " " + appointment.getPatient().getLastName() + " has been cancelled",
            "/appointments"
        );
    }
    
    private void createNotification(User user, String type, String title, String message, String link) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .link(link)
                .isRead(false)
                .build();
        
        notificationRepository.save(notification);
    }
}
