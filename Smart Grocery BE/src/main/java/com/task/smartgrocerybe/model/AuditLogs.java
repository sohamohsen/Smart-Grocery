package com.task.smartgrocerybe.model;

import com.task.smartgrocerybe.model.enums.Action;
import com.task.smartgrocerybe.model.enums.EntityType;
import com.task.smartgrocerybe.model.enums.Role;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer performedBy;

    private Role userRole;

    private Action action;

    private EntityType entityType;

    private String oldValue;

    private String newValue;

    private String description;

    private String ipAddress;

    private LocalDateTime createAt;



}
