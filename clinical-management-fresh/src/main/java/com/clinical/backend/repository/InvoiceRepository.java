package com.clinical.backend.repository;

import com.clinical.backend.entity.Invoice;
import com.clinical.backend.entity.Patient;
import com.clinical.backend.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    List<Invoice> findByPatient(Patient patient);
    
    List<Invoice> findByPatientId(Long patientId);
    
    List<Invoice> findByStatus(InvoiceStatus status);
    
    @Query("SELECT i FROM Invoice i WHERE i.dueDate < :currentDate AND i.status = 'PENDING'")
    List<Invoice> findOverdueInvoices(@Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT i FROM Invoice i WHERE " +
           "i.createdAt >= :startDate AND i.createdAt <= :endDate")
    List<Invoice> findInvoicesBetweenDates(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
