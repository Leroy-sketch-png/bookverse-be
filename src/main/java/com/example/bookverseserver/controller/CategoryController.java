package com.example.bookverseserver.controller;

import java.util.List;

import com.example.bookverseserver.dto.request.Book.CategoryRequest;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.Book.CategoryResponse;
import com.example.bookverseserver.service.CategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(
        name = "Categories",
        description = "📚 Book category management APIs - Browse categories, manage category hierarchy"
)
public class CategoryController {

    CategoryService categoryService;

    // ================= CREATE =================

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create category (Admin only)",
            description = "Create a new book category with name, description, slug, and tags. " +
                         "**Supports hierarchical categories** (parent-child relationships). " +
                         "**Requires ADMIN role**. " +
                         "**Examples**: Fiction, Technology > Programming, Science > Physics"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", 
            description = "Category created successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid category data or slug already exists"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Unauthorized"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Forbidden - Admin access required"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409", 
            description = "Category with this name/slug already exists"
        )
    })
    public ApiResponse<CategoryResponse> createCategory(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Category details (name, description, parent category ID, tags)",
                required = true,
                content = @Content(schema = @Schema(implementation = CategoryRequest.class))
            )
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
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update category (Admin only)",
            description = "Update existing category information. **Requires ADMIN role**. " +
                         "Can update name, description, parent category, tags, and slug."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Category updated successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid update data"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Unauthorized"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Forbidden - Admin access required"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Category not found"
        )
    })
    public ApiResponse<CategoryResponse> updateCategory(
            @Parameter(description = "Category ID to update", example = "1", required = true)
            @PathVariable Long categoryId,
            
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Updated category details",
                required = true,
                content = @Content(schema = @Schema(implementation = CategoryRequest.class))
            )
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
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete category (Admin only)",
            description = "Delete a category by ID. **Requires ADMIN role**. " +
                         "**Warning**: May affect books in this category. " +
                         "Consider moving books to another category first."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Category deleted successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Unauthorized"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", 
            description = "Forbidden - Admin access required"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Category not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409", 
            description = "Cannot delete - category has books or subcategories"
        )
    })
    public ApiResponse<CategoryResponse> deleteCategory(
            @Parameter(description = "Category ID to delete", example = "1", required = true)
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
            description = "Retrieve flat list of all book categories. " +
                         "**Includes**: " +
                         "- Main categories and subcategories " +
                         "- Book count per category " +
                         "- Category slugs for SEO-friendly URLs " +
                         "**Use for**: Navigation menus, category filters, breadcrumbs"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Categories retrieved successfully"
        )
    })
    public ApiResponse<List<CategoryResponse>> getAllCategories() {
        return ApiResponse.<List<CategoryResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Categories retrieved successfully")
                .result(categoryService.getAllCategories())
                .build();
    }

    // ================= GET TREE =================

    @GetMapping("/tree")
    @Operation(
            summary = "Get category tree",
            description = "Retrieve hierarchical category tree with nested children. " +
                         "**Structure**: Root categories contain nested subcategories. " +
                         "**Use for**: Mega menus, sidebar navigation, category browsers"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Category tree retrieved successfully"
        )
    })
    public ApiResponse<List<CategoryResponse>> getCategoryTree() {
        return ApiResponse.<List<CategoryResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Category tree retrieved successfully")
                .result(categoryService.getCategoryTree())
                .build();
    }

    // ================= GET FEATURED =================

    @GetMapping("/featured")
    @Operation(
            summary = "Get featured categories",
            description = "Retrieve featured categories for homepage display. " +
                         "**Includes**: Emoji icons, colors, and book counts. " +
                         "**Use for**: Homepage category showcase, promotional sections"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Featured categories retrieved successfully"
        )
    })
    public ApiResponse<List<CategoryResponse>> getFeaturedCategories() {
        return ApiResponse.<List<CategoryResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Featured categories retrieved successfully")
                .result(categoryService.getFeaturedCategories())
                .build();
    }

    // ================= GET BY ID =================

    @GetMapping("/{categoryId}")
    @Operation(
            summary = "Get category by ID",
            description = "Retrieve a single category by its ID. " +
                         "**Includes**: Full category details with parent reference."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Category retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Category not found"
        )
    })
    public ApiResponse<CategoryResponse> getCategoryById(
            @Parameter(description = "Category ID", example = "1", required = true)
            @PathVariable Long categoryId
    ) {
        return ApiResponse.<CategoryResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Category retrieved successfully")
                .result(categoryService.getCategoryById(categoryId))
                .build();
    }

    // ================= GET SUBCATEGORIES =================

    @GetMapping("/{parentId}/subcategories")
    @Operation(
            summary = "Get subcategories",
            description = "Retrieve all subcategories of a parent category. " +
                         "**Use for**: Drill-down navigation, category expansion"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Subcategories retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Parent category not found"
        )
    })
    public ApiResponse<List<CategoryResponse>> getSubcategories(
            @Parameter(description = "Parent category ID", example = "1", required = true)
            @PathVariable Long parentId
    ) {
        return ApiResponse.<List<CategoryResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Subcategories retrieved successfully")
                .result(categoryService.getSubcategories(parentId))
                .build();
    }
}
