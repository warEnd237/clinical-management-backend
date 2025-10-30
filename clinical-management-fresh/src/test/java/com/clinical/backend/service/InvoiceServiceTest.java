package com.clinical.backend.service;

import com.clinical.backend.dto.invoice.InvoiceRequest;
import com.clinical.backend.dto.invoice.InvoiceResponse;
import com.clinical.backend.entity.Appointment;
import com.clinical.backend.entity.Doctor;
import com.clinical.backend.entity.Invoice;
import com.clinical.backend.entity.Patient;
import com.clinical.backend.entity.User;
import com.clinical.backend.enums.AppointmentStatus;
import com.clinical.backend.enums.InvoiceStatus;
import com.clinical.backend.enums.UserRole;
import com.clinical.backend.exception.ResourceNotFoundException;
import com.clinical.backend.mapper.InvoiceMapper;
import com.clinical.backend.repository.AppointmentRepository;
import com.clinical.backend.repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Invoice Service Tests")
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private InvoiceMapper invoiceMapper;

    @Mock
    private PdfService pdfService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private InvoiceService invoiceService;

    private Patient testPatient;
    private Doctor testDoctor;
    private Appointment testAppointment;
    private Invoice testInvoice;
    private InvoiceRequest invoiceRequest;

    @BeforeEach
    void setUp() {
        // Create test patient
        testPatient = Patient.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .phone("+123456789")
                .email("john.doe@test.com")
                .build();

        // Create test doctor user
        User doctorUser = new User();
        doctorUser.setId(1L);
        doctorUser.setEmail("doctor@test.com");
        doctorUser.setFullName("Dr. Smith");
        doctorUser.setRole(UserRole.DOCTOR);

        // Create test doctor
        testDoctor = Doctor.builder()
                .id(1L)
                .user(doctorUser)
                .specialty("Cardiology")
                .licenseNumber("TEST123")
                .build();

        // Create test appointment
        testAppointment = Appointment.builder()
                .id(1L)
                .patient(testPatient)
                .doctor(testDoctor)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .status(AppointmentStatus.SCHEDULED)
                .reason("Consultation")
                .build();

        // Create test invoice
        testInvoice = Invoice.builder()
                .id(1L)
                .appointment(testAppointment)
                .patient(testPatient)
                .amountCents(10000) // $100.00
                .taxCents(1000) // $10.00
                .totalCents(11000) // $110.00
                .status(InvoiceStatus.PENDING)
                .dueDate(LocalDate.now().plusDays(30))
                .createdAt(LocalDateTime.now())
                .build();

        // Create test request
        invoiceRequest = new InvoiceRequest();
        invoiceRequest.setAppointmentId(1L);
        invoiceRequest.setAmountCents(10000);
        invoiceRequest.setTaxCents(1000);
        invoiceRequest.setDueDate(LocalDate.now().plusDays(30));
        invoiceRequest.setLineItems("{\"consultation\": 100}");
    }

    @Test
    @DisplayName("Should create invoice successfully")
    void testCreateInvoiceSuccess() {
        // Arrange
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);
        when(invoiceMapper.toResponse(any(Invoice.class))).thenReturn(new InvoiceResponse());

        // Act
        InvoiceResponse response = invoiceService.createInvoice(invoiceRequest);

        // Assert
        assertNotNull(response);
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
        verify(auditService, times(1)).logCreate("Invoice", 1L);
    }

    @Test
    @DisplayName("Should throw exception when appointment not found")
    void testCreateInvoiceAppointmentNotFound() {
        // Arrange
        when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());
        invoiceRequest.setAppointmentId(999L);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            invoiceService.createInvoice(invoiceRequest);
        });

        verify(invoiceRepository, never()).save(any(Invoice.class));
    }

    @Test
    @DisplayName("Should create invoice with zero tax when tax is null")
    void testCreateInvoiceWithNullTax() {
        // Arrange
        invoiceRequest.setTaxCents(null);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice saved = invocation.getArgument(0);
            assertEquals(0, saved.getTaxCents());
            assertEquals(10000, saved.getTotalCents()); // amount + 0 tax
            return testInvoice;
        });
        when(invoiceMapper.toResponse(any())).thenReturn(new InvoiceResponse());

        // Act
        invoiceService.createInvoice(invoiceRequest);

        // Assert
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    @DisplayName("Should get invoice by ID successfully")
    void testGetInvoiceByIdSuccess() {
        // Arrange
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));

        // Act
        InvoiceResponse response = invoiceService.getInvoiceById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(10000, response.getAmountCents());
    }

    @Test
    @DisplayName("Should throw exception when invoice not found by ID")
    void testGetInvoiceByIdNotFound() {
        // Arrange
        when(invoiceRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            invoiceService.getInvoiceById(999L);
        });
    }

    @Test
    @DisplayName("Should get all invoices for a patient")
    void testGetPatientInvoices() {
        // Arrange
        Invoice invoice2 = Invoice.builder()
                .id(2L)
                .appointment(testAppointment)
                .patient(testPatient)
                .amountCents(5000)
                .taxCents(500)
                .totalCents(5500)
                .status(InvoiceStatus.PAID)
                .build();

        when(invoiceRepository.findByPatientId(1L))
                .thenReturn(Arrays.asList(testInvoice, invoice2));

        // Act
        List<InvoiceResponse> responses = invoiceService.getPatientInvoices(1L);

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(invoiceRepository, times(1)).findByPatientId(1L);
    }

    @Test
    @DisplayName("Should return empty list when patient has no invoices")
    void testGetPatientInvoicesEmpty() {
        // Arrange
        when(invoiceRepository.findByPatientId(1L)).thenReturn(Arrays.asList());

        // Act
        List<InvoiceResponse> responses = invoiceService.getPatientInvoices(1L);

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    @DisplayName("Should get all unpaid invoices")
    void testGetUnpaidInvoices() {
        // Arrange
        Invoice unpaidInvoice2 = Invoice.builder()
                .id(2L)
                .appointment(testAppointment)
                .patient(testPatient)
                .amountCents(20000)
                .status(InvoiceStatus.PENDING)
                .build();

        when(invoiceRepository.findByStatus(InvoiceStatus.PENDING))
                .thenReturn(Arrays.asList(testInvoice, unpaidInvoice2));

        // Act
        List<InvoiceResponse> responses = invoiceService.getUnpaidInvoices();

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
    }

    @Test
    @DisplayName("Should mark invoice as paid successfully")
    void testMarkAsPaidSuccess() {
        // Arrange
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice saved = invocation.getArgument(0);
            assertEquals(InvoiceStatus.PAID, saved.getStatus());
            assertEquals("Credit Card", saved.getPaymentMethod());
            assertNotNull(saved.getPaidAt());
            return saved;
        });

        // Act
        InvoiceResponse response = invoiceService.markAsPaid(1L, "Credit Card");

        // Assert
        assertNotNull(response);
        assertEquals("PAID", response.getStatus());
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }

    @Test
    @DisplayName("Should throw exception when marking non-existent invoice as paid")
    void testMarkAsPaidNotFound() {
        // Arrange
        when(invoiceRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            invoiceService.markAsPaid(999L, "Cash");
        });
    }

    @Test
    @DisplayName("Should throw exception for PDF generation (not implemented)")
    void testGenerateInvoicePdfNotImplemented() {
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            invoiceService.generateInvoicePdf(1L);
        });

        assertTrue(exception.getMessage().contains("not implemented"));
    }

    @Test
    @DisplayName("Should calculate total correctly with tax")
    void testTotalCalculationWithTax() {
        // Arrange
        invoiceRequest.setAmountCents(15000); // $150
        invoiceRequest.setTaxCents(1500); // $15

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(testAppointment));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> {
            Invoice saved = invocation.getArgument(0);
            assertEquals(16500, saved.getTotalCents()); // $165
            return testInvoice;
        });
        when(invoiceMapper.toResponse(any())).thenReturn(new InvoiceResponse());

        // Act
        invoiceService.createInvoice(invoiceRequest);

        // Assert
        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    @DisplayName("Should include patient name in response")
    void testResponseIncludesPatientName() {
        // Arrange
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));

        // Act
        InvoiceResponse response = invoiceService.getInvoiceById(1L);

        // Assert
        assertNotNull(response);
        assertEquals("John Doe", response.getPatientName());
    }
}
