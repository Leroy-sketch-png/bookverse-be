package com.example.bookverseserver.dto.request.Moderation;

import com.example.bookverseserver.enums.FlagSeverity;
import com.example.bookverseserver.enums.FlagType;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Request to flag a listing for moderation review.
 * Used by moderators or automated systems to add listings to the moderation queue.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FlagListingRequest {
    
    @NotNull(message = "LISTING_ID_REQUIRED")
    Long listingId;
    
    @NotNull(message = "FLAG_TYPE_REQUIRED")
    FlagType flagType;
    
    String flagReason;
    
    @Builder.Default
    FlagSeverity severity = FlagSeverity.MEDIUM;
    
    Double confidenceScore;
}
