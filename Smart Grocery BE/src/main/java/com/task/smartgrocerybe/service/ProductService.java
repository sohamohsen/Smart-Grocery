package com.task.smartgrocerybe.service;

import com.task.smartgrocerybe.dto.ProductRequest;
import com.task.smartgrocerybe.dto.ProductResponse;
import com.task.smartgrocerybe.dto.external.OpenFoodFactsResponse;
import com.task.smartgrocerybe.exception.ResourceAlreadyExistsException;
import com.task.smartgrocerybe.exception.ResourceNotFoundException;
import com.task.smartgrocerybe.model.Category;
import com.task.smartgrocerybe.model.Product;
import com.task.smartgrocerybe.model.ProductTags;
import com.task.smartgrocerybe.model.Tag;
import com.task.smartgrocerybe.model.enums.Action;
import com.task.smartgrocerybe.model.enums.EntityType;
import com.task.smartgrocerybe.repository.*;
import com.task.smartgrocerybe.util.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ProductTagRepository productTagRepository;
    private final OpenFoodFactsService openFoodFactsService;
    private final AuditLogService auditLogService;

    // ─── Admin-Only: ─────────────────────────────────────────────────

    public ProductRequest fetchSuggestion(
            String barcode, BigDecimal price, Integer categoryId) {

        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category not found");
        }

        OpenFoodFactsResponse response =
                openFoodFactsService.fetchByBarcode(barcode);

        return mapOpenFoodFactsToProductRequest(response, price, categoryId);
    }

    @Transactional
    public ProductResponse addProduct(ProductRequest request) {

        if (request.getBarcode() != null &&
                productRepository.existsByBarcode(request.getBarcode())) {
            throw new ResourceAlreadyExistsException(
                    "Product with barcode already exists: " + request.getBarcode());
        }

        if (!categoryRepository.existsById(request.getCategoryId())) {
            throw new ResourceNotFoundException("Category not found");
        }

        Product product = buildProduct(request);
        Product saved = productRepository.save(product);

        if (request.getTags() != null && !request.getTags().isEmpty()) {
            saveTags(saved.getId(), request.getTags());
        }

        auditLogService.log(
                Action.CREATE, EntityType.PRODUCT, "Added product: " + saved.getName());

        return mapToResponse(saved);
    }

    @Transactional
    public List<ProductResponse> bulkAddProducts(List<ProductRequest> requests) {
        return requests.stream()
                .map(this::addProduct)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteProduct(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id));

        product.setIsDeleted(true);
        product.setDeletedAt(LocalDateTime.now());
        productRepository.save(product);

        auditLogService.log(
                Action.DELETE, EntityType.PRODUCT, "Soft deleted product: " + product.getName());
    }

    // ─── Admin-User  ─────────────────────────────────────────────────

    public PageResponse<ProductResponse> getApprovedProducts(
            Integer page, Integer size, String sortBy, String sortDir, String search, Integer categoryId) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> products;

        if (search != null && !search.isBlank()) {
            products = productRepository
                    .findByIsApprovedTrueAndNameContainingIgnoreCase(
                            search, pageable);
        } else if (categoryId != null) {
            products = productRepository
                    .findByIsApprovedTrueAndCategoryId(categoryId, pageable);
        } else {
            products = productRepository.findByIsApprovedTrue(pageable);
        }

        List<ProductResponse> content =
                products.getContent()
                        .stream()
                        .map(this::mapToResponse)
                        .toList();


        return PageResponse.<ProductResponse>builder()
                .content(content)
                .pageNumber(products.getNumber())
                .pageSize(products.getSize())
                .totalElements(products.getTotalElements())
                .totalPages(products.getTotalPages())
                .last(products.isLast())
                .build();
    }

    public ProductResponse getProductById(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id));
        return mapToResponse(product);
    }

    // ─── Private helpers ─────────────────────────────────────────────────

    private Product buildProduct(ProductRequest request) {
        return Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .brand(request.getBrand())
                .price(request.getPrice())
                .barcode(request.getBarcode())
                .imageUrl(request.getImageUrl())
                .categoryId(request.getCategoryId())
                .isApproved(true)
                .isDeleted(false)
                .build();
    }

    private void saveTags(Integer productId, List<String> tagNames) {
        tagNames.stream()
                .map(String::toLowerCase)
                .map(String::trim)
                .filter(t -> !t.isBlank())
                .distinct()
                .forEach(tagName -> {
                    Tag tag = tagRepository.findByName(tagName)
                            .orElseGet(() -> tagRepository.save(
                                    Tag.builder().name(tagName).build()));

                    ProductTags productTag = ProductTags.builder()
                            .productId(productId)
                            .tagId(tag.getId())
                            .build();

                    productTagRepository.save(productTag);
                });
    }

    private ProductRequest mapOpenFoodFactsToProductRequest(
            OpenFoodFactsResponse response,
            BigDecimal price,
            Integer categoryId) {

        var p = response.getProduct();

        List<String> tags = new ArrayList<>();

        if (p.getCategoryTags() != null) {
            p.getCategoryTags().stream()
                    .map(t -> t.replace("en:", "")
                            .replace("-", " ").trim())
                    .filter(t -> !t.isBlank())
                    .forEach(tags::add);
        }

        if (p.getLabelsTags() != null) {
            p.getLabelsTags().stream()
                    .map(t -> t.replace("en:", "")
                            .replace("-", " ").trim())
                    .filter(t -> !t.isBlank())
                    .forEach(tags::add);
        }

        return ProductRequest.builder()
                .name(p.getProductName())
                .brand(p.getBrands())
                .imageUrl(p.getImageUrl())
                .price(price)
                .categoryId(categoryId)
                .tags(tags)
                .build();
    }

    public ProductResponse mapToResponse(Product product) {

        List<String> tags = productTagRepository
                .findByProductId(product.getId())
                .stream()
                .map(pt -> tagRepository.findById(pt.getTagId())
                        .map(Tag::getName)
                        .orElse(""))
                .filter(t -> !t.isBlank())
                .collect(Collectors.toList());

        String categoryName = product.getCategoryId() != null
                ? categoryRepository.findById(product.getCategoryId())
                .map(Category::getName)
                .orElse(null)
                : null;

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .brand(product.getBrand())
                .price(product.getPrice())
                .barcode(product.getBarcode())
                .imageUrl(product.getImageUrl())
                .isApproved(product.getIsApproved())
                .categoryId(product.getCategoryId())
                .categoryName(categoryName)
                .tags(tags)
                .createdAt(product.getCreatedAt())
                .build();
    }
}