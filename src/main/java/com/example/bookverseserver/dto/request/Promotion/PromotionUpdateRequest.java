package com.example.bookverseserver.dto.request.Promotion;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PromotionUpdateRequest {
    
    @Size(max = 255, message = "PROMOTION_NAME_TOO_LONG")
    String name;
    
    @Min(value = 5, message = "DISCOUNT_MIN_5")
    @Max(value = 90, message = "DISCOUNT_MAX_90")
    Integer discountPercentage;
    
    LocalDateTime startDate;
    
    LocalDateTime endDate;
    
    List<Long> listingIds;
}
