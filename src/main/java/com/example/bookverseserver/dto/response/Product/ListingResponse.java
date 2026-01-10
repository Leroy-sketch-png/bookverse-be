package com.example.bookverseserver.dto.response.Product;

import com.example.bookverseserver.dto.response.Book.AuthorResponse;
import com.example.bookverseserver.dto.response.Book.CategoryResponse;
import com.example.bookverseserver.enums.BookCondition;
import com.example.bookverseserver.enums.ListingStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

/**
 * Listing response matching Vision API_CONTRACTS.md §Listings.
 * 
 * Uses nested structures for book, seller, and stats per Vision contract:
 * - book: { id, title, authors, isbn, coverImage }
 * - seller: { id, name, avatar, isPro, rating }
 * - stats: { views, favorites, soldCount }
 * - photos: string[] (URLs only)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListingResponse {

    Long id;
    
    // Nested book info (Vision: "book" object)
    BookInfo book;
    
    // Category info
    CategoryResponse category;
    
    // Nested seller info (Vision: "seller" object)
    SellerInfo seller;
    
    // Pricing
    BigDecimal price;           // Base/list price
    BigDecimal originalPrice;   // Original price before discount
    BigDecimal finalPrice;      // Actual selling price
    
    BookCondition condition;
    ListingStatus status;
    Integer quantity;
    
    // Photos as string URLs (Vision: photos: string[])
    List<String> photos;
    
    // Nested stats (Vision: "stats" object)
    ListingStats stats;
    
    String createdAt;  // ISO timestamp

    // ═══════════════════════════════════════════════════════════════════════════
    // INNER CLASSES FOR NESTED STRUCTURE
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Compact book info nested in listing (Vision: BookMetaCompact)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookInfo {
        Long id;
        String title;
        List<AuthorResponse> authors;
        String isbn;
        String coverImage;
    }

    /**
     * Compact seller info nested in listing (Vision: SellerCompact)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SellerInfo {
        Long id;
        String name;
        String avatar;
        Boolean isPro;
        BigDecimal rating;
    }

    /**
     * Listing stats (Vision: stats object)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ListingStats {
        Integer views;
        Integer favorites;  // Was "likes" in old structure
        Integer soldCount;
    }
}
