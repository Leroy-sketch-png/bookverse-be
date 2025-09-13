package com.example.bookverseserver.dto.request.Product;

import com.example.bookverseserver.enums.BookCondition;
import com.example.bookverseserver.enums.ListingStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListingUpdateRequest {
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

    // photo URLs or upload identifiers
    List<String> photoUrls;
}
