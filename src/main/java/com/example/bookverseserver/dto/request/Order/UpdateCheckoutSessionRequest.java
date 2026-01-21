package com.example.bookverseserver.dto.request.Order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Request DTO for updating a checkout session.
 * Supports both selecting existing address or creating new inline.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateCheckoutSessionRequest {
    // Option 1: Select existing address by ID
    Long shippingAddressId;
    
    // Option 2: Create new address inline during checkout
    @Valid
    InlineShippingAddressRequest shippingAddress;
    
    @Size(max = 500, message = "NOTES_TOO_LONG")
    String notes;
    
    /**
     * Inline shipping address for checkout (can be saved for future use)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class InlineShippingAddressRequest {
        @Size(max = 100, message = "SHIPPING_ADDRESS_FULL_NAME_TOO_LONG")
        String fullName;
        
        @Size(max = 20, message = "SHIPPING_ADDRESS_PHONE_TOO_LONG")
        String phoneNumber;
        
        @Size(max = 200, message = "SHIPPING_ADDRESS_LINE1_TOO_LONG")
        String addressLine1;
        
        @Size(max = 200, message = "SHIPPING_ADDRESS_LINE2_TOO_LONG")
        String addressLine2;
        
        @Size(max = 100, message = "SHIPPING_ADDRESS_CITY_TOO_LONG")
        String city;
        
        @Size(max = 20, message = "SHIPPING_ADDRESS_POSTAL_CODE_TOO_LONG")
        String postalCode;
        
        @Size(max = 100, message = "SHIPPING_ADDRESS_COUNTRY_TOO_LONG")
        String country;
        
        // GHN shipping integration fields
        Integer provinceId;
        String district;
        Integer districtId;
        String ward;
        String wardCode;
        
        @Size(max = 500, message = "NOTES_TOO_LONG")
        String note;
        
        Boolean isDefault;
        Boolean saveForFuture;
    }
}
