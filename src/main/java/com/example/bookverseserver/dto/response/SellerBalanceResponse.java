package com.example.bookverseserver.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SellerBalanceResponse {
    BigDecimal totalEarnings;       // All-time delivered order revenue
    BigDecimal pendingBalance;      // Earnings not yet requested as payout
    BigDecimal processingPayouts;   // Payouts in PENDING/PROCESSING status
    BigDecimal completedPayouts;    // All-time completed payouts
    BigDecimal availableForPayout;  // What can be requested now
    BigDecimal commissionRate;      // 3% for PRO, 8% for casual
}
