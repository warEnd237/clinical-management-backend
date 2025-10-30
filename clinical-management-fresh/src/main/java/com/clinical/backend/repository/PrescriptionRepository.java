package com.clinical.backend.repository;

import com.clinical.backend.entity.Prescription;
import com.clinical.backend.entity.Patient;
import com.clinical.backend.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    
    List<Prescription> findByPatient(Patient patient);
    
    List<Prescription> findByPatientId(Long patientId);
    
    List<Prescription> findByDoctor(Doctor doctor);
    
    @Query("SELECT p FROM Prescription p WHERE p.validUntil >= :currentDate")
    List<Prescription> findValidPrescriptions(@Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT p FROM Prescription p WHERE p.patient = :patient AND p.validUntil >= :currentDate")
    List<Prescription> findValidPrescriptionsByPatient(
            @Param("patient") Patient patient,
            @Param("currentDate") LocalDate currentDate);
}
