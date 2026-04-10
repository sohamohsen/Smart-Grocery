package com.task.smartgrocerybe.service;

import com.task.smartgrocerybe.dto.CategoryRequest;
import com.task.smartgrocerybe.dto.CategoryResponse;
import com.task.smartgrocerybe.exception.BadRequestException;
import com.task.smartgrocerybe.exception.ResourceAlreadyExistsException;
import com.task.smartgrocerybe.exception.ResourceNotFoundException;
import com.task.smartgrocerybe.model.Category;
import com.task.smartgrocerybe.model.enums.Action;
import com.task.smartgrocerybe.model.enums.EntityType;
import com.task.smartgrocerybe.repository.CategoryRepository;
import com.task.smartgrocerybe.util.ExcelUtil;
import com.task.smartgrocerybe.util.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final AuditLogService auditLogService;

    public CategoryResponse addCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new ResourceAlreadyExistsException("Category already exists");
        }

        Category category = categoryRepository.save(mapToCategory(request));

        auditLogService.log(
                Action.CREATE, EntityType.CATEGORY,
                "Added category: " + category.getName());

        return mapToCategoryResponse(category);
    }

    @Transactional
    public CategoryResponse updateCategory(Integer id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + id));

        if (Boolean.TRUE.equals(category.getIsDeleted())) {
            throw new ResourceNotFoundException(
                    "Category not found with id: " + id);
        }

        if (isValid(request.getName()) &&
                !request.getName().equalsIgnoreCase(category.getName())) {
            if (categoryRepository.existsByName(request.getName())) {
                throw new ResourceAlreadyExistsException("Category already exists");
            }
            logChange("name", category.getName(), request.getName());
            category.setName(request.getName());
        }

        if (isValid(request.getDescription()) &&
                !request.getDescription().equals(category.getDescription())) {
            logChange("description", category.getDescription(),
                    request.getDescription());
            category.setDescription(request.getDescription());
        }

        return mapToCategoryResponse(categoryRepository.save(category));
    }

    public CategoryResponse getCategory(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + id));

        if (Boolean.TRUE.equals(category.getIsDeleted())) {
            throw new ResourceNotFoundException(
                    "Category not found with id: " + id);
        }

        return mapToCategoryResponse(category);
    }

    public PageResponse<CategoryResponse> getCategories(
            int page, int size, String sortBy, String sortDir, String search) {

        Pageable pageable = buildPageable(page, size, sortBy, sortDir);

        Page<Category> categories = isValid(search)
                ? categoryRepository
                .findByNameContainingIgnoreCaseAndIsDeletedFalse(
                        search, pageable)
                : categoryRepository.findAllAndIsDeletedFalse(pageable);

        return buildPageResponse(categories);
    }

    public PageResponse<CategoryResponse> getDeletedCategories(
            int page, int size, String sortBy, String sortDir) {

        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Page<Category> categories = categoryRepository.findAllDeleted(pageable);
        return buildPageResponse(categories);
    }

    @Transactional
    public CategoryResponse restoreCategory(Integer id) {
        Category category = categoryRepository.findByIdIgnoreRestriction(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + id));

        if (!Boolean.TRUE.equals(category.getIsDeleted())) {
            throw new BadRequestException("Category is not deleted");
        }

        category.setIsDeleted(false);
        category.setDeletedAt(null);
        category = categoryRepository.save(category);

        auditLogService.log(
                Action.UPDATE, EntityType.CATEGORY,
                "Restored category: " + category.getName());

        return mapToCategoryResponse(category);
    }

    @Transactional
    public void deleteCategory(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + id));

        if (Boolean.TRUE.equals(category.getIsDeleted())) {
            throw new BadRequestException("Category is already deleted");
        }

        category.setIsDeleted(true);
        category.setDeletedAt(LocalDateTime.now());
        categoryRepository.save(category);

        auditLogService.log(
                Action.DELETE, EntityType.CATEGORY,
                "Soft deleted category: " + category.getName());
    }

    @Transactional
    public void uploadCategoriesFromExcel(MultipartFile file) {

        var rows = ExcelUtil.readExcel(file);

        for (List<String> row : rows) {

            String name = row.size() > 0 ? row.get(0) : null;
            String description = row.size() > 1 ? row.get(1) : null;

            if (!isValid(name)) continue;

            if (categoryRepository.existsByName(name)) {
                continue;
            }

            Category category = Category.builder()
                    .name(name)
                    .description(description)
                    .isDeleted(false)
                    .build();

            categoryRepository.save(category);

            auditLogService.log(
                    Action.CREATE,
                    EntityType.CATEGORY,
                    "Added from Excel: " + name
            );
        }
    }

    // ─── Private helpers ──────────────────────────────────────────────────

    private Category mapToCategory(CategoryRequest request) {
        return Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isDeleted(false)
                .build();
    }

    private CategoryResponse mapToCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .build();
    }

    private Pageable buildPageable(
            int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        return PageRequest.of(page, size, sort);
    }

    private PageResponse<CategoryResponse> buildPageResponse(
            Page<Category> categories) {
        return PageResponse.<CategoryResponse>builder()
                .content(categories.getContent()
                        .stream()
                        .map(this::mapToCategoryResponse)
                        .toList())
                .pageNumber(categories.getNumber())
                .pageSize(categories.getSize())
                .totalElements(categories.getTotalElements())
                .totalPages(categories.getTotalPages())
                .last(categories.isLast())
                .build();
    }

    private void logChange(String field, Object oldValue, Object newValue) {
        auditLogService.log(
                Action.UPDATE, EntityType.CATEGORY,
                "Updated category " + field,
                String.valueOf(oldValue),
                String.valueOf(newValue));
    }

    private boolean isValid(String value) {
        return value != null && !value.isBlank();
    }

    public CategoryResponse getDeletedCategoryById(Integer id) {
        return null;
    }
}