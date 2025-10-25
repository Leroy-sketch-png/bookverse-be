package com.example.bookverseserver.service.discount;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Chiến lược giảm giá theo phần trăm
 * Ví dụ: Giảm 20% cho đơn hàng
 */
@Component("PERCENTAGE")
@Slf4j
public class PercentageDiscountStrategy implements DiscountStrategy {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final BigDecimal MAX_PERCENTAGE = new BigDecimal("100");

    @Override
    public BigDecimal calculateDiscount(BigDecimal orderAmount, BigDecimal discountValue, BigDecimal minOrderValue) {
        if (!isValid(orderAmount, minOrderValue)) {
            log.warn("Order amount {} is less than minimum order value {}", orderAmount, minOrderValue);
            return BigDecimal.ZERO;
        }

        // Kiểm tra phần trăm hợp lệ (0-100)
        if (discountValue.compareTo(BigDecimal.ZERO) <= 0 || discountValue.compareTo(MAX_PERCENTAGE) > 0) {
            log.warn("Invalid percentage value: {}", discountValue);
            return BigDecimal.ZERO;
        }

        // Tính số tiền giảm = (orderAmount * discountValue) / 100
        BigDecimal discount = orderAmount
                .multiply(discountValue)
                .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);

        log.info("Percentage discount calculated: {} ({}%) for order amount: {}",
                discount, discountValue, orderAmount);
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

