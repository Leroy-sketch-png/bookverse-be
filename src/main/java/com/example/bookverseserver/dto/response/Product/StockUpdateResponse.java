package com.example.bookverseserver.dto.response.Product;

import com.example.bookverseserver.enums.ListingStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Response DTO for stock update operations.
 * Returns the listing ID, old/new quantities, and resulting status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StockUpdateResponse {
    Long listingId;
    Integer oldQuantity;
    Integer newQuantity;
    ListingStatus status;
}
