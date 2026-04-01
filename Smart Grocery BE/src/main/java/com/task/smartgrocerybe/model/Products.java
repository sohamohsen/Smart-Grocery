package com.task.smartgrocerybe.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("is_deleted = false")
public class Products extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    private String description;

    private String brand;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(unique = true)
    private String barcode;

    private boolean isApproved = false;

    private boolean isDeleted = false;

    private LocalDateTime deletedAt;

    private Integer deletedBy;

    private String imageUrl;

    private Integer categoryId;
}
