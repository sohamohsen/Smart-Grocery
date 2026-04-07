package com.task.smartgrocerybe.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminSummaryResponse {
    private Integer id;
    private String name;
    private String username;
    private String email;
    private Boolean active;
    private String role;
    private LocalDateTime createdAt;
}
