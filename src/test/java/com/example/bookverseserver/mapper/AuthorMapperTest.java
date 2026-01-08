package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.request.Book.AuthorDetailRequest;
import com.example.bookverseserver.dto.request.Book.AuthorRequest;
import com.example.bookverseserver.dto.response.Book.AuthorDetailResponse;
import com.example.bookverseserver.dto.response.Book.AuthorResponse;
import com.example.bookverseserver.entity.Product.Author;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthorMapperTest {

    private AuthorMapper authorMapper;

    @BeforeEach
    void setUp() {
        authorMapper = new AuthorMapperImpl();
    }

    @Test
    void testToAuthor() {
        AuthorRequest request = new AuthorRequest();
        request.setName("Test Author");

        Author author = authorMapper.toAuthor(request);

        assertThat(author).isNotNull();
        assertThat(author.getName()).isEqualTo("Test Author");
        assertThat(author.getBookMetas()).isNull();
    }

    @Test
    void testToAuthorResponse() {
        Author author = new Author();
        author.setId(1L);
        author.setName("Test Author");

        AuthorResponse response = authorMapper.toAuthorResponse(author);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Test Author");
    }

    @Test
    void testToAuthorDetailResponse() {
        Author author = new Author();
        author.setId(1L);
        author.setName("Test Author");
        author.setPersonalName("Test Personal Name");

        AuthorDetailResponse response = authorMapper.toAuthorDetailResponse(author);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Test Author");
        assertThat(response.getPersonalName()).isEqualTo("Test Personal Name");
    }

    @Test
    void testUpdateAuthor() {
        AuthorDetailRequest request = new AuthorDetailRequest();
        request.setName("Updated Author");

        Author author = new Author();
        author.setName("Old Author");

        authorMapper.updateAuthor(author, request);

        assertThat(author.getName()).isEqualTo("Updated Author");
        assertThat(author.getBookMetas()).isNull();
    }
}
