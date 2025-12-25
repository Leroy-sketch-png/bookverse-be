package com.example.bookverseserver.dto.request.Order;

import com.example.bookverseserver.dto.request.ShippingAddress.ShippingAddressRequest;
import com.example.bookverseserver.enums.PaymentMethod;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateCheckoutRequest {
  Long cartId;
  ShippingAddressRequest shippingAddress;
  ShippingAddressRequest billingAddress;
  PaymentMethod paymentMethod;
  String promoCode;
  String notes;
}
