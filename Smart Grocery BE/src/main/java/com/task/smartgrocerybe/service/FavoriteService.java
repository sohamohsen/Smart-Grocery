package com.task.smartgrocerybe.service;

import com.task.smartgrocerybe.dto.FavoriteResponse;
import com.task.smartgrocerybe.dto.ProductResponse;
import com.task.smartgrocerybe.exception.ResourceNotFoundException;
import com.task.smartgrocerybe.model.Favorite;
import com.task.smartgrocerybe.model.Product;
import com.task.smartgrocerybe.model.enums.Action;
import com.task.smartgrocerybe.model.enums.EntityType;
import com.task.smartgrocerybe.repository.FavoriteRepository;
import com.task.smartgrocerybe.repository.ProductRepository;
import com.task.smartgrocerybe.service.AuditLogService;
import com.task.smartgrocerybe.service.ProductService;
import com.task.smartgrocerybe.util.ApiResponse;
import com.task.smartgrocerybe.util.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final AuditLogService auditLogService;

    // ─── toggle — add if not exists, remove if exists ────────────────────
    @Transactional
    public ApiResponse<FavoriteResponse> toggleFavorite(Integer productId) {
        Integer userId = getCurrentUserId();

        // validate product exists and is approved
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + productId));

        if (!Boolean.TRUE.equals(product.getIsApproved())) {
            throw new ResourceNotFoundException(
                    "Product not found with id: " + productId);
        }

        // toggle logic
        if (favoriteRepository.existsByUserIdAndProductId(userId, productId)) {
            favoriteRepository.deleteByUserIdAndProductId(userId, productId);

            auditLogService.log(
                    Action.DELETE, EntityType.FAVORITE,
                    "Removed product " + productId + " from wishlist");

            return ApiResponse.success("Product removed from wishlist", null);
        } else {
            Favorite favorite = Favorite.builder()
                    .userId(userId)
                    .productId(productId)
                    .addAt(LocalDateTime.now())
                    .build();

            favoriteRepository.save(favorite);

            auditLogService.log(
                    Action.CREATE, EntityType.FAVORITE,
                    "Added product " + productId + " to wishlist");

            return ApiResponse.success(
                    "Product added to wishlist",
                    mapToResponse(favorite, product));
        }
    }

    // ─── get user wishlist ────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public PageResponse<FavoriteResponse> getWishlist(
            int page, int size, String sortBy, String sortDir) {

        Integer userId = getCurrentUserId();

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Favorite> favorites =
                favoriteRepository.findByUserId(userId, pageable);

        List<FavoriteResponse> content = favorites.getContent()
                .stream()
                .map(fav -> {
                    Product product = productRepository
                            .findById(fav.getProductId())
                            .orElse(null);
                    return product != null
                            ? mapToResponse(fav, product)
                            : null;
                })
                .filter(Objects::nonNull)
                .toList();

        return PageResponse.<FavoriteResponse>builder()
                .content(content)
                .pageNumber(favorites.getNumber())
                .pageSize(favorites.getSize())
                .totalElements(favorites.getTotalElements())
                .totalPages(favorites.getTotalPages())
                .last(favorites.isLast())
                .build();
    }

    // ─── check if product is in wishlist ─────────────────────────────────
    public boolean isInWishlist(Integer productId) {
        Integer userId = getCurrentUserId();
        return favoriteRepository.existsByUserIdAndProductId(userId, productId);
    }

    // ─── Private helpers ──────────────────────────────────────────────────

    private FavoriteResponse mapToResponse(Favorite favorite, Product product) {
        ProductResponse productResponse = productService.mapToResponse(product);

        return FavoriteResponse.builder()
                .id(favorite.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productBrand(product.getBrand())
                .productPrice(product.getPrice())
                .productImageUrl(product.getImageUrl())
                .categoryName(productResponse.getCategoryName())
                .tags(productResponse.getTags())
                .addedAt(favorite.getAddAt())
                .build();
    }

    private Integer getCurrentUserId() {
        // get username from SecurityContext
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        // you need UserRepository here to get the ID
        // inject it or get it from a UserService
        throw new RuntimeException("implement getCurrentUserId");
    }
}