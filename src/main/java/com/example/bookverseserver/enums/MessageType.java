package com.example.bookverseserver.enums;

/**
 * Types of messages in the messaging system.
 */
public enum MessageType {
    /**
     * Regular text message
     */
    TEXT,
    
    /**
     * Message with a shared listing link
     */
    LISTING_SHARE,
    
    /**
     * Order status update notification
     */
    ORDER_UPDATE,
    
    /**
     * System-generated message (e.g., conversation started)
     */
    SYSTEM
}
