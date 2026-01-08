package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.request.Book.BookMetaCreationRequest;
import com.example.bookverseserver.dto.response.Book.BookResponse;
import com.example.bookverseserver.entity.Product.BookMeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BookMetaMapperTest {

    private BookMetaMapper bookMetaMapper;

    @BeforeEach
    void setUp() {
        bookMetaMapper = new BookMetaMapperImpl();
    }

    @Test
    void testToBookMeta() {
        BookMetaCreationRequest request = new BookMetaCreationRequest();
        request.setTitle("Test Book");

        BookMeta bookMeta = bookMetaMapper.toBookMeta(request);

        assertThat(bookMeta).isNotNull();
        assertThat(bookMeta.getTitle()).isEqualTo("Test Book");
    }

    @Test
    void testUpdateBookMeta() {
        BookMetaCreationRequest request = new BookMetaCreationRequest();
        request.setTitle("Updated Book");

        BookMeta bookMeta = new BookMeta();
        bookMeta.setTitle("Old Book");

        bookMetaMapper.updateBookMeta(bookMeta, request);

        assertThat(bookMeta.getTitle()).isEqualTo("Updated Book");
    }

    @Test
    void testToBookResponse() {
        BookMeta bookMeta = new BookMeta();
        bookMeta.setId(1L);
        bookMeta.setTitle("Test Book");

        BookResponse response = bookMetaMapper.toBookResponse(bookMeta);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Test Book");
    }

    @Test
    void testToBookResponseList() {
        BookMeta bookMeta1 = new BookMeta();
        bookMeta1.setId(1L);
        bookMeta1.setTitle("Test Book 1");

        BookMeta bookMeta2 = new BookMeta();
        bookMeta2.setId(2L);
        bookMeta2.setTitle("Test Book 2");

        List<BookMeta> bookMetas = List.of(bookMeta1, bookMeta2);

        List<BookResponse> responses = bookMetaMapper.toBookResponseList(bookMetas);

        assertThat(responses).isNotNull().hasSize(2);
        assertThat(responses.get(0).getTitle()).isEqualTo("Test Book 1");
        assertThat(responses.get(1).getTitle()).isEqualTo("Test Book 2");
    }
}
