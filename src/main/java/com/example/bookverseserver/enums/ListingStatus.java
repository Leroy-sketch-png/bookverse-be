package com.example.bookverseserver.enums;

public enum ListingStatus {
    DRAFT, // Not yet published
    ACTIVE, // Available for sale
    PAUSED, // Temporarily paused by seller
    SOLD_OUT, // No stock available (auto-set when quantity = 0)
    INACTIVE, // Temporarily disabled by seller
    SOLD, // Legacy - kept for compatibility
    REMOVED // Soft deleted
}
