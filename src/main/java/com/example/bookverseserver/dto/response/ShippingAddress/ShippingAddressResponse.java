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

    String phoneNumber;

    String addressLine1;

    String addressLine2;

    String city;

    String postalCode;

    String country;

    Boolean isDefault;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;
}
