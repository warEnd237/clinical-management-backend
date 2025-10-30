package com.clinical.backend.scheduler;

import com.clinical.backend.entity.Appointment;
import com.clinical.backend.enums.AppointmentStatus;
import com.clinical.backend.repository.AppointmentRepository;
import com.clinical.backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler for sending appointment reminder emails
 * Runs daily to check for appointments happening in the next 24 hours
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentReminderScheduler {

    private final AppointmentRepository appointmentRepository;
    private final EmailService emailService;

    /**
     * Send reminders for appointments happening in the next 24 hours
     * Runs every day at 9:00 AM
     */
    @Scheduled(cron = "${app.scheduler.reminder-cron:0 0 9 * * ?}")
    public void sendAppointmentReminders() {
        log.info("Starting appointment reminder scheduler");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusHours(24);
        LocalDateTime dayAfterTomorrow = now.plusHours(48);

        try {
            // Find appointments scheduled between 24 and 48 hours from now
            List<Appointment> upcomingAppointments = appointmentRepository
                    .findByStartTimeBetweenAndStatus(tomorrow, dayAfterTomorrow, AppointmentStatus.SCHEDULED);

            log.info("Found {} appointments requiring reminders", upcomingAppointments.size());

            for (Appointment appointment : upcomingAppointments) {
                try {
                    // Check if patient has email
                    if (appointment.getPatient().getEmail() != null && 
                        !appointment.getPatient().getEmail().isEmpty()) {
                        
                        emailService.sendAppointmentReminder(appointment);
                        log.info("Reminder sent for appointment ID: {}", appointment.getId());
                    } else {
                        log.warn("Patient {} has no email address, skipping reminder for appointment {}",
                                appointment.getPatient().getId(), appointment.getId());
                    }
                } catch (Exception e) {
                    log.error("Failed to send reminder for appointment ID: {}", appointment.getId(), e);
                }
            }

            log.info("Appointment reminder scheduler completed successfully");

        } catch (Exception e) {
            log.error("Error in appointment reminder scheduler", e);
        }
    }

    /**
     * Clean up old cancelled appointments
     * Runs weekly on Sunday at midnight
     */
    @Scheduled(cron = "${app.scheduler.cleanup-cron:0 0 0 * * SUN}")
    public void cleanupOldAppointments() {
        log.info("Starting cleanup of old appointments");

        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusMonths(6);

            // This would typically archive or delete very old cancelled appointments
            // For now, just log the count
            long oldCancelledCount = appointmentRepository
                    .findByStatusAndStartTimeBefore(AppointmentStatus.CANCELLED, cutoffDate)
                    .size();

            log.info("Found {} old cancelled appointments (older than 6 months)", oldCancelledCount);
            
            // In production, you might want to archive these to a separate table
            // appointmentRepository.deleteAll(oldAppointments);

        } catch (Exception e) {
            log.error("Error in cleanup scheduler", e);
        }
    }

    /**
     * Mark no-show appointments
     * Runs every hour to check for appointments that were missed
     */
    @Scheduled(cron = "${app.scheduler.no-show-cron:0 0 * * * ?}")
    public void markNoShowAppointments() {
        log.info("Starting no-show appointment check");

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneHourAgo = now.minusHours(1);

            // Find scheduled appointments that ended over 1 hour ago
            List<Appointment> missedAppointments = appointmentRepository
                    .findByStatusAndEndTimeBefore(AppointmentStatus.SCHEDULED, oneHourAgo);

            if (!missedAppointments.isEmpty()) {
                log.info("Found {} potentially missed appointments", missedAppointments.size());

                for (Appointment appointment : missedAppointments) {
                    appointment.setStatus(AppointmentStatus.NO_SHOW);
                    appointmentRepository.save(appointment);
                    log.info("Marked appointment {} as NO_SHOW", appointment.getId());
                }
            }

        } catch (Exception e) {
            log.error("Error in no-show scheduler", e);
        }
    }
}
