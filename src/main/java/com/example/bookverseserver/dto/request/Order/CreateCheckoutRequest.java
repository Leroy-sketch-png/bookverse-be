package com.example.bookverseserver.dto.request.Order;

import com.example.bookverseserver.dto.request.ShippingAddress.ShippingAddressRequest;
import com.example.bookverseserver.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Request DTO for creating a checkout session.
 * Added validation annotations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateCheckoutRequest {
  
  @NotNull(message = "CART_ID_REQUIRED")
  Long cartId;
  
  @NotNull(message = "SHIPPING_ADDRESS_REQUIRED")
  @Valid
  ShippingAddressRequest shippingAddress;
  
  @Valid
  ShippingAddressRequest billingAddress;
  
  @NotNull(message = "PAYMENT_METHOD_REQUIRED")
  PaymentMethod paymentMethod;
  
  @Size(max = 50, message = "PROMO_CODE_TOO_LONG")
  String promoCode;
  
  @Size(max = 500, message = "NOTES_TOO_LONG")
  String notes;
}
