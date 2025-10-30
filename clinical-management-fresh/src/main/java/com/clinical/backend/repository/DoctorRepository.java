package com.clinical.backend.repository;

import com.clinical.backend.entity.Doctor;
import com.clinical.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    
    Optional<Doctor> findByUser(User user);
    
    Optional<Doctor> findByLicenseNumber(String licenseNumber);
    
    List<Doctor> findBySpecialtyContainingIgnoreCase(String specialty);
    
    @Query("SELECT d FROM Doctor d WHERE " +
           "LOWER(d.specialty) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(d.user.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Doctor> searchDoctors(@Param("searchTerm") String searchTerm);
}
