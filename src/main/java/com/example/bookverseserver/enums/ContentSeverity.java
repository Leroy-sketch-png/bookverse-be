package com.example.bookverseserver.enums;

/**
 * Severity level of content issue
 */
public enum ContentSeverity {
    LOW,      // Minor issue, context-dependent
    MEDIUM,   // Clear violation, should flag
    HIGH,     // Serious violation
    CRITICAL  // Immediate block, zero tolerance
}
