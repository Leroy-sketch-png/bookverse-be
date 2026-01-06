package com.example.bookverseserver.enums;

/**
 * Types of moderation actions.
 */
public enum ModerationActionType {
    APPROVE,            // Content approved
    REQUEST_CHANGES,    // Changes required
    REMOVE_LISTING,     // Listing removed
    BAN_LISTING,        // Permanently ban listing
    WARN_USER,          // Issue warning
    SUSPEND_USER,       // Temporary suspension
    BAN_USER,           // Permanent ban
    REFUND_ORDER,       // Initiate refund
    DISMISS             // Dismiss without action
}
