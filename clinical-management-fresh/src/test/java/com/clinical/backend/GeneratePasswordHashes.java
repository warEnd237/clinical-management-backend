package com.clinical.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
public class GeneratePasswordHashes {
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Test
    public void generateHashes() {
        System.out.println("\n\nGenerating BCrypt password hashes for Flyway migration:");
        System.out.println("======================================================");
        
        String adminPassword = "Admin@123";
        String adminHash = passwordEncoder.encode(adminPassword);
        System.out.println("-- Admin password: Admin@123");
        System.out.println("Admin hash: '" + adminHash + "'");
        
        String doctorPassword = "doctor123";
        String doctorHash = passwordEncoder.encode(doctorPassword);
        System.out.println("\n-- Doctor password: doctor123");
        System.out.println("Doctor hash: '" + doctorHash + "'");
        
        String secretaryPassword = "secretary123";
        String secretaryHash = passwordEncoder.encode(secretaryPassword);
        System.out.println("\n-- Secretary password: secretary123");
        System.out.println("Secretary hash: '" + secretaryHash + "'");
        
        System.out.println("\nVerifying hashes...");
        System.out.println("Admin hash valid: " + passwordEncoder.matches(adminPassword, adminHash));
        System.out.println("Doctor hash valid: " + passwordEncoder.matches(doctorPassword, doctorHash));
        System.out.println("Secretary hash valid: " + passwordEncoder.matches(secretaryPassword, secretaryHash));
        
        // Also verify the existing hashes in the migration file
        String existingAdminHash = "$2a$10$vI8aWBnW3fID.ZQ4/zo1G.q1lRps.9cGLcZEiGDMVr5yUP1T58zRK";
        String existingDoctorHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeXGdXRIkBnLYT.OQBIV9QGqHDEKUUVjO";
        
        System.out.println("\nVerifying existing migration hashes:");
        System.out.println("Existing admin hash matches 'Admin@123': " + passwordEncoder.matches("Admin@123", existingAdminHash));
        System.out.println("Existing doctor hash matches 'doctor123': " + passwordEncoder.matches("doctor123", existingDoctorHash));
        System.out.println("Existing secretary hash matches 'secretary123': " + passwordEncoder.matches("secretary123", existingDoctorHash));
    }
}
