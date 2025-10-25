package com.example.bookverseserver.service.discount;

import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Factory class để tạo và quản lý các DiscountStrategy
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DiscountStrategyFactory {

    private final Map<String, DiscountStrategy> strategies;

    /**
     * Lấy strategy phù hợp dựa trên loại giảm giá
     * @param discountType Loại giảm giá (FIXED_AMOUNT, PERCENTAGE, BOGO)
     * @return DiscountStrategy tương ứng
     * @throws AppException nếu loại giảm giá không hợp lệ
     */
    public DiscountStrategy getStrategy(String discountType) {
        if (discountType == null || discountType.isBlank()) {
            log.error("Discount type is null or blank");
            throw new AppException(ErrorCode.VOUCHER_DISCOUNT_TYPE_REQUIRED);
        }

        DiscountStrategy strategy = strategies.get(discountType);

        if (strategy == null) {
            log.error("No strategy found for discount type: {}", discountType);
            throw new AppException(ErrorCode.VOUCHER_DISCOUNT_TYPE_INVALID);
        }

        log.info("Using discount strategy: {}", discountType);
        return strategy;
    }
}


