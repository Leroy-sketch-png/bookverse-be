package com.example.bookverseserver.utils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Maps messy external source categories (Open Library, Google Books) to our canonical categories.
 * 
 * THE CUNNING APPROACH:
 * Open Library subjects are chaotic: "Fiction, 1813", "English literature", "FICTION / Classics"
 * We need clean categories that match our platform: fiction, non_fiction, science, etc.
 * 
 * Strategy:
 * 1. Normalize input (lowercase, strip punctuation)
 * 2. Pattern match against known keywords for each category
 * 3. Return best matches (up to 3 categories)
 * 4. If no match, return null (let seller choose)
 */
public class CategoryMapper {

    // Our canonical categories (must match frontend bookCategories)
    public static final List<String> CANONICAL_CATEGORIES = List.of(
            "fiction", "non_fiction", "science", "history", 
            "biography", "self_help", "technology", "art", 
            "children", "comics"
    );

    // Category keyword mappings (order matters - more specific first)
    private static final Map<String, List<Pattern>> CATEGORY_PATTERNS = new LinkedHashMap<>();

    static {
        // CHILDREN - check first (to catch "children's fiction" before "fiction")
        CATEGORY_PATTERNS.put("children", compilePatterns(
                "children", "juvenile", "kids", "young readers", "picture book",
                "middle grade", "ya ", "young adult", "teen", "nursery"
        ));

        // COMICS - specific medium
        CATEGORY_PATTERNS.put("comics", compilePatterns(
                "comic", "graphic novel", "manga", "superhero", "marvel", "dc comics",
                "sequential art", "cartoon"
        ));

        // BIOGRAPHY - about real people
        CATEGORY_PATTERNS.put("biography", compilePatterns(
                "biography", "biograph", "autobiography", "memoir", "life of",
                "personal narrative", "true story"
        ));

        // SELF_HELP - personal improvement
        CATEGORY_PATTERNS.put("self_help", compilePatterns(
                "self-help", "self help", "personal development", "motivation",
                "self improvement", "how to", "success", "productivity",
                "mindfulness", "mental health", "psychology", "wellness"
        ));

        // TECHNOLOGY - computers, engineering, etc.
        CATEGORY_PATTERNS.put("technology", compilePatterns(
                "technology", "computer", "programming", "software", "engineering",
                "artificial intelligence", "machine learning", "data science",
                "internet", "digital", "coding", "electronics", "robotics"
        ));

        // SCIENCE - natural sciences
        CATEGORY_PATTERNS.put("science", compilePatterns(
                "science", "physics", "chemistry", "biology", "mathematics",
                "astronomy", "geology", "ecology", "evolution", "genetics",
                "medical", "medicine", "nature", "scientific"
        ));

        // HISTORY - past events
        CATEGORY_PATTERNS.put("history", compilePatterns(
                "history", "historical", "ancient", "medieval", "century",
                "war ", "civil war", "world war", "revolution", "civilization",
                "archaeology", "prehistoric"
        ));

        // ART - creative works
        CATEGORY_PATTERNS.put("art", compilePatterns(
                "art ", " art", "artist", "painting", "sculpture", "photography",
                "design", "architecture", "music", "film", "theater", "theatre",
                "dance", "craft", "creative", "aesthetic"
        ));

        // FICTION - broad category (check after more specific ones)
        CATEGORY_PATTERNS.put("fiction", compilePatterns(
                "fiction", "novel", "stories", "fantasy", "science fiction",
                "mystery", "thriller", "romance", "horror", "adventure",
                "literary fiction", "drama", "suspense", "crime", "detective"
        ));

        // NON_FICTION - everything else factual
        CATEGORY_PATTERNS.put("non_fiction", compilePatterns(
                "non-fiction", "nonfiction", "non fiction", "essays", "reference",
                "journalism", "documentary", "true crime", "travel", "cooking",
                "business", "economics", "politics", "philosophy", "religion",
                "education", "sports", "hobby", "guide"
        ));
    }

    private static List<Pattern> compilePatterns(String... keywords) {
        return Arrays.stream(keywords)
                .map(kw -> Pattern.compile("\\b" + Pattern.quote(kw.toLowerCase()) + "\\b", Pattern.CASE_INSENSITIVE))
                .collect(Collectors.toList());
    }

    /**
     * Maps raw external subjects to our canonical categories.
     * 
     * @param rawSubjects List of raw subject strings from Open Library / Google Books
     * @return List of canonical category names (max 3), or empty list if no match
     */
    public static List<String> mapToCanonical(List<String> rawSubjects) {
        if (rawSubjects == null || rawSubjects.isEmpty()) {
            return Collections.emptyList();
        }

        // Normalize: join all subjects, lowercase, strip years and punctuation noise
        String normalizedInput = rawSubjects.stream()
                .map(CategoryMapper::normalizeSubject)
                .collect(Collectors.joining(" "));

        // Score each category based on keyword matches
        Map<String, Integer> categoryScores = new LinkedHashMap<>();
        for (String category : CANONICAL_CATEGORIES) {
            categoryScores.put(category, 0);
        }

        for (Map.Entry<String, List<Pattern>> entry : CATEGORY_PATTERNS.entrySet()) {
            String category = entry.getKey();
            for (Pattern pattern : entry.getValue()) {
                if (pattern.matcher(normalizedInput).find()) {
                    categoryScores.merge(category, 1, Integer::sum);
                }
            }
        }

        // Return top 3 categories with score > 0
        return categoryScores.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Get the primary (best match) category.
     */
    public static String mapToPrimaryCategory(List<String> rawSubjects) {
        List<String> mapped = mapToCanonical(rawSubjects);
        return mapped.isEmpty() ? null : mapped.get(0);
    }

    /**
     * Normalizes a raw subject string.
     * - Lowercase
     * - Remove years (e.g., "Fiction, 1813" → "fiction")
     * - Remove common noise words
     */
    private static String normalizeSubject(String subject) {
        if (subject == null) return "";
        
        return subject.toLowerCase()
                // Remove years like ", 1813" or "(1920s)"
                .replaceAll(",?\\s*\\d{4}s?", "")
                .replaceAll("\\(\\d{4}s?\\)", "")
                // Remove "in literature", "in fiction" noise
                .replaceAll("in (literature|fiction|art)", "")
                // Remove leading/trailing punctuation
                .replaceAll("^[\\s,./]+|[\\s,./]+$", "")
                .trim();
    }

    /**
     * Checks if any canonical category was found.
     */
    public static boolean hasMatch(List<String> rawSubjects) {
        return !mapToCanonical(rawSubjects).isEmpty();
    }
}
