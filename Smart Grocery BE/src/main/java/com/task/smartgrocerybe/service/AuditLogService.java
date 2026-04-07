package com.task.smartgrocerybe.service;

import com.task.smartgrocerybe.dto.AuditLogResponse;
import com.task.smartgrocerybe.model.AuditLogs;
import com.task.smartgrocerybe.model.enums.Action;
import com.task.smartgrocerybe.model.enums.EntityType;
import com.task.smartgrocerybe.model.enums.Role;
import com.task.smartgrocerybe.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;

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

            Role role = parseRole(auth);

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

    public Page<AuditLogResponse> getLogsForUser(
            String username, int page, int size, String sortBy, String sortDir) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        return auditLogRepository.findByPerformedBy(username, pageable)
                .map(this::mapToResponse);
    }

    private String getClientIp() {
        String ip = httpServletRequest.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = httpServletRequest.getRemoteAddr();
        }
        return ip;
    }

    private Role parseRole(Authentication auth) {
        if (auth == null || auth.getAuthorities().isEmpty()) {
            return null;
        }

        try {
            String authority = auth.getAuthorities().iterator().next().getAuthority();
            String normalized = authority.startsWith("ROLE_")
                    ? authority.substring(5)
                    : authority;
            return Role.valueOf(normalized.toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            log.warn("Unable to parse role from authentication: {}", ex.getMessage());
            return null;
        }
    }

    private Pageable buildPageable(
            int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        return PageRequest.of(page, size, sort);
    }

    private AuditLogResponse mapToResponse(AuditLogs auditLog) {
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .description(auditLog.getDescription())
                .oldValue(auditLog.getOldValue())
                .newValue(auditLog.getNewValue())
                .ipAddress(auditLog.getIpAddress())
                .createdAt(auditLog.getCreateAt())
                .build();
    }
}
