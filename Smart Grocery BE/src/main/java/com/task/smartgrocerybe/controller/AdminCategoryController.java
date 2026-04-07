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
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
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

}
