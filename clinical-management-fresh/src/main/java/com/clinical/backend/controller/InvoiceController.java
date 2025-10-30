package com.clinical.backend.controller;

import com.clinical.backend.dto.common.ApiResponse;
import com.clinical.backend.dto.invoice.InvoiceRequest;
import com.clinical.backend.dto.invoice.InvoiceResponse;
import com.clinical.backend.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoices", description = "Invoice management endpoints")
public class InvoiceController {
    
    private final InvoiceService invoiceService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('SECRETARY', 'ADMIN')")
    @Operation(summary = "Create invoice", description = "Create a new invoice (Secretary/Admin only)")
    public ResponseEntity<ApiResponse<InvoiceResponse>> createInvoice(
            @Valid @RequestBody InvoiceRequest request
    ) {
        InvoiceResponse invoice = invoiceService.createInvoice(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Invoice created successfully", invoice));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get invoice by ID", description = "Retrieve a specific invoice")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceById(@PathVariable Long id) {
        InvoiceResponse invoice = invoiceService.getInvoiceById(id);
        return ResponseEntity.ok(ApiResponse.success(invoice));
    }
    
    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get patient invoices", description = "Get all invoices for a patient")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getPatientInvoices(
            @PathVariable Long patientId
    ) {
        List<InvoiceResponse> invoices = invoiceService.getPatientInvoices(patientId);
        return ResponseEntity.ok(ApiResponse.success(invoices));
    }
    
    @GetMapping("/unpaid")
    @PreAuthorize("hasAnyRole('SECRETARY', 'ADMIN')")
    @Operation(summary = "Get unpaid invoices", description = "Get all unpaid invoices")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getUnpaidInvoices() {
        List<InvoiceResponse> invoices = invoiceService.getUnpaidInvoices();
        return ResponseEntity.ok(ApiResponse.success(invoices));
    }
    
    @PatchMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('SECRETARY', 'ADMIN')")
    @Operation(summary = "Mark invoice as paid", description = "Mark an invoice as paid")
    public ResponseEntity<ApiResponse<InvoiceResponse>> markAsPaid(
            @PathVariable Long id,
            @RequestParam String paymentMethod
    ) {
        InvoiceResponse invoice = invoiceService.markAsPaid(id, paymentMethod);
        return ResponseEntity.ok(ApiResponse.success("Invoice marked as paid", invoice));
    }
    
    @GetMapping("/{id}/pdf")
    @Operation(summary = "Download invoice PDF", description = "Generate and download invoice PDF")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long id) throws IOException {
        byte[] pdfContent = invoiceService.generateInvoicePdf(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "invoice_" + id + ".pdf");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfContent);
    }
}
