package com.clinical.backend.service;

import com.clinical.backend.entity.Appointment;
import com.clinical.backend.entity.Patient;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${email.from:noreply@clinical.com}")
    private String fromEmail;

    @Value("${app.name:Clinical Management System}")
    private String appName;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    /**
     * Send appointment confirmation email
     */
    @Async
    public void sendAppointmentConfirmation(Appointment appointment) {
        try {
            String to = appointment.getPatient().getEmail();
            if (to == null || to.isEmpty()) {
                log.warn("Patient {} has no email address", appointment.getPatient().getId());
                return;
            }

            Context context = new Context();
            context.setVariable("patientName", appointment.getPatient().getFullName());
            context.setVariable("doctorName", appointment.getDoctor().getUser().getFullName());
            context.setVariable("appointmentDate", appointment.getStartTime().format(DATE_FORMATTER));
            context.setVariable("appointmentTime", appointment.getStartTime().format(TIME_FORMATTER));
            context.setVariable("doctorSpecialty", appointment.getDoctor().getSpecialty());
            context.setVariable("reason", appointment.getReason());
            context.setVariable("appName", appName);

            String htmlContent = templateEngine.process("email/appointment-confirmation", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Appointment Confirmation - " + appointment.getStartTime().format(DATE_FORMATTER));
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Appointment confirmation email sent to {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send appointment confirmation email", e);
        }
    }

    /**
     * Send appointment reminder email (24 hours before)
     */
    @Async
    public void sendAppointmentReminder(Appointment appointment) {
        try {
            String to = appointment.getPatient().getEmail();
            if (to == null || to.isEmpty()) {
                log.warn("Patient {} has no email address", appointment.getPatient().getId());
                return;
            }

            Context context = new Context();
            context.setVariable("patientName", appointment.getPatient().getFullName());
            context.setVariable("doctorName", appointment.getDoctor().getUser().getFullName());
            context.setVariable("appointmentDate", appointment.getStartTime().format(DATE_FORMATTER));
            context.setVariable("appointmentTime", appointment.getStartTime().format(TIME_FORMATTER));
            context.setVariable("doctorSpecialty", appointment.getDoctor().getSpecialty());
            context.setVariable("doctorRoom", appointment.getDoctor().getRoom());
            context.setVariable("appName", appName);

            String htmlContent = templateEngine.process("email/appointment-reminder", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Appointment Reminder - Tomorrow at " + appointment.getStartTime().format(TIME_FORMATTER));
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Appointment reminder email sent to {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send appointment reminder email", e);
        }
    }

    /**
     * Send appointment cancellation email
     */
    @Async
    public void sendAppointmentCancellation(Appointment appointment) {
        try {
            String to = appointment.getPatient().getEmail();
            if (to == null || to.isEmpty()) {
                log.warn("Patient {} has no email address", appointment.getPatient().getId());
                return;
            }

            Context context = new Context();
            context.setVariable("patientName", appointment.getPatient().getFullName());
            context.setVariable("doctorName", appointment.getDoctor().getUser().getFullName());
            context.setVariable("appointmentDate", appointment.getStartTime().format(DATE_FORMATTER));
            context.setVariable("appointmentTime", appointment.getStartTime().format(TIME_FORMATTER));
            context.setVariable("appName", appName);

            String htmlContent = templateEngine.process("email/appointment-cancellation", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Appointment Cancelled - " + appointment.getStartTime().format(DATE_FORMATTER));
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Appointment cancellation email sent to {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send appointment cancellation email", e);
        }
    }

    /**
     * Send prescription notification email
     */
    @Async
    public void sendPrescriptionNotification(Patient patient, String doctorName, Long prescriptionId) {
        try {
            String to = patient.getEmail();
            if (to == null || to.isEmpty()) {
                log.warn("Patient {} has no email address", patient.getId());
                return;
            }

            Context context = new Context();
            context.setVariable("patientName", patient.getFullName());
            context.setVariable("doctorName", doctorName);
            context.setVariable("prescriptionId", prescriptionId);
            context.setVariable("appName", appName);

            String htmlContent = templateEngine.process("email/prescription-notification", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("New Prescription Available");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Prescription notification email sent to {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send prescription notification email", e);
        }
    }

    /**
     * Send simple text email (fallback)
     */
    @Async
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            log.info("Simple email sent to {}", to);

        } catch (Exception e) {
            log.error("Failed to send simple email", e);
        }
    }

    /**
     * Send welcome email to new patient
     */
    @Async
    public void sendWelcomeEmail(Patient patient) {
        try {
            String to = patient.getEmail();
            if (to == null || to.isEmpty()) {
                log.warn("Patient {} has no email address", patient.getId());
                return;
            }

            Context context = new Context();
            context.setVariable("patientName", patient.getFullName());
            context.setVariable("appName", appName);

            String htmlContent = templateEngine.process("email/welcome", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Welcome to " + appName);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Welcome email sent to {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send welcome email", e);
        }
    }
}
