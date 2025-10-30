package com.clinical.backend.service;

import com.clinical.backend.dto.invoice.InvoiceRequest;
import com.clinical.backend.dto.invoice.InvoiceResponse;
import com.clinical.backend.entity.Appointment;
import com.clinical.backend.entity.Invoice;
import com.clinical.backend.enums.InvoiceStatus;
import com.clinical.backend.exception.ResourceNotFoundException;
import com.clinical.backend.mapper.InvoiceMapper;
import com.clinical.backend.repository.AppointmentRepository;
import com.clinical.backend.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    
    private final InvoiceRepository invoiceRepository;
    private final AppointmentRepository appointmentRepository;
    private final InvoiceMapper invoiceMapper;
    private final PdfService pdfService;
    private final AuditService auditService;
    
    @Transactional
    public InvoiceResponse createInvoice(InvoiceRequest request) {
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));
        
        int totalCents = request.getAmountCents() + (request.getTaxCents() != null ? request.getTaxCents() : 0);
        
        Invoice invoice = Invoice.builder()
                .appointment(appointment)
                .patient(appointment.getPatient())
                .amountCents(request.getAmountCents())
                .taxCents(request.getTaxCents() != null ? request.getTaxCents() : 0)
                .totalCents(totalCents)
                .status(InvoiceStatus.PENDING)
                .dueDate(request.getDueDate())
                .lineItems(request.getLineItems())
                .build();
        
        Invoice savedInvoice = invoiceRepository.save(invoice);
        
        auditService.logCreate("Invoice", savedInvoice.getId());
        
        return invoiceMapper.toResponse(savedInvoice);
    }
    
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        
        return toResponse(invoice);
    }
    
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getPatientInvoices(Long patientId) {
        return invoiceRepository.findByPatientId(patientId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getUnpaidInvoices() {
        return invoiceRepository.findByStatus(InvoiceStatus.PENDING).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public InvoiceResponse markAsPaid(Long id, String paymentMethod) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaymentMethod(paymentMethod);
        invoice.setPaidAt(LocalDateTime.now());
        
        Invoice savedInvoice = invoiceRepository.save(invoice);
        
        return toResponse(savedInvoice);
    }
    
    public byte[] generateInvoicePdf(Long id) {
        // TODO: Implement PDF generation using PDFBox
        throw new RuntimeException("PDF generation not implemented yet");
    }
    
    private InvoiceResponse toResponse(Invoice invoice) {
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
