package com.clinical.backend.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        System.out.println("Generating BCrypt password hashes for Flyway migration:");
        System.out.println("======================================================");
        
        String adminPassword = "Admin@123";
        String adminHash = encoder.encode(adminPassword);
        System.out.println("Admin password (Admin@123): " + adminHash);
        
        String doctorPassword = "doctor123";
        String doctorHash = encoder.encode(doctorPassword);
        System.out.println("Doctor password (doctor123): " + doctorHash);
        
        String secretaryPassword = "secretary123";
        String secretaryHash = encoder.encode(secretaryPassword);
        System.out.println("Secretary password (secretary123): " + secretaryHash);
        
        System.out.println("\nVerifying hashes...");
        System.out.println("Admin hash valid: " + encoder.matches(adminPassword, adminHash));
        System.out.println("Doctor hash valid: " + encoder.matches(doctorPassword, doctorHash));
        System.out.println("Secretary hash valid: " + encoder.matches(secretaryPassword, secretaryHash));
    }
}
