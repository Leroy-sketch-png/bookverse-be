package com.example.bookverseserver.dto.request.Product;

import com.example.bookverseserver.enums.BookCondition;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * Simplified DTO for creating listings via multipart/form-data.
 * Accepts flat fields + image files directly from the frontend form.
 * Used by POST /api/listings/simple endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SimpleListingCreationRequest {

    // ─── Book Metadata ───────────────────────────────────────────────────────────

    @NotBlank(message = "TITLE_REQUIRED")
    @Size(max = 255, message = "TITLE_TOO_LONG")
    String title;

    @NotBlank(message = "AUTHOR_REQUIRED")
    @Size(max = 255, message = "AUTHOR_TOO_LONG")
    String author;

    @Size(max = 17, message = "ISBN_TOO_LONG")
    String isbn;

    @Size(max = 255, message = "PUBLISHER_TOO_LONG")
    String publisher;

    Integer publishYear;

    @NotBlank(message = "CATEGORY_REQUIRED")
    String category;

    @Size(max = 5000, message = "DESCRIPTION_TOO_LONG")
    String description;

    // ─── Listing Details ─────────────────────────────────────────────────────────

    @NotNull(message = "CONDITION_REQUIRED")
    BookCondition condition;

    String conditionNotes;

    @NotNull(message = "PRICE_REQUIRED")
    @Min(value = 0, message = "PRICE_MUST_BE_POSITIVE")
    Double price;

    @Min(value = 0, message = "ORIGINAL_PRICE_MUST_BE_POSITIVE")
    Double originalPrice;

    @NotNull(message = "STOCK_REQUIRED")
    @Min(value = 0, message = "STOCK_MUST_BE_NON_NEGATIVE")
    Integer stock;

    @Builder.Default
    String status = "DRAFT";
}
