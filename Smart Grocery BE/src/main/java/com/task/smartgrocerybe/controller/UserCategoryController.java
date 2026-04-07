package com.task.smartgrocerybe.controller;

import com.task.smartgrocerybe.dto.CategoryResponse;
import com.task.smartgrocerybe.service.CategoryService;
import com.task.smartgrocerybe.util.ApiResponse;
import com.task.smartgrocerybe.util.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
public class UserCategoryController {
    private final CategoryService categoryService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategory(@PathVariable Integer id){

        CategoryResponse response = categoryService.getCategory(id);
        return ResponseEntity.ok(
                ApiResponse.success("Category fetched successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CategoryResponse>>> getCategories(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        PageResponse<CategoryResponse> result = categoryService.getCategories(page, size, sortBy, sortDir, search);
        return ResponseEntity.ok(
                ApiResponse.success("Categories fetched successfully", result));
    }
}
