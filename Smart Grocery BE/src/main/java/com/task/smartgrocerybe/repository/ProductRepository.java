package com.task.smartgrocerybe.repository;

import com.task.smartgrocerybe.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query(value = "SELECT * FROM products WHERE " +
            "(:isApproved IS NULL OR is_approved = :isApproved) AND " +
            "(:isDeleted IS NULL OR is_deleted = :isDeleted) AND " +
            "(:barcode IS NULL OR barcode LIKE %:barcode%) AND " +
            "(:search IS NULL OR name LIKE %:search%) AND " +
            "(:categoryId IS NULL OR category_id = :categoryId)",
            countQuery = "SELECT COUNT(*) FROM products WHERE " +
                    "(:isApproved IS NULL OR is_approved = :isApproved) AND " +
                    "(:isDeleted IS NULL OR is_deleted = :isDeleted) AND " +
                    "(:barcode IS NULL OR barcode LIKE %:barcode%) AND " +
                    "(:search IS NULL OR name LIKE %:search%) AND " +
                    "(:categoryId IS NULL OR category_id = :categoryId)",
            nativeQuery = true)
    Page<Product> findWithFilters(
            @Param("isApproved") Boolean isApproved,
            @Param("isDeleted") Boolean isDeleted,
            @Param("barcode") String barcode,
            @Param("search") String search,
            @Param("categoryId") Integer categoryId,
            Pageable pageable);

    @Query(value = "SELECT * FROM products WHERE id = :id",
            nativeQuery = true)
    Optional<Product> findByIdIgnoreRestriction(@Param("id") Integer id);

    @Query(value = "SELECT * FROM products",
            countQuery = "SELECT COUNT(*) FROM products",
            nativeQuery = true)
    Page<Product> findAllIgnoreRestriction(Pageable pageable);
}