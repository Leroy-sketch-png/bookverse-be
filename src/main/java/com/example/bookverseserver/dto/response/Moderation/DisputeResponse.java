package com.example.bookverseserver.dto.response.Moderation;

import com.example.bookverseserver.enums.DisputeStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DisputeResponse {
    Long id;
    OrderInfo order;
    PartyInfo buyer;
    PartyInfo seller;
    String reason;
    String description;
    BigDecimal disputedAmount;
    List<String> evidenceUrls;
    DisputeStatus status;
    String sellerResponse;
    LocalDateTime sellerRespondedAt;
    String resolution;
    BigDecimal refundAmount;
    LocalDateTime createdAt;
    LocalDateTime resolvedAt;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderInfo {
        Long id;
        String orderNumber;
        BigDecimal total;
        LocalDateTime date;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PartyInfo {
        Long id;
        String name;
        String email;
        Double rating;
        Integer disputeCount;
    }
}
