package com.example.bookverseserver.dto.response.Product;

import com.example.bookverseserver.enums.BookCondition;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

/**
 * Summary DTO for related listings shown on listing detail page.
 * Contains minimal data needed to display related listing cards.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RelatedListingDto {
    Long id;
    SellerSummaryDto seller;
    BookCondition condition;
    BigDecimal price;
    Integer stockQuantity;
    String mainPhotoUrl;
}
