package com.task.smartgrocerybe.repository;

import com.task.smartgrocerybe.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    boolean existsByName(String name);

    @Query("SELECT c FROM Category c WHERE c.isDeleted = false")
    Page<Category> findAllAndIsDeletedFalse(Pageable pageable);

    @Query("SELECT c FROM Category c WHERE c.isDeleted = false " +
            "AND LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Category> findByNameContainingIgnoreCaseAndIsDeletedFalse(
            @Param("search") String search, Pageable pageable);

    @Query("SELECT c FROM Category c WHERE c.isDeleted = true")
    Page<Category> findAllDeleted(Pageable pageable);

    @Query(value = "SELECT * FROM categories WHERE id = :id",
            nativeQuery = true)
    Optional<Category> findByIdIgnoreRestriction(@Param("id") Integer id);
}