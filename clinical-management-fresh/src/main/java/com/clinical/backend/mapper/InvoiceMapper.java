package com.clinical.backend.mapper;

import com.clinical.backend.dto.invoice.InvoiceResponse;
import com.clinical.backend.entity.Invoice;
import org.springframework.stereotype.Component;

@Component
public class InvoiceMapper {
    
    public InvoiceResponse toResponse(Invoice invoice) {
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .appointmentId(invoice.getAppointment().getId())
                .patientId(invoice.getPatient().getId())
                .patientName(invoice.getPatient().getFirstName() + " " + invoice.getPatient().getLastName())
                .amountCents(invoice.getAmountCents())
                .taxCents(invoice.getTaxCents())
                .totalCents(invoice.getTotalCents())
                .status(invoice.getStatus().name())
                .paymentMethod(invoice.getPaymentMethod())
                .paidAt(invoice.getPaidAt())
                .dueDate(invoice.getDueDate())
                .pdfPath(invoice.getPdfPath())
                .lineItems(invoice.getLineItems())
                .createdAt(invoice.getCreatedAt())
                .build();
    }
}
