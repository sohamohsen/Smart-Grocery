package com.task.smartgrocerybe.controller;

import com.task.smartgrocerybe.dto.CategoryRequest;
import com.task.smartgrocerybe.dto.CategoryResponse;
import com.task.smartgrocerybe.service.CategoryService;
import com.task.smartgrocerybe.util.ApiResponse;
import com.task.smartgrocerybe.util.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> addCategory(
            @Valid @RequestBody CategoryRequest request) {

        var result = categoryService.addCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category added successfully", result));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Integer id,
            @Valid @RequestBody CategoryRequest request) {

        var result = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(
                ApiResponse.success("Category updated successfully", result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable Integer id) {

        categoryService.deleteCategory(id);
        return ResponseEntity.ok(
                ApiResponse.success("Category deleted successfully", null));
    }

    @PatchMapping("/restore/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> restoreCategory(
            @PathVariable Integer id) {

        var result = categoryService.restoreCategory(id);
        return ResponseEntity.ok(
                ApiResponse.success("Category restored successfully", result));
    }

    @GetMapping("/deleted")
    public ResponseEntity<ApiResponse<PageResponse<CategoryResponse>>> getDeletedCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        var result = categoryService.getDeletedCategories(
                page, size, sortBy, sortDir);
        return ResponseEntity.ok(
                ApiResponse.success("Deleted categories fetched successfully", result));
    }
}