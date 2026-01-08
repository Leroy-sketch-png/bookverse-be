package com.example.bookverseserver.dto.request.Order;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateCheckoutSessionRequest {
    Long shippingAddressId;
    String notes;
}
