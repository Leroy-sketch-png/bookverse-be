package com.example.bookverseserver.dto.response.Cart;

import com.example.bookverseserver.dto.response.CartItem.CartItemForCartResponse;
import com.example.bookverseserver.dto.response.Voucher.VoucherCartResponse;
import lombok.Builder;

import java.util.List;

/**
 * Cart response matching Vision API_CONTRACTS.md §Cart
 * Uses nested summary instead of flat financial fields.
 */
@Builder
public record CartResponse(
        Long id,
        List<CartItemForCartResponse> cartItems,
        CartSummary summary,
        VoucherCartResponse voucher,
        Integer itemCount
) {
}