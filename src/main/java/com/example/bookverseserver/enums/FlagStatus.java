package com.example.bookverseserver.enums;

/**
 * Status for flagged listings.
 */
public enum FlagStatus {
    PENDING,      // Awaiting moderator review
    REVIEWING,    // Moderator is investigating
    RESOLVED,     // Action taken
    DISMISSED     // Cleared, no action needed
}
