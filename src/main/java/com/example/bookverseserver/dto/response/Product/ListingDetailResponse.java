package com.example.bookverseserver.dto.response.Product;

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

    // Seller information
    SellerSummaryDto seller;

    // Listing details
    BookCondition condition;
    BigDecimal price;
    BigDecimal originalPrice;
    Integer discount; // Calculated percentage discount
    Integer stockQuantity;
    ListingStatus status;
    String description;
    List<String> images; // Photo URLs

    // Shipping info
    ShippingInfoDto shippingInfo;

    // Statistics
    Integer viewCount;
    Integer soldCount;

    // Timestamps
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    // Related listings (same book, different sellers)
    List<RelatedListingDto> relatedListings;
}
