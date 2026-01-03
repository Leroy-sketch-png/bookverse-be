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
public class CreatePromotionRequest {
    
    @NotBlank(message = "Promotion name is required")
    @Size(max = 255, message = "Name cannot exceed 255 characters")
    String name;
    
    @NotNull(message = "Discount percentage is required")
    @Min(value = 1, message = "Discount must be at least 1%")
    @Max(value = 100, message = "Discount cannot exceed 100%")
    Integer discountPercentage;
    
    @NotNull(message = "Start date is required")
    LocalDateTime startDate;
    
    @NotNull(message = "End date is required")
    LocalDateTime endDate;
    
    @NotEmpty(message = "At least one listing must be selected")
    List<Long> appliedListingIds;
}
