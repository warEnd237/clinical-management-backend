package com.clinical.backend.controller;

import com.clinical.backend.entity.User;
import com.clinical.backend.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Tag(name = "Test", description = "Testing and development endpoints")
public class TestController {
    
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    
    @GetMapping("/hash-password")
    @Operation(summary = "Hash password", description = "Hash a password for testing purposes")
    public Map<String, String> hashPassword(@RequestParam String password) {
        String hash = passwordEncoder.encode(password);
        Map<String, String> response = new HashMap<>();
        response.put("password", password);
        response.put("hash", hash);
        return response;
    }
    
    @GetMapping("/verify-password")
    @Operation(summary = "Verify password", description = "Verify a password against a hash")
    public Map<String, Object> verifyPassword(
            @RequestParam String password, 
            @RequestParam String hash) {
        boolean matches = passwordEncoder.matches(password, hash);
        Map<String, Object> response = new HashMap<>();
        response.put("password", password);
        response.put("hash", hash);
        response.put("matches", matches);
        return response;
    }
    
    @GetMapping("/users")
    @Operation(summary = "Get all users", description = "Get all users for testing")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    @GetMapping("/test-login")
    @Operation(summary = "Test login", description = "Test login credentials")
    public Map<String, Object> testLogin(@RequestParam String email, @RequestParam String password) {
        Map<String, Object> response = new HashMap<>();
        
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            response.put("found", false);
            response.put("message", "User not found");
            return response;
        }
        
        boolean matches = passwordEncoder.matches(password, user.getPasswordHash());
        response.put("found", true);
        response.put("email", user.getEmail());
        response.put("storedHash", user.getPasswordHash());
        response.put("inputPassword", password);
        response.put("matches", matches);
        response.put("role", user.getRole().name());
        response.put("active", user.getIsActive());
        
        return response;
    }
}
