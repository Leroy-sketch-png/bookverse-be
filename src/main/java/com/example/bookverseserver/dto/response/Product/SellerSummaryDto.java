package com.example.bookverseserver.dto.response.Product;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Summary DTO for seller information in listing responses.
 * Contains seller profile data for listing display.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SellerSummaryDto {
    Long id;
    String username;
    String businessName; // From displayName in profile
    String avatar; // From avatarUrl in profile
    Double rating; // Average rating
    Integer totalSales; // Total sold count
    Integer totalReviews; // Rating count
    Boolean isProSeller; // Pro seller status
    LocalDateTime memberSince; // Account creation date
}
