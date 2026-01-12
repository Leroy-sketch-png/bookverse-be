package com.example.bookverseserver.dto.response.Product;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * Response DTO for bulk listing upload.
 * Returns detailed success/failure breakdown for each item.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BulkUploadResponse {

    int successCount;
    int failureCount;
    List<ListingResponse> listings;
    List<BulkUploadError> errors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkUploadError {
        int index;      // 0-indexed position in the input array
        int rowNumber;  // 1-indexed row number for user display (index + 2 if header row exists)
        String title;   // Book title for easy identification
        String message; // Error description
    }
}
