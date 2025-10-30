package com.clinical.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {
    
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
            "status", "UP",
            "database", "PostgreSQL",
            "message", "Clinical Management Backend is RUNNING!",
            "timestamp", System.currentTimeMillis()
        );
    }
}
