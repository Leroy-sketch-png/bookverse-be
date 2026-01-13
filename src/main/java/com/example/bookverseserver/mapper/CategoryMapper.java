package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.request.Book.CategoryRequest;
import com.example.bookverseserver.dto.response.Book.CategoryResponse;
import com.example.bookverseserver.entity.Product.Category;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bookMetas", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    Category toCategory(CategoryRequest request);

    @Named("toFlat")
    @Mapping(source = "parent.id", target = "parentId")
    @Mapping(target = "children", ignore = true)
    CategoryResponse toCategoryResponse(Category category);
    
    /**
     * Map with children for tree structure
     */
    @Named("toTree")
    @Mapping(source = "parent.id", target = "parentId")
    @Mapping(source = "children", target = "children", qualifiedByName = "toFlat")
    CategoryResponse toCategoryResponseWithChildren(Category category);
    
    @IterableMapping(qualifiedByName = "toFlat")
    List<CategoryResponse> toCategoryResponseList(List<Category> categories);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bookMetas", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCategory(@MappingTarget Category category, CategoryRequest request);
}