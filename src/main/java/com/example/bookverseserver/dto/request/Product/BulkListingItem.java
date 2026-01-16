package com.example.bookverseserver.dto.request.Product;

import com.example.bookverseserver.enums.BookCondition;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Single item in bulk listing upload.
 * Maps to frontend BulkBookEntry type.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BulkListingItem {

    @NotBlank(message = "TITLE_REQUIRED")
    @Size(max = 200, message = "TITLE_TOO_LONG")
    String title;

    @NotBlank(message = "AUTHOR_REQUIRED")
    @Size(max = 100, message = "AUTHOR_TOO_LONG")
    String author;

    @Size(max = 13, message = "ISBN_TOO_LONG")
    String isbn;

    @NotBlank(message = "PUBLISHER_REQUIRED")
    @Size(max = 100, message = "PUBLISHER_TOO_LONG")
    String publisher;

    @NotNull(message = "PUBLISH_YEAR_REQUIRED")
    @Min(value = 1800, message = "PUBLISH_YEAR_TOO_OLD")
    @Max(value = 2100, message = "PUBLISH_YEAR_TOO_NEW")
    Integer publishYear;

    @NotBlank(message = "CATEGORY_REQUIRED")
    String category;

    @NotNull(message = "CONDITION_REQUIRED")
    BookCondition condition;

    @NotNull(message = "PRICE_REQUIRED")
    @DecimalMin(value = "1000", message = "PRICE_TOO_LOW")
    @DecimalMax(value = "50000000", message = "PRICE_TOO_HIGH")
    Double price;

    @NotNull(message = "STOCK_REQUIRED")
    @Min(value = 1, message = "STOCK_TOO_LOW")
    @Max(value = 10000, message = "STOCK_TOO_HIGH")
    Integer stock;

    @NotBlank(message = "DESCRIPTION_REQUIRED")
    @Size(min = 20, max = 2000, message = "DESCRIPTION_LENGTH_INVALID")
    String description;
}
