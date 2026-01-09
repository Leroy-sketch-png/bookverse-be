package com.example.bookverseserver.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Listing Status Enum
 * 
 * Vision API Contract specifies UPPERCASE values:
 * - DRAFT: Not yet published
 * - ACTIVE: Available for sale
 * - PAUSED: Temporarily paused by seller
 * - SOLD_OUT: No stock available (auto-set when quantity = 0)
 * - INACTIVE: Temporarily disabled by seller
 * - REMOVED: Soft deleted
 */
public enum ListingStatus {
    DRAFT,
    ACTIVE,
    PAUSED,
    SOLD_OUT,
    INACTIVE,
    SOLD, // Legacy - kept for compatibility
    REMOVED;

    @JsonValue
    public String toValue() {
        return name();
    }

    @JsonCreator
    public static ListingStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        // Accept both uppercase and lowercase for backwards compatibility
        String normalized = value.toUpperCase().replace("-", "_");
        // Handle legacy "out_of_stock" -> "SOLD_OUT"
        if ("OUT_OF_STOCK".equals(normalized)) {
            return SOLD_OUT;
        }
        try {
            return ListingStatus.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown listing status: " + value);
        }
    }
}
