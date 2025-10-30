package com.clinical.backend.service;

import com.clinical.backend.entity.Invoice;
import com.clinical.backend.entity.Prescription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfService {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    
    public byte[] generatePrescriptionPdf(Prescription prescription) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float margin = 50;
                float yPosition = page.getMediaBox().getHeight() - margin;
                float fontSize = 12;
                float leading = 1.5f * fontSize;
                
                // Title
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("MEDICAL PRESCRIPTION");
                contentStream.endText();
                yPosition -= leading * 2;
                
                // Prescription ID and Date
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Prescription ID: " + prescription.getId());
                contentStream.endText();
                yPosition -= leading;
                
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Date: " + prescription.getCreatedAt().format(DATE_FORMATTER));
                contentStream.endText();
                yPosition -= leading * 2;
                
                // Doctor Information
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Doctor Information");
                contentStream.endText();
                yPosition -= leading;
                
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Name: " + prescription.getDoctor().getUser().getFullName());
                contentStream.endText();
                yPosition -= leading;
                
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Specialty: " + prescription.getDoctor().getSpecialty());
                contentStream.endText();
                yPosition -= leading;
                
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("License: " + prescription.getDoctor().getLicenseNumber());
                contentStream.endText();
                yPosition -= leading * 2;
                
                // Patient Information
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Patient Information");
                contentStream.endText();
                yPosition -= leading;
                
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Name: " + prescription.getPatient().getFullName());
                contentStream.endText();
                yPosition -= leading;
                
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("DOB: " + prescription.getPatient().getDateOfBirth().format(DATE_FORMATTER));
                contentStream.endText();
                yPosition -= leading * 2;
                
                // Diagnosis
                if (prescription.getDiagnosis() != null) {
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText("Diagnosis");
                    contentStream.endText();
                    yPosition -= leading;
                    
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText(prescription.getDiagnosis());
                    contentStream.endText();
                    yPosition -= leading * 2;
                }
                
                // Medications
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Medications");
                contentStream.endText();
                yPosition -= leading;
                
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText(prescription.getMedications());
                contentStream.endText();
                yPosition -= leading * 2;
                
                // Instructions
                if (prescription.getInstructions() != null) {
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText("Instructions");
                    contentStream.endText();
                    yPosition -= leading;
                    
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText(prescription.getInstructions());
                    contentStream.endText();
                    yPosition -= leading * 2;
                }
                
                // Valid Until
                if (prescription.getValidUntil() != null) {
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText("Valid Until: " + prescription.getValidUntil().format(DATE_FORMATTER));
                    contentStream.endText();
                }
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }
    
    public byte[] generateInvoicePdf(Invoice invoice) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float margin = 50;
                float yPosition = page.getMediaBox().getHeight() - margin;
                float fontSize = 12;
                float leading = 1.5f * fontSize;
                
                // Title
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("INVOICE");
                contentStream.endText();
                yPosition -= leading * 2;
                
                // Invoice Details
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Invoice ID: " + invoice.getId());
                contentStream.endText();
                yPosition -= leading;
                
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Date: " + invoice.getCreatedAt().format(DATE_FORMATTER));
                contentStream.endText();
                yPosition -= leading;
                
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Status: " + invoice.getStatus());
                contentStream.endText();
                yPosition -= leading * 2;
                
                // Patient Information
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Bill To:");
                contentStream.endText();
                yPosition -= leading;
                
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText(invoice.getPatient().getFullName());
                contentStream.endText();
                yPosition -= leading;
                
                if (invoice.getPatient().getEmail() != null) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText(invoice.getPatient().getEmail());
                    contentStream.endText();
                    yPosition -= leading;
                }
                
                yPosition -= leading;
                
                // Line Items
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Description");
                contentStream.endText();
                
                contentStream.beginText();
                contentStream.newLineAtOffset(400, yPosition);
                contentStream.showText("Amount");
                contentStream.endText();
                yPosition -= leading;
                
                // Draw line
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(page.getMediaBox().getWidth() - margin, yPosition);
                contentStream.stroke();
                yPosition -= leading;
                
                // Service details
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Medical Consultation");
                contentStream.endText();
                
                contentStream.beginText();
                contentStream.newLineAtOffset(400, yPosition);
                contentStream.showText(String.format("$%.2f", invoice.getAmountCents() / 100.0));
                contentStream.endText();
                yPosition -= leading * 2;
                
                // Tax
                if (invoice.getTaxCents() > 0) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText("Tax");
                    contentStream.endText();
                    
                    contentStream.beginText();
                    contentStream.newLineAtOffset(400, yPosition);
                    contentStream.showText(String.format("$%.2f", invoice.getTaxCents() / 100.0));
                    contentStream.endText();
                    yPosition -= leading;
                }
                
                // Draw line
                yPosition -= 5;
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(page.getMediaBox().getWidth() - margin, yPosition);
                contentStream.stroke();
                yPosition -= leading;
                
                // Total
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, yPosition);
                contentStream.showText("Total");
                contentStream.endText();
                
                contentStream.beginText();
                contentStream.newLineAtOffset(400, yPosition);
                contentStream.showText(String.format("$%.2f", invoice.getTotalCents() / 100.0));
                contentStream.endText();
                yPosition -= leading * 2;
                
                // Due Date
                if (invoice.getDueDate() != null) {
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(margin, yPosition);
                    contentStream.showText("Due Date: " + invoice.getDueDate().format(DATE_FORMATTER));
                    contentStream.endText();
                }
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }
}
