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
public class FavoriteResponse {
    private Integer id;
    private Integer productId;
    private String productName;
    private String productBrand;
    private BigDecimal productPrice;
    private String productImageUrl;
    private String categoryName;
    private List<String> tags;
    private LocalDateTime addedAt;
}