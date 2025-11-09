package com.example.bookverseserver.dto.response.Voucher;

import com.example.bookverseserver.enums.DiscountType;

import java.math.BigDecimal;

public record VoucherCartResponse(
        Long id,
        String code,
        DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal minOrderValue
) {
}
