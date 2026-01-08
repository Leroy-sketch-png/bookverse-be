package com.example.bookverseserver.dto.request.Order;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApplyVoucherRequest {
    @NotBlank(message = "Voucher code is required")
    String code;
}
