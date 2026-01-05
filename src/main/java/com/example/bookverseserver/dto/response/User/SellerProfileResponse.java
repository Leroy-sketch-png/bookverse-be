package com.example.bookverseserver.dto.response.User;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SellerProfileResponse {
    String id; // Changed to String to match frontend expectations
    String username;
    String displayName;
    String avatarUrl;
    String coverImageUrl;
    String bio;
    String location;
    LocalDate memberSince;
    Boolean isVerified;
    Boolean isProSeller;
    String badge;
    SellerStats stats;
    List<String> tags;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SellerStats {
        Integer totalSales;
        Double averageRating;
        Integer totalReviews;
        Double fulfillmentRate;
        String responseTime;
        Double repeatBuyerRate;
        String membershipDuration;
    }
}
