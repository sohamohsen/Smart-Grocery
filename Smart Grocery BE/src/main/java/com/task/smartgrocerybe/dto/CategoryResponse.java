package com.task.smartgrocerybe.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CategoryResponse {
    private Integer id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
}
