package com.clinical.backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.clinical.backend.entity.Appointment;
import com.clinical.backend.entity.Doctor;
import com.clinical.backend.entity.Patient;
import com.clinical.backend.enums.AppointmentStatus;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    List<Appointment> findByPatient(Patient patient);
    
    List<Appointment> findByDoctor(Doctor doctor);
    
    List<Appointment> findByStatus(AppointmentStatus status);
    
    List<Appointment> findByPatientIdAndStatusIn(Long patientId, List<AppointmentStatus> statuses);
    
    List<Appointment> findByDoctorIdAndStatusIn(Long doctorId, List<AppointmentStatus> statuses);
    
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId " +
           "AND a.status NOT IN ('CANCELLED', 'NO_SHOW') " +
           "AND ((a.startTime < :endTime AND a.endTime > :startTime))")
    List<Appointment> findDoctorConflictingAppointments(
        @Param("doctorId") Long doctorId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
    
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.patient.id = :patientId " +
           "AND a.status NOT IN ('CANCELLED', 'NO_SHOW') " +
           "AND DATE(a.startTime) = DATE(:appointmentDate)")
    long countPatientAppointmentsOnDate(
        @Param("patientId") Long patientId,
        @Param("appointmentDate") LocalDateTime appointmentDate
    );
    
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId " +
           "AND a.startTime >= :startDate " +
           "AND a.startTime < :endDate " +
           "AND a.status NOT IN ('CANCELLED', 'NO_SHOW') " +
           "ORDER BY a.startTime")
    List<Appointment> findDoctorAppointmentsBetween(
        @Param("doctorId") Long doctorId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId " +
           "AND a.startTime >= :startDate " +
           "AND a.startTime < :endDate " +
           "ORDER BY a.startTime")
    List<Appointment> findPatientAppointmentsBetween(
        @Param("patientId") Long patientId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT a FROM Appointment a WHERE " +
           "a.startTime >= :startDate " +
           "AND a.startTime < :endDate " +
           "ORDER BY a.startTime")
    List<Appointment> findAllAppointmentsBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // For appointment reminder scheduler
    List<Appointment> findByStartTimeBetweenAndStatus(
        LocalDateTime startTime, 
        LocalDateTime endTime, 
        AppointmentStatus status
    );
    
    List<Appointment> findByStatusAndStartTimeBefore(
        AppointmentStatus status, 
        LocalDateTime dateTime
    );
    
    List<Appointment> findByStatusAndEndTimeBefore(
        AppointmentStatus status, 
        LocalDateTime dateTime
    );
}
