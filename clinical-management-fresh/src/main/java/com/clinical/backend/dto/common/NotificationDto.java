package com.clinical.backend.dto.common;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {
    
    private Long id;
    private String type;
    private String title;
    private String message;
    private String link;
    private String targetUserEmail;
    private LocalDateTime createdAt;
    
    public static NotificationDto appointmentCreated(String userEmail, Long appointmentId, String patientName, String doctorName, String dateTime) {
        return NotificationDto.builder()
                .type("APPOINTMENT_CREATED")
                .title("New Appointment Created")
                .message(String.format("Appointment scheduled for %s with Dr. %s on %s", patientName, doctorName, dateTime))
                .link("/appointments/" + appointmentId)
                .targetUserEmail(userEmail)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    public static NotificationDto appointmentCancelled(String userEmail, Long appointmentId, String patientName, String reason) {
        return NotificationDto.builder()
                .type("APPOINTMENT_CANCELLED")
                .title("Appointment Cancelled")
                .message(String.format("Appointment with %s has been cancelled. %s", patientName, reason != null ? "Reason: " + reason : ""))
                .link("/appointments/" + appointmentId)
                .targetUserEmail(userEmail)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    public static NotificationDto appointmentStatusChanged(String userEmail, Long appointmentId, String patientName, String newStatus) {
        return NotificationDto.builder()
                .type("APPOINTMENT_STATUS_CHANGED")
                .title("Appointment Status Updated")
                .message(String.format("Appointment with %s status changed to %s", patientName, newStatus))
                .link("/appointments/" + appointmentId)
                .targetUserEmail(userEmail)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    public static NotificationDto messageReceived(String userEmail, Long messageId, String senderName, String subject) {
        return NotificationDto.builder()
                .type("MESSAGE_RECEIVED")
                .title("New Message Received")
                .message(String.format("You have a new message from %s: %s", senderName, subject))
                .link("/messages/" + messageId)
                .targetUserEmail(userEmail)
                .createdAt(LocalDateTime.now())
                .build();
    }
}