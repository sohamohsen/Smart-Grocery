package com.task.smartgrocerybe.controller;

import com.task.smartgrocerybe.dto.FavoriteResponse;
import com.task.smartgrocerybe.service.FavoriteService;
import com.task.smartgrocerybe.util.ApiResponse;
import com.task.smartgrocerybe.util.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
public class FavoriteController {

    private final FavoriteService favoriteService;

    // toggle — add or remove from wishlist
    @PostMapping("/{productId}")
    public ResponseEntity<ApiResponse<FavoriteResponse>> toggleFavorite(
            @PathVariable Integer productId) {

        ApiResponse<FavoriteResponse> result =
                favoriteService.toggleFavorite(productId);
        return ResponseEntity.ok(result);
    }

    // get my wishlist
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<FavoriteResponse>>> getWishlist(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "addedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        PageResponse<FavoriteResponse> result =
                favoriteService.getWishlist(page, size, sortBy, sortDir);
        return ResponseEntity.ok(
                ApiResponse.success("Wishlist fetched successfully", result));
    }

    // check if product is in wishlist — useful for Angular to show filled heart
    @GetMapping("/check/{productId}")
    public ResponseEntity<ApiResponse<Boolean>> isInWishlist(
            @PathVariable Integer productId) {

        boolean result = favoriteService.isInWishlist(productId);
        return ResponseEntity.ok(
                ApiResponse.success("Checked successfully", result));
    }
}