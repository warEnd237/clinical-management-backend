package com.clinical.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * TEMPORARY CONTROLLER - Remove after fixing password hashes
 * Used to generate correct BCrypt hashes and test password matching
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class PasswordTestController {
    
    private final PasswordEncoder passwordEncoder;
    
    @GetMapping("/generate-hashes")
    public Map<String, String> generatePasswordHashes() {
        Map<String, String> hashes = new HashMap<>();
        
        String adminPassword = "Admin@123";
        String doctorPassword = "doctor123";
        String secretaryPassword = "secretary123";
        
        hashes.put("admin_password", adminPassword);
        hashes.put("admin_hash", passwordEncoder.encode(adminPassword));
        
        hashes.put("doctor_password", doctorPassword);
        hashes.put("doctor_hash", passwordEncoder.encode(doctorPassword));
        
        hashes.put("secretary_password", secretaryPassword);
        hashes.put("secretary_hash", passwordEncoder.encode(secretaryPassword));
        
        return hashes;
    }
    
    @PostMapping("/verify-password")
    public Map<String, Object> verifyPassword(@RequestParam String password, @RequestParam String hash) {
        Map<String, Object> result = new HashMap<>();
        result.put("password", password);
        result.put("hash", hash);
        result.put("matches", passwordEncoder.matches(password, hash));
        return result;
    }
    
    @GetMapping("/test-existing-hashes")
    public Map<String, Object> testExistingHashes() {
        Map<String, Object> results = new HashMap<>();
        
        // Test the hashes currently in the migration file
        String currentAdminHash = "$2a$10$rBV2JDeWW3.vKyeQcM8fFO4777l.ZDRkh1XfIbzkXh3p.XQv4okcy";
        String currentDoctorHash = "$2a$10$6P1To7kR1PXJmiHYJGXRLOHUabRq5v6tMQPfRTfXES0zGQqvNFUt2";
        String currentSecretaryHash = "$2a$10$J9x8x9Gq0k1n0lZ1TA9a3eYYFqXJXdtrx2RP.WQhs6J6ZeVb0Puoi";
        
        results.put("admin_hash_valid", passwordEncoder.matches("Admin@123", currentAdminHash));
        results.put("doctor_hash_valid", passwordEncoder.matches("doctor123", currentDoctorHash));
        results.put("secretary_hash_valid", passwordEncoder.matches("secretary123", currentSecretaryHash));
        
        return results;
    }
}
