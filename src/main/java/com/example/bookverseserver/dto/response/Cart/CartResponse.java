package com.example.bookverseserver.dto.response.Cart;

import com.example.bookverseserver.dto.response.CartItem.CartItemForCartResponse;
import com.example.bookverseserver.dto.response.Voucher.VoucherCartResponse;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.Set;

@Builder
public record CartResponse(
        Long id,
        Long userId,

        // --- CÁC TRƯỜNG TÍNH TOÁN (Financial Breakdown) ---
        BigDecimal subtotal,    // Tổng tiền hàng (tương đương totalPrice cũ trong DB)
        BigDecimal tax,         // Thuế (9%) - Mới thêm
        BigDecimal shipping,    // Phí ship - Mới thêm
        BigDecimal discount,    // Tiền giảm giá
        BigDecimal total,       // TỔNG THANH TOÁN CUỐI CÙNG (User phải trả) - Mới thêm

        // --- META DATA ---
        Integer itemCount,

        // --- OBJECTS ---
        VoucherCartResponse voucher,
        Set<CartItemForCartResponse> cartItems
) {
}