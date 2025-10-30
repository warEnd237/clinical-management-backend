package com.clinical.backend.dto.invoice;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceRequest {
    
    @NotNull(message = "Appointment ID is required")
    private Long appointmentId;
    
    @NotNull(message = "Amount is required")
    @Min(value = 0, message = "Amount must be positive")
    private Integer amountCents;
    
    @Builder.Default
    @Min(value = 0, message = "Tax must be positive")
    private Integer taxCents = 0;
    
    private LocalDate dueDate;
    
    private String lineItems;  // JSON string
}
