package com.task.smartgrocerybe.controller;

import com.task.smartgrocerybe.dto.ProductResponse;
import com.task.smartgrocerybe.service.ProductService;
import com.task.smartgrocerybe.util.ApiResponse;
import com.task.smartgrocerybe.util.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
public class UserProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String barcode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        var result = productService.getApprovedProducts(
                page, size, sortBy, sortDir,
                search, categoryId, barcode);

        return ResponseEntity.ok(
                ApiResponse.success("Products fetched successfully", result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(
            @PathVariable Integer id) {

        var result = productService.getProductById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Product fetched successfully", result));
    }
}