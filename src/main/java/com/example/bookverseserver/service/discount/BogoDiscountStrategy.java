package com.example.bookverseserver.service.discount;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Chiến lược giảm giá BOGO (Buy One Get One)
 * Ví dụ: Mua 1 tặng 1 - discountValue = 100 (giảm 100% cho sản phẩm thứ 2)
 *        Mua 1 giảm 50% sản phẩm thứ 2 - discountValue = 50
 *
 * Logic: Giảm discountValue% cho 50% giá trị đơn hàng
 * (giả định rằng có ít nhất 2 sản phẩm với giá bằng nhau)
 */
@Component("BOGO")
@Slf4j
public class BogoDiscountStrategy implements DiscountStrategy {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final BigDecimal FIFTY = new BigDecimal("50");

    @Override
    public BigDecimal calculateDiscount(BigDecimal orderAmount, BigDecimal discountValue, BigDecimal minOrderValue) {
        if (!isValid(orderAmount, minOrderValue)) {
            log.warn("Order amount {} is less than minimum order value {}", orderAmount, minOrderValue);
            return BigDecimal.ZERO;
        }

        // Kiểm tra discountValue hợp lệ (0-100)
        if (discountValue.compareTo(BigDecimal.ZERO) <= 0 || discountValue.compareTo(ONE_HUNDRED) > 0) {
            log.warn("Invalid BOGO discount value: {}", discountValue);
            return BigDecimal.ZERO;
        }

        // BOGO: Giảm discountValue% cho 50% giá trị đơn hàng
        // Ví dụ: Đơn hàng 1,000,000đ, BOGO 100% -> Giảm 50% của 1,000,000 = 500,000đ
        //        Đơn hàng 1,000,000đ, BOGO 50% -> Giảm 50% của (50% * 1,000,000) = 250,000đ
        BigDecimal halfOrderAmount = orderAmount
                .multiply(FIFTY)
                .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);

        BigDecimal discount = halfOrderAmount
                .multiply(discountValue)
                .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);

        log.info("BOGO discount calculated: {} ({}% on half of order) for order amount: {}",
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

