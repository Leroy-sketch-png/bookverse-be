package com.example.bookverseserver.service;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;

import com.example.bookverseserver.dto.request.Book.CategoryRequest;
import com.example.bookverseserver.dto.response.Book.CategoryResponse;
import com.example.bookverseserver.entity.Product.Category;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.mapper.CategoryMapper;
import com.example.bookverseserver.repository.CategoryRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.example.bookverseserver.dto.request.Authentication.RoleRequest;
import com.example.bookverseserver.dto.response.Authentication.RoleResponse;
import com.example.bookverseserver.mapper.RoleMapper;
import com.example.bookverseserver.repository.PermissionRepository;
import com.example.bookverseserver.repository.RoleRepository;

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


//    public CategoryResponse updateCategory(Long categoryId, CategoryRequest request) {
//        Category category = categoryRepository.findById(categoryId)
//                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
//        categoryMapper.updateCategory(category, request);
//    }
}
