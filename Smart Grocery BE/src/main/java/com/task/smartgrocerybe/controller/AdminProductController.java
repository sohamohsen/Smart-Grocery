package com.task.smartgrocerybe.controller;

import com.task.smartgrocerybe.dto.ProductRequest;
import com.task.smartgrocerybe.dto.ProductResponse;
import com.task.smartgrocerybe.service.ProductService;
import com.task.smartgrocerybe.util.ApiResponse;
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
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminProductController {

    private final ProductService productService;

    @GetMapping("/fetch/{barcode}")
    public ResponseEntity<ApiResponse<ProductRequest>> fetchSuggestion(
            @PathVariable String barcode,
            @RequestParam BigDecimal price,
            @RequestParam Integer categoryId) {

        var result = productService.fetchSuggestion(barcode, price, categoryId);
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
                .body(ApiResponse.success(result.size() + " products added successfully", result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Integer id) {

        productService.deleteProduct(id);
        return ResponseEntity.ok(
                ApiResponse.success("Product deleted successfully", null));
    }
}