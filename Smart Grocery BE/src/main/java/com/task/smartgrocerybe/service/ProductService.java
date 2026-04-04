package com.task.smartgrocerybe.service;

import com.task.smartgrocerybe.dto.ProductRequest;
import com.task.smartgrocerybe.dto.ProductResponse;
import com.task.smartgrocerybe.dto.external.OpenFoodFactsResponse;
import com.task.smartgrocerybe.exception.BadRequestException;
import com.task.smartgrocerybe.exception.ResourceAlreadyExistsException;
import com.task.smartgrocerybe.exception.ResourceNotFoundException;
import com.task.smartgrocerybe.model.Category;
import com.task.smartgrocerybe.model.Product;
import com.task.smartgrocerybe.model.ProductTags;
import com.task.smartgrocerybe.model.Tag;
import com.task.smartgrocerybe.model.enums.Action;
import com.task.smartgrocerybe.model.enums.EntityType;
import com.task.smartgrocerybe.repository.CategoryRepository;
import com.task.smartgrocerybe.repository.ProductRepository;
import com.task.smartgrocerybe.repository.ProductTagRepository;
import com.task.smartgrocerybe.repository.TagRepository;
import com.task.smartgrocerybe.service.AuditLogService;
import com.task.smartgrocerybe.service.OpenFoodFactsService;
import com.task.smartgrocerybe.util.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
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

    // ─── Admin only ───────────────────────────────────────────────────────

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
                Action.CREATE, EntityType.PRODUCT,
                "Added product: " + saved.getName());

        return mapToResponse(saved);
    }

    @Transactional
    public List<ProductResponse> bulkAddProducts(List<ProductRequest> requests) {
        return requests.stream()
                .map(this::addProduct)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductResponse updateProduct(Integer id, ProductRequest request) {
        // findById uses @SQLRestriction — only finds non-deleted
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id));

        updateField("name", product.getName(),
                request.getName(), product::setName);
        updateField("description", product.getDescription(),
                request.getDescription(), product::setDescription);
        updateField("brand", product.getBrand(),
                request.getBrand(), product::setBrand);
        updateField("imageUrl", product.getImageUrl(),
                request.getImageUrl(), product::setImageUrl);

        if (request.getPrice() != null &&
                !request.getPrice().equals(product.getPrice())) {
            logChange("price", product.getPrice(), request.getPrice());
            product.setPrice(request.getPrice());
        }

        if (isValid(request.getBarcode()) &&
                !request.getBarcode().equals(product.getBarcode())) {
            if (productRepository.existsByBarcode(request.getBarcode())) {
                throw new ResourceAlreadyExistsException("Barcode already exists");
            }
            logChange("barcode", product.getBarcode(), request.getBarcode());
            product.setBarcode(request.getBarcode());
        }

        if (request.getCategoryId() != null &&
                !request.getCategoryId().equals(product.getCategoryId())) {
            if (!categoryRepository.existsById(request.getCategoryId())) {
                throw new ResourceNotFoundException(
                        "Category not found with id: " + request.getCategoryId());
            }
            logChange("category", product.getCategoryId(), request.getCategoryId());
            product.setCategoryId(request.getCategoryId());
        }

        return mapToResponse(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Integer id) {
        // findById uses @SQLRestriction — throws if already deleted
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id));

        product.setIsDeleted(true);
        product.setDeletedAt(LocalDateTime.now());
        productRepository.save(product);

        auditLogService.log(
                Action.DELETE, EntityType.PRODUCT,
                "Soft deleted product: " + product.getName());
    }

    @Transactional
    public ProductResponse restoreProduct(Integer id) {
        Product product = productRepository.findByIdIgnoreRestriction(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id));

        if (!Boolean.TRUE.equals(product.getIsDeleted())) {
            throw new BadRequestException("Product is not deleted");
        }

        product.setIsDeleted(false);
        product.setDeletedAt(null);
        productRepository.save(product);

        auditLogService.log(
                Action.UPDATE, EntityType.PRODUCT,
                "Restored product: " + product.getName());

        return mapToResponse(product);
    }

    // admin sees everything with filters — bypasses @SQLRestriction
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getProducts(
            int page, int size, String sortBy, String sortDir,
            String search, Integer categoryId, String barcode,
            Boolean isApproved, Boolean isDeleted) {

        Pageable pageable = buildPageable(page, size, sortBy, sortDir);

        Page<Product> products = productRepository.findWithFilters(
                isApproved,
                isDeleted,
                isValid(barcode) ? barcode : null,
                isValid(search) ? search : null,
                categoryId,
                pageable
        );

        return buildPageResponse(products);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getAllProducts(
            int page, int size, String sortBy, String sortDir) {

        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Page<Product> products = productRepository.findAllIgnoreRestriction(pageable);
        return buildPageResponse(products);
    }

    // ─── User + Admin ─────────────────────────────────────────────────────

    // user sees ONLY approved + not deleted — @SQLRestriction handles deleted
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getApprovedProducts(
            int page, int size, String sortBy, String sortDir,
            String search, Integer categoryId, String barcode) {

        Pageable pageable = buildPageable(page, size, sortBy, sortDir);

        Page<Product> products;

        if (isValid(barcode)) {
            products = productRepository
                    .findByIsApprovedTrueAndBarcodeContaining(barcode, pageable);
        } else if (isValid(search)) {
            products = productRepository
                    .findByIsApprovedTrueAndNameContainingIgnoreCase(search, pageable);
        } else if (categoryId != null) {
            products = productRepository
                    .findByIsApprovedTrueAndCategoryId(categoryId, pageable);
        } else {
            products = productRepository.findByIsApprovedTrue(pageable);
        }

        return buildPageResponse(products);
    }

    // user gets single product — @SQLRestriction blocks deleted automatically
    public ProductResponse getProductById(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id));
        return mapToResponse(product);
    }

    // ─── Private helpers ──────────────────────────────────────────────────

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

                    productTagRepository.save(
                            ProductTags.builder()
                                    .productId(productId)
                                    .tagId(tag.getId())
                                    .build());
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
                    .map(t -> t.replace("en:", "").replace("-", " ").trim())
                    .filter(t -> !t.isBlank())
                    .forEach(tags::add);
        }

        if (p.getLabelsTags() != null) {
            p.getLabelsTags().stream()
                    .map(t -> t.replace("en:", "").replace("-", " ").trim())
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

    private ProductResponse mapToResponse(Product product) {
        List<String> tags = productTagRepository
                .findByProductId(product.getId())
                .stream()
                .map(pt -> tagRepository.findById(pt.getTagId())
                        .map(Tag::getName).orElse(""))
                .filter(t -> !t.isBlank())
                .collect(Collectors.toList());

        String categoryName = product.getCategoryId() != null
                ? categoryRepository.findById(product.getCategoryId())
                .map(Category::getName).orElse(null)
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

    private Pageable buildPageable(
            int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        return PageRequest.of(page, size, sort);
    }

    private PageResponse<ProductResponse> buildPageResponse(
            Page<Product> products) {
        List<ProductResponse> content = products.getContent()
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

    private <T> void updateField(String fieldName, T oldValue,
                                 T newValue, Consumer<T> setter) {
        if (newValue != null &&
                (!(newValue instanceof String s) || !s.isBlank()) &&
                !newValue.equals(oldValue)) {
            logChange(fieldName, oldValue, newValue);
            setter.accept(newValue);
        }
    }

    private void logChange(String field, Object oldValue, Object newValue) {
        auditLogService.log(
                Action.UPDATE, EntityType.PRODUCT,
                "Updated product " + field,
                String.valueOf(oldValue),
                String.valueOf(newValue));
    }

    private boolean isValid(String value) {
        return value != null && !value.isBlank();
    }
}