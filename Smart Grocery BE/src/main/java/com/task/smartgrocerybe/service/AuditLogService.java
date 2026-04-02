package com.task.smartgrocerybe.service;

import com.task.smartgrocerybe.model.AuditLogs;
import com.task.smartgrocerybe.model.enums.Action;
import com.task.smartgrocerybe.model.enums.EntityType;
import com.task.smartgrocerybe.model.enums.Role;
import com.task.smartgrocerybe.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final HttpServletRequest httpServletRequest;

    public void log(Action action,
                    EntityType entityType,
                    String description) {
        log(action, entityType, description, null, null);
    }

    public void log(Action action,
                    EntityType entityType,
                    String description,
                    String oldValue,
                    String newValue) {
        try {
            Authentication auth = SecurityContextHolder
                    .getContext().getAuthentication();

            String username = (auth != null && auth.isAuthenticated())
                    ? auth.getName()
                    : "system";

            Role role = Role.valueOf((auth != null && !auth.getAuthorities().isEmpty())
                    ? auth.getAuthorities().iterator().next().getAuthority()
                    : "UNKNOWN");

            String ipAddress = getClientIp();

            AuditLogs auditLog = AuditLogs.builder()
                    .performedBy(username)
                    .userRole(role)
                    .action(action)
                    .entityType(entityType)
                    .description(description)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .ipAddress(ipAddress)
                    .createAt(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);

        } catch (Exception e) {
            // never let audit logging break the main flow
            log.error("Failed to save audit log: {}", e.getMessage());
        }
    }

    private String getClientIp() {
        String ip = httpServletRequest.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = httpServletRequest.getRemoteAddr();
        }
        return ip;
    }
}