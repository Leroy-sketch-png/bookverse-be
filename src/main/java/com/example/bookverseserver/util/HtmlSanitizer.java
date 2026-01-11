package com.example.bookverseserver.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

/**
 * Utility for sanitizing user-generated content to prevent XSS attacks.
 * Uses Jsoup's battle-tested HTML sanitizer.
 */
@Component
public class HtmlSanitizer {

    /**
     * Strict sanitization - removes ALL HTML tags.
     * Use for: usernames, titles, single-line inputs.
     */
    public String sanitizeStrict(String input) {
        if (input == null) return null;
        return Jsoup.clean(input, Safelist.none());
    }

    /**
     * Basic sanitization - allows safe formatting tags only.
     * Allowed: b, i, u, em, strong, br
     * Use for: short descriptions, comments.
     */
    public String sanitizeBasic(String input) {
        if (input == null) return null;
        return Jsoup.clean(input, Safelist.simpleText());
    }

    /**
     * Relaxed sanitization - allows common formatting and structure.
     * Allowed: a[href], b, blockquote, br, cite, code, em, i, li, ol, p, pre, span, strike, strong, sub, sup, u, ul
     * Use for: reviews, long descriptions, rich text.
     */
    public String sanitizeRelaxed(String input) {
        if (input == null) return null;
        // Custom safelist based on basic with some additions
        Safelist safelist = Safelist.relaxed()
                .removeProtocols("a", "href", "ftp", "mailto") // Only allow http/https
                .addEnforcedAttribute("a", "rel", "nofollow noopener") // Prevent SEO spam & tabnabbing
                .addEnforcedAttribute("a", "target", "_blank"); // Open links in new tab
        
        return Jsoup.clean(input, safelist);
    }

    /**
     * Sanitize and also escape special characters for safe storage.
     * Use when content will be rendered as plain text.
     */
    public String sanitizeAndEscape(String input) {
        if (input == null) return null;
        // First remove HTML, then escape any remaining special chars
        String cleaned = sanitizeStrict(input);
        return escapeHtml(cleaned);
    }

    /**
     * Escape HTML entities without removing tags.
     * Use when you want to display raw user input as text.
     */
    public String escapeHtml(String input) {
        if (input == null) return null;
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    /**
     * Check if content contains potentially dangerous HTML/scripts.
     * Use for validation before processing.
     */
    public boolean containsUnsafeContent(String input) {
        if (input == null) return false;
        String cleaned = sanitizeStrict(input);
        // If sanitization changed the content, it contained unsafe HTML
        return !input.equals(cleaned);
    }
}
