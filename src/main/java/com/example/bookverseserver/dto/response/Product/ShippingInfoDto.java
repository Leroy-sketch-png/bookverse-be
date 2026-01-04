package com.example.bookverseserver.dto.response.Product;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * DTO for shipping information in listing responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShippingInfoDto {
    Boolean freeShipping;
    String estimatedDays;
    String shipsFrom;
}
