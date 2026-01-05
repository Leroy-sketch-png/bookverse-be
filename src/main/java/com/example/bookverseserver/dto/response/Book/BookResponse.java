package com.example.bookverseserver.dto.response.Book;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookResponse {
    private String id;
    private String title;
    private String isbn;
    private List<AuthorResponse> authors;
    private List<CategoryResponse> categories;
    private String cover_url;
    private Map<String, Object> cheapest_listing_preview;
}