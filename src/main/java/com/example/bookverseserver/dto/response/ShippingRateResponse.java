package com.example.bookverseserver.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

/**
 * Shipping rate response from carrier API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShippingRateResponse {
    
    BigDecimal fee;              // Total shipping fee
    BigDecimal serviceFee;       // Base service fee
    BigDecimal insuranceFee;     // Insurance fee
    String estimatedDeliveryDate; // Expected delivery date
    String serviceType;           // Service name (Standard, Express, etc.)
    String carrierName;           // Carrier name (GHN, GHTK, etc.)
}
