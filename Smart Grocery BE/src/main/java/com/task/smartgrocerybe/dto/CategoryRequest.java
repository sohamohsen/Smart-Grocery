package com.task.smartgrocerybe.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;
}
