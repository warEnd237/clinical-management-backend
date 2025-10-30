package com.clinical.backend.controller;

import com.clinical.backend.dto.common.ApiResponse;
import com.clinical.backend.dto.patient.PatientRequest;
import com.clinical.backend.dto.patient.PatientResponse;
import com.clinical.backend.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@Tag(name = "Patients", description = "Patient management endpoints")
public class PatientController {
    
    private final PatientService patientService;
    
    @GetMapping
    @Operation(summary = "Get all patients", description = "Retrieve paginated list of all patients")
    public ResponseEntity<ApiResponse<Page<PatientResponse>>> getAllPatients(
            @PageableDefault(size = 20, sort = "lastName", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<PatientResponse> patients = patientService.getAllPatients(pageable);
        return ResponseEntity.ok(ApiResponse.success(patients));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get patient by ID", description = "Retrieve a specific patient by their ID")
    public ResponseEntity<ApiResponse<PatientResponse>> getPatientById(@PathVariable Long id) {
        PatientResponse patient = patientService.getPatientById(id);
        return ResponseEntity.ok(ApiResponse.success(patient));
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search patients", description = "Search patients by name or email")
    public ResponseEntity<ApiResponse<Page<PatientResponse>>> searchPatients(
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<PatientResponse> patients = patientService.searchPatients(query, pageable);
        return ResponseEntity.ok(ApiResponse.success(patients));
    }
    
    @PostMapping
    @Operation(summary = "Create patient", description = "Create a new patient record")
    public ResponseEntity<ApiResponse<PatientResponse>> createPatient(@Valid @RequestBody PatientRequest request) {
        PatientResponse patient = patientService.createPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Patient created successfully", patient));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update patient", description = "Update an existing patient record")
    public ResponseEntity<ApiResponse<PatientResponse>> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientRequest request
    ) {
        PatientResponse patient = patientService.updatePatient(id, request);
        return ResponseEntity.ok(ApiResponse.success("Patient updated successfully", patient));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete patient", description = "Delete a patient record (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.ok(ApiResponse.success("Patient deleted successfully", null));
    }
}
