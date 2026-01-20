package com.example.bookverseserver.dto.request.Order;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Request DTO for updating a checkout session.
 * Added validation for notes length.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateCheckoutSessionRequest {
    Long shippingAddressId;
    
    @Size(max = 500, message = "NOTES_TOO_LONG")
    String notes;
}
