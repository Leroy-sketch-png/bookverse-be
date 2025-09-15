package com.example.bookverseserver.dto.request.Product;

import com.example.bookverseserver.dto.request.Book.BookMetaCreationRequest;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListingCreationRequest {
    Long bookMetaId;
    BookMetaCreationRequest bookMetaPayload;
    ListingRequest listing;
    List<ListingPhotoRequest> photos;
}

