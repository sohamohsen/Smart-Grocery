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

    @Query(value = """
    SELECT *
    FROM products p
    WHERE (:isApproved IS NULL OR p.is_approved = :isApproved)
      AND (:isDeleted IS NULL OR p.is_deleted = :isDeleted)
      AND (:barcode IS NULL OR p.barcode LIKE CONCAT('%', :barcode, '%'))
      AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
      AND (:categoryId IS NULL OR p.category_id = :categoryId)
    ORDER BY p.created_at DESC
""",
            countQuery = """
    SELECT COUNT(*)
    FROM products p
    WHERE (:isApproved IS NULL OR p.is_approved = :isApproved)
      AND (:isDeleted IS NULL OR p.is_deleted = :isDeleted)
      AND (:barcode IS NULL OR p.barcode LIKE CONCAT('%', :barcode, '%'))
      AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')))
      AND (:categoryId IS NULL OR p.category_id = :categoryId)
""",
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

    @Query(value = "SELECT * FROM products WHERE id = :id AND is_deleted = true",
            nativeQuery = true)
    Optional<Product> findByIdAndIsDeletedTrue(@Param("id") Integer id);

    @Query(value = """
    SELECT *
    FROM products
    WHERE is_deleted = true
    ORDER BY created_at DESC
""",
            countQuery = """
    SELECT COUNT(*)
    FROM products
    WHERE is_deleted = true
""",
            nativeQuery = true)
    Page<Product> findDeletedProducts(Pageable pageable);
}
