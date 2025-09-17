package com.example.bookverseserver.dto.response.Book;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class BookDetailResponse {
    private Long id;
    private String title;
    private String isbn;
    private String publisher;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date published_date;
    private Integer pages;
    private String description;
    private List<AuthorResponse> authors;
    private List<CategoryResponse> categories;
    private List<Map<String, String>> images;
    private Map<String, Object> cheapest_listing_preview;
}