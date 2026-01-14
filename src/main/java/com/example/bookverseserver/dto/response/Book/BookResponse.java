package com.example.bookverseserver.dto.response.Book;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Book response for cards and search results.
 * 
 * MARKETPLACE PATTERN:
 * - Shows aggregated data across all sellers
 * - "from $X" (minPrice) + "N sellers" (totalListings)
 * - Clicking goes to /books/:id to compare sellers
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookResponse {
    private Long id;
    private String title;
    private String isbn;
    private List<AuthorResponse> authors;
    private List<CategoryResponse> categories;
    private String coverUrl;
    
    // ═══════════════════════════════════════════════════════════════════════════
    // MARKETPLACE FIELDS — aggregated across all active listings
    // ═══════════════════════════════════════════════════════════════════════════
    
    /** Lowest price among all sellers */
    private BigDecimal minPrice;
    
    /** Highest price among all sellers */
    private BigDecimal maxPrice;
    
    /** Currency code (e.g., "VND", "USD") */
    private String currency;
    
    /** Number of active listings (sellers) for this book */
    private Integer totalListings;
    
    /** Average rating of the book */
    private BigDecimal averageRating;
    
    /** Total number of reviews */
    private Integer totalReviews;
}