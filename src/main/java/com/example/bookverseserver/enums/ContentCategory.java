package com.example.bookverseserver.enums;

/**
 * Category of content issue detected
 */
public enum ContentCategory {
    CLEAN,      // No issues detected
    TOXIC,      // Hate speech, harassment, violence
    SPAM,       // Promotional content, links
    OFF_TOPIC   // Unrelated to books/reviews
}
