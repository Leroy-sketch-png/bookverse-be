package com.example.bookverseserver.service.discount;

import java.math.BigDecimal;

/**
 * Strategy Pattern interface cho các loại giảm giá
 */
public interface DiscountStrategy {

    /**
     * Tính toán số tiền giảm giá
     * @param orderAmount Tổng giá trị đơn hàng
     * @param discountValue Giá trị giảm giá (có thể là số tiền hoặc phần trăm)
     * @param minOrderValue Giá trị đơn hàng tối thiểu
     * @return Số tiền được giảm
     */
    BigDecimal calculateDiscount(BigDecimal orderAmount, BigDecimal discountValue, BigDecimal minOrderValue);

    /**
     * Kiểm tra xem voucher có hợp lệ với đơn hàng không
     * @param orderAmount Tổng giá trị đơn hàng
     * @param minOrderValue Giá trị đơn hàng tối thiểu
     * @return true nếu hợp lệ, false nếu không
     */
    boolean isValid(BigDecimal orderAmount, BigDecimal minOrderValue);
}

