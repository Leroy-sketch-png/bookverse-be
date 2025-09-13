package com.example.bookverseserver.dto.response.Product;

import com.example.bookverseserver.enums.BookCondition;
import com.example.bookverseserver.enums.ListingStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListingUpdateResponse {
    Long id;

    String titleOverride;
    BigDecimal price;
    String currency;

    BookCondition condition;
    Integer quantity;
    String location;

    ListingStatus status;
    Boolean visibility;

    BigDecimal platformFeePercent;
    BigDecimal suggestedPriceLow;
    BigDecimal suggestedPriceHigh;

    Integer views;
    Integer likes;
    Integer soldCount;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    List<String> photoUrls;
}
