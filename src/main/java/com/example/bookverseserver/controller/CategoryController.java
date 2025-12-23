package com.example.bookverseserver.controller;

import java.util.List;

import com.example.bookverseserver.dto.request.Book.CategoryRequest;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.Book.CategoryResponse;
import com.example.bookverseserver.service.CategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(
        name = "Category",
        description = "APIs for managing book categories"
)
public class CategoryController {

    CategoryService categoryService;

    // ================= CREATE =================

    @PostMapping
    @Operation(
            summary = "Create category",
            description = "Create a new book category with name, description and tags"
    )
    @SecurityRequirement(name = "bearer-key") // nếu chỉ admin dùng
    public ApiResponse<CategoryResponse> createCategory(
            @RequestBody CategoryRequest categoryRequest
    ) {
        return ApiResponse.<CategoryResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Category created successfully")
                .result(categoryService.createCategory(categoryRequest))
                .build();
    }

    // ================= UPDATE =================

    @PutMapping("/{categoryId}")
    @Operation(
            summary = "Update category",
            description = "Update category information by category ID"
    )
    @SecurityRequirement(name = "bearer-key")
    public ApiResponse<CategoryResponse> updateCategory(
            @PathVariable Long categoryId,
            @RequestBody CategoryRequest categoryRequest
    ) {
        return ApiResponse.<CategoryResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Category updated successfully")
                .result(categoryService.updateCategory(categoryId, categoryRequest))
                .build();
    }

    // ================= DELETE =================

    @DeleteMapping("/{categoryId}")
    @Operation(
            summary = "Delete category",
            description = "Delete a category by ID"
    )
    @SecurityRequirement(name = "bearer-key")
    public ApiResponse<CategoryResponse> deleteCategory(
            @PathVariable Long categoryId
    ) {
        return ApiResponse.<CategoryResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Category deleted successfully")
                .result(categoryService.deleteCategory(categoryId))
                .build();
    }

    // ================= GET ALL =================

    @GetMapping
    @Operation(
            summary = "Get all categories",
            description = "Retrieve all available book categories"
    )
    public ApiResponse<List<CategoryResponse>> getAllCategories() {
        return ApiResponse.<List<CategoryResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Categories retrieved successfully")
                .result(categoryService.getAllCategories())
                .build();
    }
}
