package com.example.bookverseserver.dto.response.Order;

import com.example.bookverseserver.dto.response.ShippingAddress.ShippingAddressResponse;
import com.example.bookverseserver.enums.OrderStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SellerOrderResponse {
    UUID id;
    String orderNumber;
    OrderStatus status;
    String buyerName;
    String buyerEmail;
    ShippingAddressResponse shippingAddress;
    List<SellerOrderItemResponse> books;
    BigDecimal totalPrice;
    String trackingNumber;
    LocalDateTime shippedAt;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
