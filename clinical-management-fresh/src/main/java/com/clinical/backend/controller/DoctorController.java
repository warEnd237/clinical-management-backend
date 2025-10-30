package com.clinical.backend.controller;

import com.clinical.backend.dto.common.ApiResponse;
import com.clinical.backend.entity.Doctor;
import com.clinical.backend.repository.DoctorRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
@Tag(name = "Doctors", description = "Doctor management endpoints")
public class DoctorController {
    
    private final DoctorRepository doctorRepository;
    
    @GetMapping
    @Operation(summary = "Get all doctors", description = "Retrieve list of all doctors")
    public ResponseEntity<ApiResponse<List<Doctor>>> getAllDoctors() {
        List<Doctor> doctors = doctorRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success(doctors));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get doctor by ID", description = "Retrieve a specific doctor by their ID")
    public ResponseEntity<ApiResponse<Doctor>> getDoctorById(@PathVariable Long id) {
        return doctorRepository.findById(id)
                .map(doctor -> ResponseEntity.ok(ApiResponse.success(doctor)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search doctors", description = "Search doctors by name or specialty")
    public ResponseEntity<ApiResponse<List<Doctor>>> searchDoctors(@RequestParam String q) {
        List<Doctor> doctors = doctorRepository.searchDoctors(q);
        return ResponseEntity.ok(ApiResponse.success(doctors));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create doctor", description = "Create a new doctor record (Admin only)")
    public ResponseEntity<ApiResponse<Doctor>> createDoctor(@RequestBody Doctor doctor) {
        try {
            Doctor createdDoctor = doctorRepository.save(doctor);
            return ResponseEntity.ok(ApiResponse.success("Doctor created successfully", createdDoctor));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create doctor: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update doctor", description = "Update an existing doctor record (Admin only)")
    public ResponseEntity<ApiResponse<Doctor>> updateDoctor(@PathVariable Long id, @RequestBody Doctor doctorDetails) {
        return doctorRepository.findById(id)
                .map(doctor -> {
                    doctor.setSpecialty(doctorDetails.getSpecialty());
                    doctor.setLicenseNumber(doctorDetails.getLicenseNumber());
                    doctor.setRoom(doctorDetails.getRoom());
                    doctor.setPhone(doctorDetails.getPhone());
                    doctor.setConsultationFeeCents(doctorDetails.getConsultationFeeCents());
                    doctor.setAvailableFrom(doctorDetails.getAvailableFrom());
                    doctor.setAvailableTo(doctorDetails.getAvailableTo());
                    
                    Doctor updatedDoctor = doctorRepository.save(doctor);
                    return ResponseEntity.ok(ApiResponse.success("Doctor updated successfully", updatedDoctor));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete doctor", description = "Delete a doctor record (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteDoctor(@PathVariable Long id) {
        try {
            doctorRepository.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success("Doctor deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete doctor: " + e.getMessage()));
        }
    }
}
