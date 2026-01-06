package com.example.bookverseserver.enums;

/**
 * User roles in Bookverse platform.
 * Matches Vision specification (vision/API_CONTRACTS.md).
 * 
 * Hierarchy (highest to lowest):
 * ADMIN > MODERATOR > PRO_SELLER > SELLER > USER
 */
public enum RoleName {
    USER,           // Base authenticated user (can buy)
    SELLER,         // Can list and sell books
    PRO_SELLER,     // Paid tier with advanced features
    MODERATOR,      // Content moderation
    ADMIN           // Platform administration
}