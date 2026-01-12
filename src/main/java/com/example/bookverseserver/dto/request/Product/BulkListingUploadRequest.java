package com.example.bookverseserver.dto.request.Product;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * Request DTO for PRO seller bulk listing upload.
 * Used by POST /api/seller/listings/bulk-upload endpoint.
 * 
 * Per Vision API_CONTRACTS.md §7.2 Bulk Upload (PRO Only).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BulkListingUploadRequest {

    @NotEmpty(message = "BULK_UPLOAD_EMPTY")
    @Size(max = 100, message = "BULK_UPLOAD_TOO_MANY_ITEMS")
    @Valid
    List<BulkListingItem> books;
}
