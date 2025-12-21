package com.example.bookverseserver.dto.response.Cart;

import com.example.bookverseserver.dto.response.CartItem.CartItemForCartResponse;
import com.example.bookverseserver.dto.response.CartItem.CartItemResponse;
import com.example.bookverseserver.dto.response.Voucher.VoucherCartResponse;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Set;

@Builder
public record CartResponse(
      Long id,
      Long userId,
      BigDecimal totalPrice,
      BigDecimal discount,
      VoucherCartResponse voucher,
      Set<CartItemForCartResponse> cartItems
) {
}
