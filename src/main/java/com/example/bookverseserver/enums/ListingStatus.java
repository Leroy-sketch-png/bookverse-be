package com.example.bookverseserver.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Listing Status Enum
 * Frontend expects lowercase values: active, out_of_stock, draft
 * Backend maintains uppercase for Java conventions
 * 
 * Mapping:
 * - DRAFT -> draft
 * - ACTIVE -> active  
 * - SOLD_OUT -> out_of_stock
 * - PAUSED, INACTIVE, REMOVED -> Additional backend statuses
 */
public enum ListingStatus {
    DRAFT("draft"), // Not yet published
    ACTIVE("active"), // Available for sale
    PAUSED("paused"), // Temporarily paused by seller
    SOLD_OUT("out_of_stock"), // No stock available (auto-set when quantity = 0)
    INACTIVE("inactive"), // Temporarily disabled by seller
    SOLD("sold"), // Legacy - kept for compatibility
    REMOVED("removed"); // Soft deleted
    
    private final String value;
    
    ListingStatus(String value) {
        this.value = value;
    }
    
    @JsonValue
    public String getValue() {
        return value;
    }
    
    // Helper method to convert from frontend string to enum
    public static ListingStatus fromValue(String value) {
        for (ListingStatus status : ListingStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown listing status: " + value);
    }
}
