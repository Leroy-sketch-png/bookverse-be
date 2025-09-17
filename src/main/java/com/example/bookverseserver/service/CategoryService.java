package com.example.bookverseserver.service;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import com.example.bookverseserver.dto.request.Book.CategoryRequest;
import com.example.bookverseserver.dto.response.Book.CategoryResponse;
import com.example.bookverseserver.entity.Product.Category;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.mapper.CategoryMapper;
import com.example.bookverseserver.repository.CategoryRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse createCategory(CategoryRequest categoryRequest) {
        Category category = categoryMapper.toCategory(categoryRequest);
        try {
            category = categoryRepository.save(category);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.CATEGORY_EXISTED);
        }
        return categoryMapper.toCategoryResponse(category);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse updateCategory(Long categoryId, CategoryRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        categoryMapper.updateCategory(category, request);

        Category updated = categoryRepository.save(category);
        return categoryMapper.toCategoryResponse(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        categoryRepository.delete(category);

        return categoryMapper.toCategoryResponse(category);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream().map(categoryMapper::toCategoryResponse).toList();
    }

    public Category getOrCreateCategory(String categoryName) {
        Optional<Category> existingCategory = categoryRepository.findByName(categoryName);
        if (existingCategory.isPresent()) {
            return existingCategory.get();
        }

        Category newCategory = Category.builder()
                .name(categoryName)
                .build();
        return categoryRepository.save(newCategory);
    }
}
