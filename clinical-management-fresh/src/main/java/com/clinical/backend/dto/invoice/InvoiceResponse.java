package com.clinical.backend.dto.invoice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponse {
    
    private Long id;
    private Long appointmentId;
    private Long patientId;
    private String patientName;
    private Integer amountCents;
    private Integer taxCents;
    private Integer totalCents;
    private String status;
    private String paymentMethod;
    private LocalDateTime paidAt;
    private LocalDate dueDate;
    private String pdfPath;
    private String lineItems;
    private LocalDateTime createdAt;
}
