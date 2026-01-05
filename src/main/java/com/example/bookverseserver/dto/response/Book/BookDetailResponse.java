package com.example.bookverseserver.dto.response.Book;

import com.example.bookverseserver.dto.response.User.SellerProfileResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
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
    private Date publicationDate; // Match frontend naming
    private Integer pageCount; // Match frontend naming
    private String language;
    private String description;
    private String coverImageUrl; // Match frontend naming
    private List<AuthorResponse> authors;
    private List<CategoryResponse> categories;
    private BigDecimal price;
    private BigDecimal finalPrice;
    private Map<String, Object> discount; // {type: 'PERCENT' | 'FIXED', value: number}
    private String currency;
    private SellerProfileResponse seller; // Match frontend naming
    private Double averageRating; // Average rating from reviews
    private Integer totalReviews; // Total number of reviews
}