package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.request.Book.CategoryRequest;
import com.example.bookverseserver.dto.response.Book.CategoryResponse;
import com.example.bookverseserver.entity.Product.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CategoryMapperTest {

    @Autowired
    private CategoryMapper categoryMapper;

    @Test
    void testToCategory() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Test Category");

        Category category = categoryMapper.toCategory(request);

        assertThat(category).isNotNull();
        assertThat(category.getName()).isEqualTo("Test Category");
        assertThat(category.getId()).isNull();
        assertThat(category.getBookMetas()).isNull();
        assertThat(category.getCreatedAt()).isNull();
        assertThat(category.getUpdatedAt()).isNull();
    }

    @Test
    void testToCategoryResponse() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Test Category");

        CategoryResponse response = categoryMapper.toCategoryResponse(category);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Test Category");
    }

    @Test
    void testUpdateCategory() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Updated Category");

        Category category = new Category();
        category.setName("Old Category");

        categoryMapper.updateCategory(category, request);

        assertThat(category.getName()).isEqualTo("Updated Category");
        assertThat(category.getId()).isNull();
        assertThat(category.getBookMetas()).isNull();
        assertThat(category.getCreatedAt()).isNull();
        assertThat(category.getUpdatedAt()).isNull();
    }
}
