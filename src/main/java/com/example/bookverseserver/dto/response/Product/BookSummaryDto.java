package com.example.bookverseserver.dto.response.Product;

import com.example.bookverseserver.dto.response.Book.AuthorResponse;
import com.example.bookverseserver.dto.response.Book.BookDetailResponse.ExternalLinkResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

/**
 * Summary DTO for book information in listing responses.
 * 
 * IMPORTANT: For listing detail pages, this must include rich discovery data
 * (firstLine, subjectPlaces, etc.) because the frontend expects it.
 * This is NOT just a "summary" — it's the full book data for the product page.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookSummaryDto {
    Long id;
    String title;
    String author; // Primary author name (kept for backward compatibility)
    List<AuthorResponse> authors; // Full author details
    String isbn;
    String coverImage;
    BigDecimal averageRating;
    Integer totalReviews;
    
    // ═══════════════════════════════════════════════════════════════════════════
    // DESCRIPTION & RICH DISCOVERY DATA (for product detail pages)
    // ═══════════════════════════════════════════════════════════════════════════
    
    String description;
    
    /** Famous opening line (marketing gold!) */
    String firstLine;
    
    /** Story locations: ["England", "Derbyshire"] */
    List<String> subjectPlaces;
    
    /** Characters: ["Elizabeth Bennet", "Mr. Darcy"] */
    List<String> subjectPeople;
    
    /** Time periods: ["19th century"] */
    List<String> subjectTimes;
    
    /** External links: Wikipedia, Britannica, etc. */
    List<ExternalLinkResponse> externalLinks;
    
    /** Genre tags: ["Romance", "Historical Fiction"] */
    List<String> tags;
}
