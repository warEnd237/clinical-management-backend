package com.clinical.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Home Controller - Redirects root URL to Swagger UI
 */
@Controller
public class HomeController {
    
    /**
     * Redirect root URL to Swagger UI documentation
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/swagger-ui/";
    }
}
