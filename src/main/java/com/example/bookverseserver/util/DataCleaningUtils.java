package com.example.bookverseserver.util;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * STARK INDUSTRIES DATA CLEANING UTILITY
 * 
 * Competitive Edge Philosophy:
 * "THEIR OUTPUT IS OUR INPUT, NOT OUR OUTPUT.
 * We are the CURATOR, not the courier.
 * Every piece of data must be cleaned, deduplicated, and meaningful."
 * 
 * This utility ensures:
 * 1. English-only (Latin script) - reject Cyrillic, Arabic, Chinese, etc.
 * 2. Deduplicated - normalize similar entries
 * 3. Meaningful - filter out metadata noise
 */
public final class DataCleaningUtils {
    
    private DataCleaningUtils() {
        // Utility class
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // LATIN SCRIPT VALIDATION
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Check if text contains non-Latin script (Cyrillic, Chinese, Arabic, etc.)
     * Allows Latin letters including accented characters (French, Spanish accents OK).
     * 
     * @param text Input text to check
     * @return true if contains non-Latin script that should be rejected
     */
    public static boolean containsNonLatinScript(String text) {
        if (text == null || text.isEmpty()) return false;
        
        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
                if (block != Character.UnicodeBlock.BASIC_LATIN && 
                    block != Character.UnicodeBlock.LATIN_1_SUPPLEMENT &&
                    block != Character.UnicodeBlock.LATIN_EXTENDED_A &&
                    block != Character.UnicodeBlock.LATIN_EXTENDED_B &&
                    block != Character.UnicodeBlock.LATIN_EXTENDED_ADDITIONAL) {
                    return true; // Found Cyrillic, Chinese, Arabic, etc.
                }
            }
        }
        return false;
    }
    
    /**
     * Check if text is primarily Latin script (allows some non-Latin if mostly Latin).
     * More lenient than containsNonLatinScript for edge cases like "São Paulo".
     */
    public static boolean isPrimarilyLatinScript(String text) {
        if (text == null || text.isEmpty()) return false;
        
        int latinCount = 0;
        int nonLatinCount = 0;
        
        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
                if (block == Character.UnicodeBlock.BASIC_LATIN || 
                    block == Character.UnicodeBlock.LATIN_1_SUPPLEMENT ||
                    block == Character.UnicodeBlock.LATIN_EXTENDED_A ||
                    block == Character.UnicodeBlock.LATIN_EXTENDED_B ||
                    block == Character.UnicodeBlock.LATIN_EXTENDED_ADDITIONAL) {
                    latinCount++;
                } else {
                    nonLatinCount++;
                }
            }
        }
        
        // Require at least 80% Latin script
        int total = latinCount + nonLatinCount;
        return total > 0 && (latinCount * 100 / total) >= 80;
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // DEDUPLICATION & NORMALIZATION
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Deduplicate a list of strings, keeping the shortest/cleanest variant.
     * 
     * Example input: ["Russia", "Russia (Federation)", "Saint Petersburg", "Saint Petersburg (Russia)"]
     * Example output: ["Russia", "Saint Petersburg"]
     * 
     * @param items List of strings to deduplicate
     * @return Deduplicated list
     */
    public static List<String> deduplicateByBase(List<String> items) {
        if (items == null || items.isEmpty()) return Collections.emptyList();
        
        // Group by base name (text before parentheses or the whole string)
        Map<String, String> baseToShortest = new LinkedHashMap<>();
        
        for (String item : items) {
            if (item == null) continue;
            
            String base = extractBaseName(item).toLowerCase().trim();
            String existing = baseToShortest.get(base);
            
            // Keep the SHORTER version (usually cleaner)
            if (existing == null || item.length() < existing.length()) {
                baseToShortest.put(base, item.trim());
            }
        }
        
        return new ArrayList<>(baseToShortest.values());
    }
    
    /**
     * Extract base name from strings like "Saint Petersburg (Russia)" → "Saint Petersburg"
     */
    public static String extractBaseName(String text) {
        if (text == null) return "";
        
        int parenIndex = text.indexOf('(');
        if (parenIndex > 0) {
            return text.substring(0, parenIndex).trim();
        }
        
        // Also handle comma-separated qualifiers: "Saint Petersburg, Russia"
        int commaIndex = text.indexOf(',');
        if (commaIndex > 0) {
            return text.substring(0, commaIndex).trim();
        }
        
        return text.trim();
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // SUBJECT CLEANING (Places, People, Times)
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Clean a list of subject entries (places, people, times).
     * Applies: Latin-only filter, deduplication, limit.
     * 
     * @param rawSubjects Raw subject strings from Open Library
     * @param limit Maximum number of entries to keep
     * @return Cleaned, deduplicated, Latin-only list
     */
    public static List<String> cleanSubjectList(List<String> rawSubjects, int limit) {
        if (rawSubjects == null || rawSubjects.isEmpty()) return Collections.emptyList();
        
        return rawSubjects.stream()
                // Filter out non-Latin script (Spanish "San Petersburgo" with accents is OK)
                .filter(s -> s != null && !containsNonLatinScript(s))
                // Trim whitespace
                .map(String::trim)
                // Filter empty
                .filter(s -> !s.isEmpty())
                // Collect for deduplication
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> deduplicateByBase(list).stream()
                                .limit(limit)
                                .collect(Collectors.toList())
                ));
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // AUTHOR NAME CLEANING
    // ═══════════════════════════════════════════════════════════════════════════
    
    /**
     * Clean author name, attempting to convert Cyrillic to English equivalent
     * or extract from by_statement if available.
     * 
     * @param rawName Author name from Open Library (might be Cyrillic)
     * @param byStatement The "by_statement" field (often has English name)
     * @return Cleaned author name in Latin script, or null if unsalvageable
     */
    public static String cleanAuthorName(String rawName, String byStatement) {
        // If raw name is already Latin, use it directly
        if (rawName != null && !containsNonLatinScript(rawName)) {
            return AuthorNameNormalizer.normalize(rawName);
        }
        
        // Try to extract English name from by_statement
        // Format: "Leo Tolstoy ; translated by ..." or "by Fyodor Dostoyevsky"
        if (byStatement != null && !byStatement.isEmpty()) {
            String extracted = extractAuthorFromByStatement(byStatement);
            if (extracted != null && !containsNonLatinScript(extracted)) {
                return AuthorNameNormalizer.normalize(extracted);
            }
        }
        
        // Try well-known transliterations
        String transliterated = tryKnownTransliteration(rawName);
        if (transliterated != null) {
            return transliterated;
        }
        
        // Last resort: return null (author will need manual fix or be skipped)
        return null;
    }
    
    /**
     * Extract author name from by_statement field.
     * Examples:
     * - "Fyodor Dostoyevsky ; translated by..." → "Fyodor Dostoyevsky"
     * - "by Leo Tolstoy" → "Leo Tolstoy"
     * - "Leo Tolstoy ; introduction by..." → "Leo Tolstoy"
     */
    private static String extractAuthorFromByStatement(String byStatement) {
        if (byStatement == null) return null;
        
        String cleaned = byStatement.trim();
        
        // Remove leading "by " if present
        if (cleaned.toLowerCase().startsWith("by ")) {
            cleaned = cleaned.substring(3).trim();
        }
        
        // Split on common separators
        String[] separators = {" ; ", " translated", " introduction", " edited", " with ", " and "};
        for (String sep : separators) {
            int idx = cleaned.toLowerCase().indexOf(sep.toLowerCase());
            if (idx > 0) {
                cleaned = cleaned.substring(0, idx).trim();
            }
        }
        
        // Validate result is reasonable (2+ words, not too long)
        if (cleaned.length() > 3 && cleaned.length() < 100 && cleaned.contains(" ")) {
            return cleaned;
        }
        
        return null;
    }
    
    /**
     * Map well-known Cyrillic author names to their English equivalents.
     * This is a curated list of famous authors whose names we KNOW.
     */
    private static final Map<String, String> KNOWN_TRANSLITERATIONS = Map.ofEntries(
        // Russian Greats
        Map.entry("лев толстой", "Leo Tolstoy"),
        Map.entry("лев николаевич толстой", "Leo Tolstoy"),
        Map.entry("фёдор достоевский", "Fyodor Dostoevsky"),
        Map.entry("фёдор михайлович достоевский", "Fyodor Dostoevsky"),
        Map.entry("фёдор достоевский", "Fyodor Dostoevsky"),
        Map.entry("антон чехов", "Anton Chekhov"),
        Map.entry("антон павлович чехов", "Anton Chekhov"),
        Map.entry("александр пушкин", "Alexander Pushkin"),
        Map.entry("александр сергеевич пушкин", "Alexander Pushkin"),
        Map.entry("николай гоголь", "Nikolai Gogol"),
        Map.entry("иван тургенев", "Ivan Turgenev"),
        Map.entry("борис пастернак", "Boris Pasternak"),
        Map.entry("михаил булгаков", "Mikhail Bulgakov"),
        Map.entry("владимир набоков", "Vladimir Nabokov"),
        Map.entry("максим горький", "Maxim Gorky"),
        
        // Add more as discovered
        Map.entry("марина цветаева", "Marina Tsvetaeva"),
        Map.entry("анна ахматова", "Anna Akhmatova")
    );
    
    private static String tryKnownTransliteration(String cyrillicName) {
        if (cyrillicName == null) return null;
        
        String normalized = cyrillicName.toLowerCase().trim();
        return KNOWN_TRANSLITERATIONS.get(normalized);
    }
    
    // ═══════════════════════════════════════════════════════════════════════════
    // SLUG GENERATION
    // ═══════════════════════════════════════════════════════════════════════════
    
    private static final Pattern SLUG_CLEAN = Pattern.compile("[^\\w-]");
    private static final Pattern MULTIPLE_DASHES = Pattern.compile("-+");
    
    /**
     * Convert text to URL-safe slug.
     * "Crime & Punishment" → "crime-punishment"
     */
    public static String toSlug(String input) {
        if (input == null) return null;
        
        String normalized = Normalizer.normalize(input.trim().toLowerCase(), Normalizer.Form.NFD);
        String noAccents = normalized.replaceAll("\\p{M}", ""); // Remove diacritics
        String spaceToDash = noAccents.replaceAll("\\s+", "-");
        String cleaned = SLUG_CLEAN.matcher(spaceToDash).replaceAll("");
        return MULTIPLE_DASHES.matcher(cleaned).replaceAll("-");
    }
}
