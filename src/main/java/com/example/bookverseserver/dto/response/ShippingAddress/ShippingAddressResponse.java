package com.example.bookverseserver.dto.response.ShippingAddress;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Shipping address response matching Vision/FE ShippingAddressData type.
 * 
 * FE expects: id, userId, fullName, phoneNumber, addressLine1, addressLine2, 
 *             city, postalCode, country, isDefault, createdAt, updatedAt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShippingAddressResponse {

    Long id;
    Long userId;

    String fullName;
    String phoneNumber; // Matches FE ShippingAddressData.phoneNumber
    
    String addressLine1;
    String addressLine2;

    String city;
    String postalCode;
    String country;

    Boolean isDefault;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    
    // Additional fields for order context (not in FE ShippingAddressData but useful)
    String ward;
    String district;
    String note;
}
