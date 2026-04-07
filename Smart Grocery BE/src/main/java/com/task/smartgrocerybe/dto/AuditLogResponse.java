package com.task.smartgrocerybe.dto;

import com.task.smartgrocerybe.model.enums.Action;
import com.task.smartgrocerybe.model.enums.EntityType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuditLogResponse {
    private Integer id;
    private Action action;
    private EntityType entityType;
    private String description;
    private String oldValue;
    private String newValue;
    private String ipAddress;
    private LocalDateTime createdAt;
}
