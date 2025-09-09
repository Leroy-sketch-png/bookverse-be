package com.example.bookverseserver.controller;

import java.util.List;

import com.example.bookverseserver.dto.request.Book.CategoryRequest;
import com.example.bookverseserver.dto.response.Book.CategoryResponse;
import com.example.bookverseserver.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.request.Authentication.PermissionRequest;
import com.example.bookverseserver.dto.response.Authentication.PermissionResponse;
import com.example.bookverseserver.service.PermissionService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CategoryController {
    CategoryService categoryService;

    @PostMapping
    ApiResponse<CategoryResponse> createCategory(@RequestBody CategoryRequest categoryRequest) {
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.createCategory(categoryRequest))
                .build();
    }

    @PutMapping("/{categoryId}")
    ApiResponse<CategoryResponse> updateCategory(@PathVariable("categoryId") Long id, @RequestBody CategoryRequest categoryRequest) {
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.updateCategory(id, categoryRequest))
                .build();
    }

    @DeleteMapping("/{categoryId}")
    ApiResponse<CategoryResponse> deleteCategory(@PathVariable("categoryId") Long id) {
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.deleteCategory(id))
                .build();
    }

    @GetMapping
    ApiResponse<List<CategoryResponse>> getAllCategories() {
            return ApiResponse.<List<CategoryResponse>>builder()
                    .result(categoryService.getAllCategories())
                    .build();
    }
}