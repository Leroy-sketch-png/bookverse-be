package com.example.bookverseserver.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PayoutRequest {

    @NotNull(message = "AMOUNT_REQUIRED")
    @DecimalMin(value = "10.00", message = "MINIMUM_PAYOUT_AMOUNT")
    BigDecimal amount;

    @NotBlank(message = "PAYOUT_METHOD_REQUIRED")
    String method; // e.g., "bank_transfer", "paypal"
}
