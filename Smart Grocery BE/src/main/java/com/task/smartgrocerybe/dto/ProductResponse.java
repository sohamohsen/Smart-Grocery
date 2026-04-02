package com.task.smartgrocerybe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Integer id;
    private String name;
    private String description;
    private String brand;
    private BigDecimal price;
    private String barcode;
    private String imageUrl;
    private boolean isApproved;
    private Integer categoryId;
    private String categoryName;
    private List<String> tags;
    private LocalDateTime createdAt;
}