package com.example.bookverseserver.dto.request.Voucher;

import com.example.bookverseserver.enums.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class VoucherRequest {

    @NotBlank(message = "VOUCHER_CODE_REQUIRED")
    String code;

    @NotNull(message = "VOUCHER_DISCOUNT_TYPE_REQUIRED")
    DiscountType discountType;

    @NotNull(message = "VOUCHER_DISCOUNT_VALUE_REQUIRED")
    @Positive(message = "VOUCHER_DISCOUNT_VALUE_INVALID")
    BigDecimal discountValue;

    @Positive(message = "VOUCHER_MIN_ORDER_VALUE_INVALID")
    BigDecimal minOrderValue;

    String description;

    @NotNull(message = "VOUCHER_VALID_TO_REQUIRED")
    LocalDateTime validTo;

    @NotNull(message = "VOUCHER_MAX_USAGE_PER_USER_REQUIRED")
    @Positive(message = "VOUCHER_MAX_USAGE_PER_USER_INVALID")
    Integer maxUsagePerUser;
}
