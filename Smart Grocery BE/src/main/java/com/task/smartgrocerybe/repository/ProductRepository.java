package com.task.smartgrocerybe.repository;

import com.task.smartgrocerybe.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    boolean existsByBarcode(String barcode);

    Page<Product> findByIsApprovedTrue(Pageable pageable);

    Page<Product> findByIsApprovedTrueAndNameContainingIgnoreCase(String search, Pageable pageable);

    Page<Product> findByIsApprovedTrueAndCategoryId(Integer categoryId, Pageable pageable);
}
