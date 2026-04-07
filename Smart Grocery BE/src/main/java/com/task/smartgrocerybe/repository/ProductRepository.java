package com.task.smartgrocerybe.repository;

import com.task.smartgrocerybe.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    Page<Product> findByIsApprovedTrue(Pageable pageable);

    Page<Product> findByIsApprovedTrueAndNameContainingIgnoreCase(
            String name, Pageable pageable);

    Page<Product> findByIsApprovedTrueAndCategoryId(
            Integer categoryId, Pageable pageable);

    Page<Product> findByIsApprovedTrueAndBarcodeContaining(
            String barcode, Pageable pageable);

    boolean existsByBarcode(String barcode);

    // ✅ JPQL بدل native → مفيش مشاكل createdAt
    @Query("""
        SELECT p FROM Product p
        WHERE (:isApproved IS NULL OR p.isApproved = :isApproved)
          AND (:isDeleted IS NULL OR p.isDeleted = :isDeleted)
          AND (:barcode IS NULL OR p.barcode LIKE %:barcode%)
          AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:categoryId IS NULL OR p.categoryId = :categoryId)
    """)
    Page<Product> findWithFilters(
            Boolean isApproved,
            Boolean isDeleted,
            String barcode,
            String search,
            Integer categoryId,
            Pageable pageable
    );

    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdIgnoreRestriction(Integer id);

    @Query("SELECT p FROM Product p")
    Page<Product> findAllIgnoreRestriction(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.isDeleted = true")
    Optional<Product> findByIdAndIsDeletedTrue(Integer id);

    @Query("""
        SELECT p FROM Product p
        WHERE p.isDeleted = true
    """)
    Page<Product> findDeletedProducts(Pageable pageable);
}