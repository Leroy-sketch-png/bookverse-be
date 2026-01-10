package com.example.bookverseserver.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for filing a dispute on an order.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DisputeRequest {

    @NotNull(message = "ORDER_REQUIRED")
    Long orderId;

    @NotBlank(message = "DISPUTE_REASON_REQUIRED")
    @Size(max = 100, message = "DISPUTE_REASON_TOO_LONG")
    String reason;

    @NotBlank(message = "DISPUTE_DESCRIPTION_REQUIRED")
    @Size(min = 20, max = 2000, message = "DISPUTE_DESCRIPTION_TOO_SHORT")
    String description;

    BigDecimal disputedAmount;

    List<String> evidenceUrls;
}
