package com.example.bookverseserver.enums;

/**
 * Types of content flags.
 */
public enum FlagType {
    SPAM,           // Suspicious keywords, duplicate listings
    COUNTERFEIT,    // Suspected fake products
    PRICING,        // Abnormal pricing (too high/low)
    INAPPROPRIATE,  // Adult content, offensive material
    POLICY_VIOLATION // General policy violation
}
