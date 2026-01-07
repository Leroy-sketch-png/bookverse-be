package com.example.bookverseserver.dto.response.Seller;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

/**
 * Listing displayed on public seller profile.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SellerProfileListingResponse {
    Long id;
    String title;
    BigDecimal price;
    String image;
    String condition;
    Double rating;
    Integer soldCount;
    String category;
}
