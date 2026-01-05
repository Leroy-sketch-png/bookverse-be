package com.example.bookverseserver.dto.response.ShippingAddress;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShippingAddressResponse {

    Long id;

    String fullName;

    String phone; // Match frontend naming (not phoneNumber)

    // Frontend expects: street, city, state, zipCode
    String street; // Maps from addressLine1 + addressLine2
    
    String ward; // Additional Vietnamese address field
    
    String district; // Additional Vietnamese address field (can map to state)

    String city;
    
    String state; // Can be mapped from district for frontend compatibility

    String zipCode; // Match frontend naming (not postalCode)
    
    String postalCode; // Keep for backward compatibility
    
    String country;
    
    String note; // Additional field from entity

    Boolean isDefault;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;
}
