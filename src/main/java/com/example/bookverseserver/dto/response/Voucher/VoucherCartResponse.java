package com.example.bookverseserver.dto.response.Voucher;

import com.example.bookverseserver.enums.DiscountType;
import lombok.Builder;

import java.math.BigDecimal;
@Builder
public record VoucherCartResponse(
        Long id,
        String code,
        DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal minOrderValue,
        Integer maxUsagePerUser
) {
}
