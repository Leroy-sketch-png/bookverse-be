package com.example.bookverseserver.dto.response.Product;

import com.example.bookverseserver.dto.response.Book.CategoryResponse;
import com.example.bookverseserver.enums.BookCondition;
import com.example.bookverseserver.enums.ListingStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Detailed response DTO for single listing view.
 * Includes full book info, seller info, shipping, and related listings.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListingDetailResponse {
    Long id;

    // Book information
    BookSummaryDto book;

    // Category information
    CategoryResponse category;

    // Seller information
    SellerSummaryDto seller;

    // Listing details
    BookCondition condition;
    BigDecimal price;           // Base/list price
    BigDecimal originalPrice;   // Original price before discount (if any)
    BigDecimal finalPrice;      // Actual selling price (per Vision API_CONTRACTS.md)
    Integer discount;           // Calculated percentage discount
    Integer quantity;           // Stock quantity (renamed from stockQuantity per Vision)
    ListingStatus status;
    String description;
    List<String> photos;        // Photo URLs (renamed from images per Vision)

    // Shipping info
    ShippingInfoDto shippingInfo;

    // Statistics
    Integer views;              // View count (renamed from viewCount per Vision)
    Integer soldCount;

    // Timestamps
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    // Related listings (same book, different sellers)
    List<RelatedListingDto> relatedListings;
}
