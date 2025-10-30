package com.clinical.backend.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    
    private String error;
    private String message;
    private int status;
    private String path;
    private Map<String, String> validationErrors;
    
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
