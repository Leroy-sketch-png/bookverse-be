package com.example.bookverseserver.service.discount;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Chiến lược giảm giá theo số tiền cố định
 * Ví dụ: Giảm 50,000đ cho đơn hàng
 */
@Component("FIXED_AMOUNT")
@Slf4j
public class FixedAmountDiscountStrategy implements DiscountStrategy {

    @Override
    public BigDecimal calculateDiscount(BigDecimal orderAmount, BigDecimal discountValue, BigDecimal minOrderValue) {
        if (!isValid(orderAmount, minOrderValue)) {
            log.warn("Order amount {} is less than minimum order value {}", orderAmount, minOrderValue);
            return BigDecimal.ZERO;
        }

        // Giảm giá không được vượt quá giá trị đơn hàng
        BigDecimal discount = discountValue.min(orderAmount);
        log.info("Fixed amount discount calculated: {} for order amount: {}", discount, orderAmount);
        return discount;
    }

    @Override
    public boolean isValid(BigDecimal orderAmount, BigDecimal minOrderValue) {
        if (minOrderValue == null) {
            return true;
        }
        return orderAmount.compareTo(minOrderValue) >= 0;
    }
}

