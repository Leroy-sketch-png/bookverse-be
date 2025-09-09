package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.request.Book.CategoryRequest;
import com.example.bookverseserver.dto.response.Book.CategoryResponse;
import com.example.bookverseserver.entity.Product.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "books", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Category toCategory(CategoryRequest request);

    CategoryResponse toCategoryResponse(Category category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "books", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateCategory(@MappingTarget Category category, CategoryRequest request);
}
