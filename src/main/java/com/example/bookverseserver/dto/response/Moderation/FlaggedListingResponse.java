package com.example.bookverseserver.dto.response.Moderation;

import com.example.bookverseserver.enums.FlagSeverity;
import com.example.bookverseserver.enums.FlagStatus;
import com.example.bookverseserver.enums.FlagType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlaggedListingResponse {
    Long id;
    ListingSummary listing;
    FlagType flagType;
    String flagReason;
    Double confidenceScore;
    FlagSeverity severity;
    FlagStatus status;
    String reviewNote;
    LocalDateTime flaggedAt;
    LocalDateTime reviewedAt;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ListingSummary {
        Long id;
        String title;
        java.math.BigDecimal price;
        String imageUrl;
        SellerSummary seller;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SellerSummary {
        Long id;
        String name;
        Double rating;
        LocalDateTime joinedAt;
        Integer listingCount;
    }
}
