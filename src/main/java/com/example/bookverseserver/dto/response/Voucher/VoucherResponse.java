package com.example.bookverseserver.dto.response.Voucher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class VoucherResponse {

    Long id;

    String code;

    String discountType;

    BigDecimal discountValue;

    BigDecimal minOrderValue;

    String description;

    LocalDateTime validTo;
}
