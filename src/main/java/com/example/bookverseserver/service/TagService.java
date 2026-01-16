package com.example.bookverseserver.service;

import com.example.bookverseserver.entity.Product.BookTag;
import com.example.bookverseserver.repository.BookTagRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Intelligent tag extraction from Open Library subjects.
 * 
 * Unlike CategoryService which maps to 6 broad buckets,
 * TagService extracts GRANULAR genre tags for discovery:
 * - "romance", "historical", "regency", "mystery", "coming of age"
 * 
 * The goal: Enable powerful filtering like "Romance + Historical + Regency"
 */
@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)  // Default read-only, override for write methods
public class TagService {
    
    BookTagRepository bookTagRepository;
    
    /**
     * High-value genre tags we want to extract from Open Library subjects.
     * These are the tags that users actually search for.
     * 
     * Format: keyword -> display name
     */
    private static final Map<String, String> GENRE_MAPPINGS = Map.ofEntries(
        // Fiction Genres
        Map.entry("romance", "Romance"),
        Map.entry("love stories", "Romance"),
        Map.entry("historical fiction", "Historical Fiction"),
        Map.entry("historical", "Historical"),
        Map.entry("mystery", "Mystery"),
        Map.entry("thriller", "Thriller"),
        Map.entry("horror", "Horror"),
        Map.entry("fantasy", "Fantasy"),
        Map.entry("science fiction", "Science Fiction"),
        Map.entry("literary fiction", "Literary Fiction"),
        Map.entry("fiction classics", "Classics"),
        Map.entry("classical literature", "Classics"),
        Map.entry("domestic fiction", "Domestic Fiction"),
        Map.entry("adventure", "Adventure"),
        Map.entry("coming of age", "Coming of Age"),
        Map.entry("regency", "Regency"),
        Map.entry("gothic", "Gothic"),
        Map.entry("magical realism", "Magical Realism"),
        Map.entry("dystopian", "Dystopian"),
        Map.entry("crime", "Crime"),
        Map.entry("detective", "Detective"),
        Map.entry("suspense", "Suspense"),
        Map.entry("psychological", "Psychological"),
        Map.entry("war", "War"),
        Map.entry("satire", "Satire"),
        Map.entry("humor", "Humor"),
        Map.entry("comedy", "Comedy"),
        
        // Non-Fiction Genres  
        Map.entry("biography", "Biography"),
        Map.entry("memoir", "Memoir"),
        Map.entry("autobiography", "Autobiography"),
        Map.entry("history", "History"),
        Map.entry("philosophy", "Philosophy"),
        Map.entry("politics", "Politics"),
        Map.entry("psychology", "Psychology"),
        Map.entry("self-help", "Self-Help"),
        Map.entry("personal development", "Personal Development"),
        Map.entry("business", "Business"),
        Map.entry("economics", "Economics"),
        Map.entry("science", "Science"),
        Map.entry("technology", "Technology"),
        Map.entry("nature", "Nature"),
        Map.entry("travel", "Travel"),
        Map.entry("cooking", "Cooking"),
        Map.entry("art", "Art"),
        Map.entry("music", "Music"),
        Map.entry("religion", "Religion"),
        Map.entry("spirituality", "Spirituality"),
        Map.entry("true crime", "True Crime"),
        Map.entry("essays", "Essays"),
        
        // Themes & Descriptors
        Map.entry("family", "Family"),
        Map.entry("courtship", "Courtship"),
        Map.entry("marriage", "Marriage"),
        Map.entry("social class", "Social Class"),
        Map.entry("manners", "Manners & Customs"),
        Map.entry("young women", "Young Women"),
        Map.entry("sisters", "Sisters"),
        Map.entry("friendship", "Friendship"),
        Map.entry("love", "Love"),
        Map.entry("death", "Death"),
        Map.entry("identity", "Identity"),
        Map.entry("childhood", "Childhood")
    );
    
    /**
     * Subjects to EXCLUDE (noise, not useful for discovery)
     */
    private static final Set<String> EXCLUDED_PATTERNS = Set.of(
        "fiction",  // Too generic (we have Fiction as a category)
        "novel",    // Format, not genre
        "large type books",
        "reading level",
        "textbooks",
        "english language",
        "language and languages",
        "readers",
        "adaptations",
        "coloring books",
        "emoticons",
        "drama (dramatic works",
        "british and irish fiction"
    );

    /**
     * Extract meaningful tags from a list of Open Library subject names.
     * 
     * @param rawSubjects Raw subject strings from Open Library
     * @return Set of BookTag entities (persisted or existing)
     */
    @Transactional
    public Set<BookTag> extractTags(List<String> rawSubjects) {
        if (rawSubjects == null || rawSubjects.isEmpty()) {
            return new HashSet<>();
        }
        
        Set<String> matchedTagNames = new LinkedHashSet<>(); // Preserve order
        
        for (String rawSubject : rawSubjects) {
            if (rawSubject == null) continue;
            
            String normalized = rawSubject.toLowerCase().trim();
            
            // Skip excluded patterns
            if (isExcluded(normalized)) {
                continue;
            }
            
            // Try to match against our genre mappings
            for (Map.Entry<String, String> entry : GENRE_MAPPINGS.entrySet()) {
                if (normalized.contains(entry.getKey())) {
                    matchedTagNames.add(entry.getValue());
                }
            }
        }
        
        // Limit to 10 most relevant tags per book
        List<String> finalTags = matchedTagNames.stream()
                .limit(10)
                .collect(Collectors.toList());
        
        // Get or create each tag
        Set<BookTag> tags = new HashSet<>();
        for (String tagName : finalTags) {
            BookTag tag = getOrCreateTag(tagName);
            tags.add(tag);
        }
        
        log.info("Extracted {} tags from {} raw subjects: {}", 
                tags.size(), rawSubjects.size(), 
                tags.stream().map(BookTag::getName).collect(Collectors.joining(", ")));
        
        return tags;
    }
    
    /**
     * Get an existing tag or create a new one.
     */
    @Transactional
    public BookTag getOrCreateTag(String tagName) {
        String slug = toSlug(tagName);
        
        return bookTagRepository.findBySlug(slug)
                .orElseGet(() -> {
                    BookTag newTag = BookTag.builder()
                            .name(tagName)
                            .slug(slug)
                            .usageCount(0)
                            .build();
                    
                    try {
                        return bookTagRepository.save(newTag);
                    } catch (DataIntegrityViolationException e) {
                        // Race condition - another thread created it
                        return bookTagRepository.findBySlug(slug).orElseThrow();
                    }
                });
    }
    
    /**
     * Get popular tags for discovery/browse pages.
     */
    public List<BookTag> getPopularTags() {
        return bookTagRepository.findTop20ByOrderByUsageCountDesc();
    }
    
    private boolean isExcluded(String normalized) {
        for (String pattern : EXCLUDED_PATTERNS) {
            if (normalized.contains(pattern)) {
                return true;
            }
        }
        // Exclude non-English subjects (contain non-ASCII letters that aren't accents)
        if (containsNonLatinScript(normalized)) {
            return true;
        }
        return false;
    }
    
    private boolean containsNonLatinScript(String text) {
        // Allow Latin letters (including accented), spaces, hyphens, apostrophes
        // Reject Cyrillic, Chinese, Arabic, etc.
        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
                if (block != Character.UnicodeBlock.BASIC_LATIN && 
                    block != Character.UnicodeBlock.LATIN_1_SUPPLEMENT &&
                    block != Character.UnicodeBlock.LATIN_EXTENDED_A &&
                    block != Character.UnicodeBlock.LATIN_EXTENDED_B) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private String toSlug(String input) {
        if (input == null) return null;
        String nowhitespace = input.trim().toLowerCase().replaceAll("\\s+", "-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("[^\\w-]");
        return pattern.matcher(normalized).replaceAll("");
    }
}
