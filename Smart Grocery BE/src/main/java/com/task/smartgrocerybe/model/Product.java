package com.task.smartgrocerybe.model;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("is_deleted = 0")
public class Product extends BaseEntity {

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

    private Boolean isApproved = false;

    private Boolean isDeleted = false;

    private LocalDateTime deletedAt;

    private Integer deletedBy;

    private String imageUrl;

    private Integer categoryId;
}