package com.clinical.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class SimpleTestController {
    
    @GetMapping("/status")
    public Map<String, String> getStatus() {
        return Map.of(
            "status", "RUNNING", 
            "database", "PostgreSQL", 
            "message", "Clinical Management Fresh is UP!"
        );
    }
}
