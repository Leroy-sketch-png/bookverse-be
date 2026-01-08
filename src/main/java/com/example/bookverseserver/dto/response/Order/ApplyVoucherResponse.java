package com.example.bookverseserver.dto.response.Order;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

/**
 * Response when applying a voucher to checkout
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApplyVoucherResponse {
    String code;
    String discountType; // PERCENTAGE, FIXED
    BigDecimal discountValue;
    BigDecimal discountAmount; // Actual discount applied to this order
    BigDecimal newTotal; // Updated order total after discount
}
