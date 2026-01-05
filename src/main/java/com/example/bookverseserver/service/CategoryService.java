package com.example.bookverseserver.service;

import java.text.Normalizer;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import com.example.bookverseserver.dto.request.Book.CategoryRequest;
import com.example.bookverseserver.dto.response.Book.CategoryResponse;
import com.example.bookverseserver.entity.Product.Category;
import com.example.bookverseserver.enums.ApprovedCategory; // Ensure you created this Enum file
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.mapper.CategoryMapper;
import com.example.bookverseserver.repository.CategoryRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryService {
    CategoryRepository categoryRepository;
    CategoryMapper categoryMapper;

    // --- STANDARD CRUD METHODS ---

    public CategoryResponse createCategory(CategoryRequest categoryRequest) {
        Category category = categoryMapper.toCategory(categoryRequest);

        // Auto-generate slug if missing
        if (category.getSlug() == null || category.getSlug().isEmpty()) {
            category.setSlug(toSlug(category.getName()));
        }

        try {
            category = categoryRepository.save(category);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.CATEGORY_EXISTED);
        }
        return categoryMapper.toCategoryResponse(category);
    }

    public CategoryResponse updateCategory(Long categoryId, CategoryRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        categoryMapper.updateCategory(category, request);

        // Update slug if name changed
        if (request.getName() != null) {
            category.setSlug(toSlug(request.getName()));
        }

        Category updated = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(updated);
    }

    public CategoryResponse deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        categoryRepository.delete(category);
        return categoryMapper.toCategoryResponse(category);
    }

    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream().map(categoryMapper::toCategoryResponse).toList();
    }

    // --- OPEN LIBRARY & BUSINESS LOGIC ---

    /**
     * 1. ENTRY POINT: Call this from BookService.
     * Takes a messy raw subject, filters it against your 6 Approved Categories,
     * and returns the database entity (or null if rejected).
     */
    public Category filterAndGetCategory(String rawSubject) {
        if (rawSubject == null || rawSubject.isEmpty()) return null;

        // Step 1: Map the raw string to one of your 6 buckets
        ApprovedCategory matchedEnum = mapSubjectToBucket(rawSubject);

        // Step 2: If it's not in the allowed list, reject it
        if (matchedEnum == null) {
            return null;
        }

        // Step 3: Get or Create the official category from DB
        return getOrCreateCategory(matchedEnum.getDisplayName());
    }

    /**
     * 2. DB INTERACTION: Finds or Creates a category.
     * FIX: Now generates a Slug to prevent Database Constraint Violation.
     */
    public Category getOrCreateCategory(String categoryName) {
        Optional<Category> existingCategory = categoryRepository.findByName(categoryName);
        if (existingCategory.isPresent()) {
            return existingCategory.get();
        }

        // Generate Slug automatically
        String slug = toSlug(categoryName);

        Category newCategory = Category.builder()
                .name(categoryName)
                .slug(slug) // <--- THIS WAS MISSING
                .build();

        // Handle race conditions (if two threads try to create 'Fiction' at once)
        try {
            return categoryRepository.save(newCategory);
        } catch (DataIntegrityViolationException e) {
            return categoryRepository.findByName(categoryName).orElseThrow();
        }
    }

    // --- HELPERS ---

    /**
     * 3. MAPPING LOGIC: The "Brain" that sorts books into your 6 buckets.
     */
    private ApprovedCategory mapSubjectToBucket(String rawSubject) {
        String input = rawSubject.toLowerCase().trim();

        // TECHNOLOGY
        if (input.contains("technology") || input.contains("computer") ||
                input.contains("programming") || input.contains("software") ||
                input.contains("engineering") || input.contains("web") ||
                input.contains("java") || input.contains("python") || input.contains("code")) {
            return ApprovedCategory.TECHNOLOGY;
        }

        // SCIENCE
        if (input.contains("science") || input.contains("physics") ||
                input.contains("chemistry") || input.contains("biology") ||
                input.contains("astronomy") || input.contains("mathematics") ||
                input.contains("nature") || input.contains("space")) {
            return ApprovedCategory.SCIENCE;
        }

        // BUSINESS
        if (input.contains("business") || input.contains("economics") ||
                input.contains("finance") || input.contains("management") ||
                input.contains("marketing") || input.contains("entrepreneurship") ||
                input.contains("investing") || input.contains("startup")) {
            return ApprovedCategory.BUSINESS;
        }

        // SELF-HELP
        if (input.contains("self-help") || input.contains("self help") ||
                input.contains("personal development") || input.contains("motivation") ||
                input.contains("psychology") || input.contains("success") ||
                input.contains("happiness") || input.contains("habits")) {
            return ApprovedCategory.SELF_HELP;
        }

        // FICTION vs NON-FICTION (Order matters)
        if (input.contains("non-fiction") || input.contains("non fiction") ||
                input.contains("biography") || input.contains("memoir") ||
                input.contains("history") || input.contains("essay") || input.contains("true story")) {
            return ApprovedCategory.NON_FICTION;
        }

        if (input.contains("fiction") || input.contains("fantasy") ||
                input.contains("mystery") || input.contains("romance") ||
                input.contains("horror") || input.contains("thriller") ||
                input.contains("novel") || input.contains("adventure")) {
            return ApprovedCategory.FICTION;
        }

        return null; // Not matched
    }

    /**
     * 4. SLUG GENERATOR: Converts "Science Fiction" -> "science-fiction"
     */
    private String toSlug(String input) {
        if (input == null) return null;
        String nowhitespace = input.trim().toLowerCase().replaceAll("\\s+", "-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("[^\\w-]");
        return pattern.matcher(normalized).replaceAll("");
    }
}