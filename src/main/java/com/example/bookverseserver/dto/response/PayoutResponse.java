package com.example.bookverseserver.dto.response;

import com.example.bookverseserver.enums.SellerPayoutStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PayoutResponse {
    Long id;
    BigDecimal amount;
    SellerPayoutStatus status;
    String method;
    String externalReference;
    LocalDateTime createdAt;
    LocalDateTime paidAt;
    
    // Seller info for admin view
    Long sellerId;
    String sellerUsername;
    String sellerDisplayName;
    String sellerEmail;
}
