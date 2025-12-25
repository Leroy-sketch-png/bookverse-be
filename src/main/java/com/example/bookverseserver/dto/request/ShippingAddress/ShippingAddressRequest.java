package com.example.bookverseserver.dto.request.ShippingAddress;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShippingAddressRequest {

    @NotBlank(message = "SHIPPING_ADDRESS_FULL_NAME_REQUIRED")
    @Size(max = 100, message = "SHIPPING_ADDRESS_FULL_NAME_TOO_LONG")
    String fullName;

    @NotBlank(message = "SHIPPING_ADDRESS_PHONE_REQUIRED")
    @Pattern(regexp = "^[0-9]{10}$", message = "SHIPPING_ADDRESS_PHONE_INVALID")
    String phoneNumber;

    @NotBlank(message = "SHIPPING_ADDRESS_LINE1_REQUIRED")
    String addressLine1;

    String addressLine2;

    @NotBlank(message = "SHIPPING_ADDRESS_CITY_REQUIRED")
    @Size(max = 100, message = "SHIPPING_ADDRESS_CITY_TOO_LONG")
    String city;

    @Size(max = 20, message = "SHIPPING_ADDRESS_POSTAL_CODE_TOO_LONG")
    String postalCode;

    @NotBlank(message = "SHIPPING_ADDRESS_COUNTRY_REQUIRED")
    @Size(max = 100, message = "SHIPPING_ADDRESS_COUNTRY_TOO_LONG")
    String country;

    @Builder.Default
    Boolean isDefault = false;
}

