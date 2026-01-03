package com.example.bookverseserver.dto.response.Promotion;

import com.example.bookverseserver.enums.PromotionStatus;
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
public class PromotionResponse {
    Long id;
    String name;
    Integer discountPercentage;
    LocalDateTime startDate;
    LocalDateTime endDate;
    PromotionStatus status;
    List<Long> appliedBooks; // Listing IDs
    BigDecimal totalRevenue;
    Integer itemsSold;
    LocalDateTime createdAt;
}
