package com.example.bookverseserver.enums;

/**
 * Status for disputes.
 */
public enum DisputeStatus {
    OPEN,           // Newly filed
    INVESTIGATING,  // Under review
    AWAITING_RESPONSE, // Waiting for seller/buyer response
    RESOLVED,       // Resolved
    ESCALATED       // Escalated to higher authority
}
