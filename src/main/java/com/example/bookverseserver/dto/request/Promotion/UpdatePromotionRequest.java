package com.example.bookverseserver.dto.request.Promotion;

import com.example.bookverseserver.enums.PromotionStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdatePromotionRequest {
    
    @Size(max = 255, message = "Name cannot exceed 255 characters")
    String name;
    
    @Min(value = 1, message = "Discount must be at least 1%")
    @Max(value = 100, message = "Discount cannot exceed 100%")
    Integer discountPercentage;
    
    LocalDateTime startDate;
    LocalDateTime endDate;
    PromotionStatus status;
    List<Long> appliedListingIds;
}
