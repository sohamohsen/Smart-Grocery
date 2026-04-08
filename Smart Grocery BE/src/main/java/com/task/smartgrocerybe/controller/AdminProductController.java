package com.task.smartgrocerybe.controller;

import com.task.smartgrocerybe.dto.ProductRequest;
import com.task.smartgrocerybe.dto.ProductResponse;
import com.task.smartgrocerybe.service.ProductService;
import com.task.smartgrocerybe.util.ApiResponse;
import com.task.smartgrocerybe.util.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/admin/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPER_ADMIN')")
public class AdminProductController {

    private final ProductService productService;

    @GetMapping("/fetch")
    public ResponseEntity<ApiResponse<ProductRequest>> fetchSuggestion(
            @RequestParam(required = false) String barcode) {

        var result = productService.fetchSuggestion(barcode);
        return ResponseEntity.ok(
                ApiResponse.success("Product fetched successfully", result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> addProduct(
            @Valid @RequestBody ProductRequest request) {

        var result = productService.addProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product added successfully", result));
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> bulkAddProducts(
            @Valid @RequestBody List<ProductRequest> requests) {

        var result = productService.bulkAddProducts(requests);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        result.size() + " products added successfully", result));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Integer id,
            @RequestBody ProductRequest request) {

        var result = productService.updateProduct(id, request);
        return ResponseEntity.ok(
                ApiResponse.success("Product updated successfully", result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Integer id) {

        productService.deleteProduct(id);
        return ResponseEntity.ok(
                ApiResponse.success("Product deleted successfully", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String barcode,
            @RequestParam(required = false) Boolean isApproved,
            @RequestParam(required = false) Boolean isDeleted,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        var result = productService.getProducts(
                page, size, sortBy, sortDir,
                search, categoryId, barcode,
                isApproved, isDeleted);

        return ResponseEntity.ok(
                ApiResponse.success("Products fetched successfully", result));
    }

}
