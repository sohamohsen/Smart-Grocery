package com.task.smartgrocerybe.controller;

import com.task.smartgrocerybe.dto.AdminSummaryResponse;
import com.task.smartgrocerybe.dto.AuditLogResponse;
import com.task.smartgrocerybe.dto.CategoryResponse;
import com.task.smartgrocerybe.dto.FavoriteResponse;
import com.task.smartgrocerybe.dto.ProductResponse;
import com.task.smartgrocerybe.dto.RegisterRequest;
import com.task.smartgrocerybe.exception.BadRequestException;
import com.task.smartgrocerybe.service.AuthService;
import com.task.smartgrocerybe.service.AuditLogService;
import com.task.smartgrocerybe.service.CategoryService;
import com.task.smartgrocerybe.service.FavoriteService;
import com.task.smartgrocerybe.service.ProductService;
import com.task.smartgrocerybe.util.ApiResponse;
import com.task.smartgrocerybe.util.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/super-admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
public class SuperAdminController {
    private final CategoryService categoryService;
    private final ProductService productService;
    private final AuthService authService;
    private final AuditLogService auditLogService;
    private final FavoriteService favoriteService;

    @PostMapping("/add-admin")
    public ResponseEntity<ApiResponse<AdminSummaryResponse>> createAdmin(
            @RequestBody RegisterRequest request) {
        var result = authService.createAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Admin added successfully", result));
    }

    @GetMapping("/admins")
    public ResponseEntity<ApiResponse<PageResponse<AdminSummaryResponse>>> getAdmins(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search) {

        var result = authService.getAdmins(page, size, sortBy, sortDir, search);
        return ResponseEntity.ok(
                ApiResponse.success("Admins fetched successfully", result));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PageResponse<AdminSummaryResponse>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search) {

        var result = authService.getUsers(page, size, sortBy, sortDir, search);
        return ResponseEntity.ok(
                ApiResponse.success("Users fetched successfully", result));
    }

    @GetMapping("/admins/{id}/actions")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getAdminActions(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(defaultValue = "createAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        var admin = authService.getAccountSummary(id);
        if (!"ADMIN".equals(admin.getRole())) {
            throw new BadRequestException("Actions can only be viewed for admin accounts");
        }

        Page<AuditLogResponse> logs = auditLogService.getLogsForUser(
                admin.getUsername(), page, size, sortBy, sortDir);

        PageResponse<AuditLogResponse> result = PageResponse.<AuditLogResponse>builder()
                .content(logs.getContent())
                .pageNumber(logs.getNumber())
                .pageSize(logs.getSize())
                .totalElements(logs.getTotalElements())
                .totalPages(logs.getTotalPages())
                .last(logs.isLast())
                .build();

        return ResponseEntity.ok(
                ApiResponse.success("Admin actions fetched successfully", result));
    }

    @GetMapping("/users/{id}/wishlist")
    public ResponseEntity<ApiResponse<PageResponse<FavoriteResponse>>> getUserWishlist(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(defaultValue = "addedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        var result = favoriteService.getWishlistForUser(id, page, size, sortBy, sortDir);
        return ResponseEntity.ok(
                ApiResponse.success("User wishlist fetched successfully", result));
    }

    @PatchMapping("/accounts/{id}/status")
    public ResponseEntity<ApiResponse<AdminSummaryResponse>> updateAccountStatus(
            @PathVariable Integer id,
            @RequestParam boolean active) {

        var result = authService.updateAccountStatus(id, active);
        return ResponseEntity.ok(
                ApiResponse.success("Account status updated successfully", result));
    }

    @DeleteMapping({"/deleted/category{id}", "/deleted/category/{id}"})
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable Integer id) {

        categoryService.deleteCategory(id);
        return ResponseEntity.ok(
                ApiResponse.success("Category deleted successfully", null));
    }

    @PatchMapping("/restore/category/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> restoreCategory(
            @PathVariable Integer id) {

        var result = categoryService.restoreCategory(id);
        return ResponseEntity.ok(
                ApiResponse.success("Category restored successfully", result));
    }

    @GetMapping("/all/category/deleted")
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

    @GetMapping("/category/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getDeletedCategory(
            @PathVariable Integer id) {

        var result = categoryService.getDeletedCategoryById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Category fetched successfully", result));
    }

    @DeleteMapping("/product/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Integer id) {

        productService.deleteProduct(id);
        return ResponseEntity.ok(
                ApiResponse.success("Product deleted successfully", null));
    }

    @PatchMapping("/restore/product/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> restoreProduct(
            @PathVariable Integer id) {

        var result = productService.restoreProduct(id);
        return ResponseEntity.ok(
                ApiResponse.success("Product restored successfully", result));
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getDeletedProduct(
            @PathVariable Integer id) {

        var result = productService.getDeletedProductById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Product fetched successfully", result));
    }

    @GetMapping("/all/deleted/products")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getDeletedProduct(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        var result = productService.getDeletedProduct(
                page, size, sortBy, sortDir);
        return ResponseEntity.ok(
                ApiResponse.success("Deleted products fetched successfully", result));
    }

}
