package com.example.bookverseserver.dto.request.Product;

import com.example.bookverseserver.dto.request.Book.BookMetaCreationRequest;
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
public class ListingRequest {

    // Option 1: use bookMetaId if the Book is already there
    Long bookMetaId;

    // Option 2: payload to create a new bookMeta on the fly
    BookMetaCreationRequest bookMeta;

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

    List<ListingPhotoRequest> photos;
}
