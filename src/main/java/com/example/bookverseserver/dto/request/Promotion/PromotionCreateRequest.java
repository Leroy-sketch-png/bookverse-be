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
public class PromotionCreateRequest {
    
    @NotBlank(message = "PROMOTION_NAME_REQUIRED")
    @Size(max = 255, message = "PROMOTION_NAME_TOO_LONG")
    String name;
    
    @NotNull(message = "DISCOUNT_PERCENTAGE_REQUIRED")
    @Min(value = 1, message = "DISCOUNT_MIN_1")
    @Max(value = 100, message = "DISCOUNT_MAX_100")
    Integer discountPercentage;
    
    @NotNull(message = "START_DATE_REQUIRED")
    LocalDateTime startDate;
    
    @NotNull(message = "END_DATE_REQUIRED")
    LocalDateTime endDate;
    
    @NotEmpty(message = "LISTING_IDS_REQUIRED")
    List<Long> listingIds;
}
