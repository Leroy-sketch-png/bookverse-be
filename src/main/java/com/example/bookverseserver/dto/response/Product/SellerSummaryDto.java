package com.example.bookverseserver.dto.response.Product;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Summary DTO for seller information in listing detail responses.
 * Extends basic seller info with additional stats.
 * 
 * Vision field mapping:
 * - name → seller's display name (consistent with SellerInfo)
 * - isPro → PRO seller status (consistent with SellerInfo)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SellerSummaryDto {
    Long id;
    String username;
    String name; // Display name (renamed from businessName for consistency)
    String avatar; // From avatarUrl in profile
    Double rating; // Average rating
    Integer totalSales; // Total sold count
    Integer totalReviews; // Rating count
    Boolean isPro; // PRO seller status (renamed from isProSeller for consistency)
    LocalDateTime memberSince; // Account creation date
}
