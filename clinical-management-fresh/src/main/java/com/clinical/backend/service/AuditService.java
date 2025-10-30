package com.clinical.backend.service;

import com.clinical.backend.entity.AuditLog;
import com.clinical.backend.entity.User;
import com.clinical.backend.repository.AuditLogRepository;
import com.clinical.backend.repository.UserRepository;
import com.clinical.backend.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class AuditService {
    
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public void logAccess(String entityType, Long entityId, String action) {
        createAuditLog(action, entityType, entityId, null);
    }
    
    @Transactional
    public void logCreate(String entityType, Long entityId) {
        createAuditLog("CREATE", entityType, entityId, null);
    }
    
    @Transactional
    public void logUpdate(String entityType, Long entityId) {
        createAuditLog("UPDATE", entityType, entityId, null);
    }
    
    @Transactional
    public void logDelete(String entityType, Long entityId) {
        createAuditLog("DELETE", entityType, entityId, null);
    }
    
    private void createAuditLog(String action, String entityType, Long entityId, String changes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                User user = userRepository.findById(userDetails.getId()).orElse(null);
                
                HttpServletRequest request = getCurrentRequest();
                
                AuditLog auditLog = AuditLog.builder()
                        .user(user)
                        .action(action)
                        .entityType(entityType)
                        .entityId(entityId)
                        .changes(changes)
                        .ipAddress(request != null ? getClientIP(request) : null)
                        .userAgent(request != null ? request.getHeader("User-Agent") : null)
                        .build();
                
                auditLogRepository.save(auditLog);
            }
        } catch (Exception e) {
            // Log silently, don't disrupt main flow
        }
    }
    
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
    
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
