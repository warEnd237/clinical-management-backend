package com.clinical.backend.config;

import com.clinical.backend.entity.Doctor;
import com.clinical.backend.entity.Patient;
import com.clinical.backend.entity.User;
import com.clinical.backend.enums.UserRole;
import com.clinical.backend.repository.DoctorRepository;
import com.clinical.backend.repository.PatientRepository;
import com.clinical.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Skip when Flyway is enabled - Flyway handles data initialization
        // This is just a backup for when Flyway is disabled
        if (userRepository.count() > 0) {
            log.info("Database already contains {} users. Skipping DataInitializer.", userRepository.count());
            return;
        }
        
        // Only run when database is empty and Flyway hasn't run
        log.info("WARNING: DataInitializer running - this should only happen when Flyway is disabled!");
            
            // Create Admin User
            User admin = new User();
            admin.setEmail("admin@clinical.com");
            admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
            admin.setFullName("System Administrator");
            admin.setRole(UserRole.ADMIN);
            admin.setIsActive(true);
            userRepository.save(admin);
            
            // Create Doctor User
            User doctorUser = new User();
            doctorUser.setEmail("doctor@clinical.com");
            doctorUser.setPasswordHash(passwordEncoder.encode("doctor123"));
            doctorUser.setFullName("Dr. John Smith");
            doctorUser.setRole(UserRole.DOCTOR);
            doctorUser.setIsActive(true);
            userRepository.save(doctorUser);
            
            // Create Doctor Profile
            Doctor doctor = Doctor.builder()
                    .user(doctorUser)
                    .specialty("Cardiology")
                    .licenseNumber("MD-12345")
                    .room("A101")
                    .phone("+33123456789")
                    .consultationFeeCents(5000) // 50 euros
                    .availableFrom(LocalTime.of(8, 0))
                    .availableTo(LocalTime.of(18, 0))
                    .build();
            doctorRepository.save(doctor);
            
            // Create Secretary User
            User secretaryUser = new User();
            secretaryUser.setEmail("secretary@clinical.com");
            secretaryUser.setPasswordHash(passwordEncoder.encode("secretary123"));
            secretaryUser.setFullName("Mary Johnson");
            secretaryUser.setRole(UserRole.SECRETARY);
            secretaryUser.setIsActive(true);
            userRepository.save(secretaryUser);
            
            // Create Sample Patients
            Patient patient1 = Patient.builder()
                    .firstName("Alice")
                    .lastName("Dupont")
                    .dateOfBirth(LocalDate.of(1985, 6, 15))
                    .gender("Female")
                    .phone("+33987654321")
                    .email("alice.dupont@email.com")
                    .address("123 Rue de la Paix, 75001 Paris")
                    .bloodType("O+")
                    .emergencyContactName("Pierre Dupont")
                    .emergencyContactPhone("+33123987456")
                    .build();
            patientRepository.save(patient1);
            
            Patient patient2 = Patient.builder()
                    .firstName("Bob")
                    .lastName("Martin")
                    .dateOfBirth(LocalDate.of(1978, 3, 22))
                    .gender("Male")
                    .phone("+33654321987")
                    .email("bob.martin@email.com")
                    .address("456 Avenue des Champs, 75008 Paris")
                    .bloodType("A+")
                    .emergencyContactName("Claire Martin")
                    .emergencyContactPhone("+33789123456")
                    .build();
            patientRepository.save(patient2);
            
        log.info("Clinical management data initialized successfully by DataInitializer!");
        log.info("Login credentials:");
        log.info("Admin: admin@clinical.com / Admin@123");
        log.info("Doctor: doctor@clinical.com / doctor123");
        log.info("Secretary: secretary@clinical.com / secretary123");
    }
}
