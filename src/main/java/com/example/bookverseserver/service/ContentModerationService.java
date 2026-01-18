package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.ModerationRequest;
import com.example.bookverseserver.dto.response.ModerationResponse;
import com.example.bookverseserver.enums.ContentCategory;
import com.example.bookverseserver.enums.ContentModerationDecision;
import com.example.bookverseserver.enums.ContentSeverity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Content Moderation Service
 * 
 * Hybrid approach stolen from chefkix-ai-service, adapted for Bookverse:
 * - Fast rule-based checks first (blocked terms, patterns)
 * - AI fallback for uncertain cases (TODO: wire to AIService)
 * 
 * Thresholds:
 * - 0-24: APPROVE (clean)
 * - 25-74: FLAG (needs review)
 * - 75-100: BLOCK (violates policy)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContentModerationService {
    
    private static final int THRESHOLD_APPROVE = 25;
    private static final int THRESHOLD_BLOCK = 75;
    
    private final ObjectMapper objectMapper;
    
    // Loaded from blocked_terms.json
    private List<String> criticalTerms = new ArrayList<>();
    private List<String> highTerms = new ArrayList<>();
    private List<String> mediumTerms = new ArrayList<>();
    private List<String> bookWhitelist = new ArrayList<>();
    private List<String> bookTitlesWhitelist = new ArrayList<>();
    private List<Pattern> trollPatterns = new ArrayList<>();
    private List<Pattern> spamPatterns = new ArrayList<>();
    private List<String> offTopicKeywords = new ArrayList<>();
    
    // Extended leet speak normalization map (HashMap allows >10 entries)
    private static final Map<Character, Character> LEET_MAP;
    static {
        LEET_MAP = new HashMap<>();
        // Basic leet
        LEET_MAP.put('4', 'a'); LEET_MAP.put('@', 'a');
        LEET_MAP.put('3', 'e'); LEET_MAP.put('€', 'e');
        LEET_MAP.put('1', 'i'); LEET_MAP.put('!', 'i'); LEET_MAP.put('|', 'i');
        LEET_MAP.put('0', 'o');
        LEET_MAP.put('5', 's'); LEET_MAP.put('$', 's');
        LEET_MAP.put('7', 't'); LEET_MAP.put('+', 't');
        LEET_MAP.put('8', 'b');
        LEET_MAP.put('6', 'g'); LEET_MAP.put('9', 'g');
        LEET_MAP.put('2', 'z');
    }
    
    // Cyrillic homoglyph map (lookalike characters used to bypass filters)
    // Using Unicode escapes for reliability across different file encodings
    private static final Map<Character, Character> HOMOGLYPH_MAP;
    static {
        HOMOGLYPH_MAP = new HashMap<>();
        // Cyrillic lookalikes → Latin (using Unicode escapes for reliability)
        HOMOGLYPH_MAP.put('\u0430', 'a'); HOMOGLYPH_MAP.put('\u0410', 'a'); // Cyrillic а/А → a
        HOMOGLYPH_MAP.put('\u0435', 'e'); HOMOGLYPH_MAP.put('\u0415', 'e'); // Cyrillic е/Е → e
        HOMOGLYPH_MAP.put('\u0456', 'i'); HOMOGLYPH_MAP.put('\u0406', 'i'); // Cyrillic і/І → i
        HOMOGLYPH_MAP.put('\u043e', 'o'); HOMOGLYPH_MAP.put('\u041e', 'o'); // Cyrillic о/О → o
        HOMOGLYPH_MAP.put('\u0440', 'p'); HOMOGLYPH_MAP.put('\u0420', 'p'); // Cyrillic р/Р → p
        HOMOGLYPH_MAP.put('\u0441', 'c'); HOMOGLYPH_MAP.put('\u0421', 'c'); // Cyrillic с/С → c
        HOMOGLYPH_MAP.put('\u0443', 'y'); HOMOGLYPH_MAP.put('\u0423', 'y'); // Cyrillic у/У → y
        HOMOGLYPH_MAP.put('\u0445', 'x'); HOMOGLYPH_MAP.put('\u0425', 'x'); // Cyrillic х/Х → x
        HOMOGLYPH_MAP.put('\u0412', 'b'); // Cyrillic В → b
        HOMOGLYPH_MAP.put('\u041d', 'h'); // Cyrillic Н → h
        HOMOGLYPH_MAP.put('\u041a', 'k'); // Cyrillic К → k
        HOMOGLYPH_MAP.put('\u041c', 'm'); // Cyrillic М → m
        HOMOGLYPH_MAP.put('\u0422', 't'); // Cyrillic Т → t
        // Greek lookalikes
        HOMOGLYPH_MAP.put('\u03b1', 'a'); // Greek α → a
        HOMOGLYPH_MAP.put('\u03b5', 'e'); // Greek ε → e  
        HOMOGLYPH_MAP.put('\u03bf', 'o'); // Greek ο → o
    }
    
    // FAIL-CLOSED: Hardcoded fallback critical terms if JSON fails to load
    // These are the absolute minimum that MUST always be blocked
    private static final List<String> FALLBACK_CRITICAL_TERMS = List.of(
        "kill yourself", "kill urself", "commit suicide",
        "child porn", "child exploitation",
        "i will kill you", "gonna kill you"
    );
    
    // Banter indicators (reduce severity for friendly jokes)
    private static final Set<String> BANTER_INDICATORS = Set.of(
        "lol", "lmao", "haha", "hehe", "jk", "just kidding", "kidding",
        "joking", "just joking", "no offense", "just saying", "😂", "🤣",
        "😅", "😆", "😄", "😁", "🙃", "😉", "😜", "😝", "🤪", "❤️", "💀",
        "💯", "👍", "🔥", "😎"
    );
    
    @PostConstruct
    public void loadBlockedTerms() {
        try {
            ClassPathResource resource = new ClassPathResource("moderation/blocked_terms.json");
            InputStream inputStream = resource.getInputStream();
            Map<String, List<String>> terms = objectMapper.readValue(
                inputStream, new TypeReference<>() {}
            );
            
            criticalTerms = terms.getOrDefault("critical", List.of());
            highTerms = terms.getOrDefault("high", List.of());
            mediumTerms = terms.getOrDefault("medium", List.of());
            bookWhitelist = terms.getOrDefault("book_whitelist", List.of());
            bookTitlesWhitelist = terms.getOrDefault("book_titles_whitelist", List.of());
            offTopicKeywords = terms.getOrDefault("off_topic_keywords", List.of());
            
            // Compile regex patterns
            trollPatterns = terms.getOrDefault("troll_patterns", List.of()).stream()
                .map(p -> Pattern.compile(p, Pattern.CASE_INSENSITIVE))
                .collect(Collectors.toList());
            
            spamPatterns = terms.getOrDefault("spam_patterns", List.of()).stream()
                .map(p -> Pattern.compile(p, Pattern.CASE_INSENSITIVE))
                .collect(Collectors.toList());
            
            log.info("✅ Loaded blocked terms: {} critical, {} high, {} medium, {} phrase whitelist, {} title whitelist",
                criticalTerms.size(), highTerms.size(), mediumTerms.size(), bookWhitelist.size(), bookTitlesWhitelist.size());
                
        } catch (IOException e) {
            log.error("❌ Failed to load blocked_terms.json: {} — USING FALLBACK CRITICAL TERMS", e.getMessage());
            // FAIL-CLOSED: Use hardcoded fallback for critical terms, NEVER allow moderation to be a no-op
            criticalTerms = new ArrayList<>(FALLBACK_CRITICAL_TERMS);
            log.warn("⚠️ Moderation running with {} fallback critical terms only", criticalTerms.size());
        }
    }
    
    /**
     * Main moderation entry point — hybrid rule + AI approach
     */
    public ModerationResponse moderate(ModerationRequest request) {
        long startTime = System.currentTimeMillis();
        
        RuleResult ruleResult = checkRules(request.getText(), request.getUserReputation());
        
        // Calculate reputation-adjusted threshold
        double reputationFactor = (request.getUserReputation() - 50) / 100.0;
        int adjustedApproveThreshold = (int) (THRESHOLD_APPROVE + (reputationFactor * 10));
        
        ContentModerationDecision decision;
        String reason;
        boolean aiUsed = false;
        
        if (ruleResult.shouldBlock) {
            decision = ContentModerationDecision.BLOCK;
            reason = "Content violates community guidelines";
        } else if (ruleResult.score >= THRESHOLD_BLOCK) {
            decision = ContentModerationDecision.BLOCK;
            reason = "Multiple policy violations detected";
        } else if (ruleResult.score <= adjustedApproveThreshold) {
            decision = ContentModerationDecision.APPROVE;
            reason = "Content appears clean";
        } else {
            // Uncertain zone (25-74): would call AI here
            // For now, use FLAG as conservative default
            decision = ContentModerationDecision.FLAG;
            reason = "Content flagged for human review";
            // TODO: Wire to AIService for uncertain cases
            // aiUsed = true;
        }
        
        long processingTime = System.currentTimeMillis() - startTime;
        
        return ModerationResponse.builder()
            .decision(decision)
            .category(ruleResult.category)
            .severity(ruleResult.severity)
            .score(ruleResult.score)
            .matchedTerms(ruleResult.matchedTerms.isEmpty() ? null : ruleResult.matchedTerms)
            .reason(reason)
            .aiUsed(aiUsed)
            .processingTimeMs(processingTime)
            .build();
    }
    
    /**
     * Quick check for content that should be blocked immediately
     * Used for inline validation during create/update
     */
    public boolean shouldBlock(String text) {
        RuleResult result = checkRules(text, 50);
        return result.shouldBlock || result.score >= THRESHOLD_BLOCK;
    }
    
    /**
     * Quick check for content that needs moderation review
     */
    public boolean needsReview(String text) {
        RuleResult result = checkRules(text, 50);
        return result.score >= THRESHOLD_APPROVE && result.score < THRESHOLD_BLOCK;
    }
    
    // ========================================================================
    // RULE-BASED MODERATION (Stolen from chefkix, adapted for Java)
    // ========================================================================
    
    private RuleResult checkRules(String content, int userReputation) {
        String normalizedText = normalizeText(content);
        int score = 0;
        List<String> matchedTerms = new ArrayList<>();
        ContentCategory category = ContentCategory.CLEAN;
        ContentSeverity severity = ContentSeverity.LOW;
        
        // 1. Check critical violations first — immediate block
        for (String term : criticalTerms) {
            if (approxMatch(normalizedText, term)) {
                matchedTerms.add(term);
                return new RuleResult(100, ContentCategory.TOXIC, ContentSeverity.CRITICAL, 
                    matchedTerms, true);
            }
        }
        
        // 2. Check high severity terms
        for (String term : highTerms) {
            if (approxMatch(normalizedText, term)) {
                // Check book-context whitelist
                if (!isBookContext(content)) {
                    matchedTerms.add(term);
                    
                    if (isSelfDeprecating(content) && isFriendlyBanter(content)) {
                        continue; // Skip self-deprecating jokes
                    }
                    
                    if (isFriendlyBanter(content)) {
                        score += 15; // Reduced for banter
                    } else if (isTargetedAttack(content)) {
                        score += 80; // Targeted attack = block
                    } else {
                        score += 40; // High term alone = flag
                    }
                    category = ContentCategory.TOXIC;
                    severity = ContentSeverity.HIGH;
                }
            }
        }
        
        // 3. Check medium severity terms
        for (String term : mediumTerms) {
            if (approxMatch(normalizedText, term)) {
                if (isFriendlyBanter(content)) {
                    continue; // Skip casual swearing in banter
                }
                matchedTerms.add(term);
                
                if (isTargetedAttack(content)) {
                    score += 40;
                } else {
                    score += 25; // Medium term = flag threshold
                }
                
                if (category == ContentCategory.CLEAN) {
                    category = ContentCategory.TOXIC;
                    severity = ContentSeverity.MEDIUM;
                }
            }
        }
        
        // 4. Check troll patterns
        for (Pattern pattern : trollPatterns) {
            if (pattern.matcher(content).find()) {
                matchedTerms.add("troll_pattern");
                score += 30;
                if (category == ContentCategory.CLEAN) {
                    category = ContentCategory.TOXIC;
                    severity = ContentSeverity.MEDIUM;
                }
            }
        }
        
        // 5. Check spam patterns
        for (Pattern pattern : spamPatterns) {
            if (pattern.matcher(content).find()) {
                matchedTerms.add("spam_pattern");
                score += 35;
                if (category == ContentCategory.CLEAN) {
                    category = ContentCategory.SPAM;
                    severity = ContentSeverity.MEDIUM;
                }
            }
        }
        
        // 6. Check for excessive URLs (spam indicator)
        long urlCount = Pattern.compile("https?://", Pattern.CASE_INSENSITIVE)
            .matcher(content).results().count();
        if (urlCount > 2) {
            score += (int) (urlCount * 10);
            if (category == ContentCategory.CLEAN) {
                category = ContentCategory.SPAM;
                severity = ContentSeverity.LOW;
            }
        }
        
        // 7. Check off-topic keywords
        for (String keyword : offTopicKeywords) {
            if (normalizedText.contains(keyword.toLowerCase())) {
                score += 10;
                if (category == ContentCategory.CLEAN) {
                    category = ContentCategory.OFF_TOPIC;
                    severity = ContentSeverity.LOW;
                }
            }
        }
        
        // 8. Check for excessive caps (shouting)
        long capsRatio = content.chars().filter(Character::isUpperCase).count() * 100 / 
            Math.max(content.length(), 1);
        if (capsRatio > 50 && content.length() > 20) {
            score += 15;
        }
        
        // 9. Check for repeated characters (spam indicator)
        if (Pattern.compile("(.)\\1{4,}").matcher(content).find()) {
            score += 10;
        }
        
        // Cap score at 100
        score = Math.min(score, 100);
        boolean shouldBlock = score >= THRESHOLD_BLOCK;
        
        return new RuleResult(score, category, severity, matchedTerms, shouldBlock);
    }
    
    // ========================================================================
    // TEXT NORMALIZATION (Stolen from chefkix, enhanced with Unicode security)
    // ========================================================================
    
    private String normalizeText(String text) {
        if (text == null) return "";
        
        // Step 1: Unicode normalization (NFKD decomposes characters)
        // This handles accented characters and some homoglyphs
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFKD);
        
        // Step 2: Strip zero-width characters and control characters
        // These are used to bypass filters: "nig\u200Bger" (with zero-width space)
        normalized = normalized.replaceAll("[\\u200B-\\u200D\\uFEFF\\u00AD\\u034F\\u2060\\u2061-\\u2064]", "");
        
        // Step 3: Apply Cyrillic/Greek homoglyph normalization BEFORE lowercasing
        StringBuilder homoglyphNormalized = new StringBuilder();
        for (char c : normalized.toCharArray()) {
            homoglyphNormalized.append(HOMOGLYPH_MAP.getOrDefault(c, c));
        }
        normalized = homoglyphNormalized.toString().toLowerCase();
        
        // Step 4: Leet speak normalization
        StringBuilder sb = new StringBuilder();
        for (char c : normalized.toCharArray()) {
            sb.append(LEET_MAP.getOrDefault(c, c));
        }
        normalized = sb.toString();
        
        // Step 5: Collapse spaced single letters (k i l l -> kill)
        normalized = normalized.replaceAll("\\b([a-z])\\s+(?=[a-z]\\b)", "$1");
        
        // Step 6: Collapse repeated characters (more than 2)
        normalized = normalized.replaceAll("(.)\\1{2,}", "$1$1");
        
        // Step 7: Remove special characters except spaces
        normalized = normalized.replaceAll("[^a-z0-9\\s]", " ");
        
        // Step 8: Collapse multiple spaces
        normalized = normalized.replaceAll("\\s+", " ").trim();
        
        return normalized;
    }
    
    private boolean approxMatch(String normalizedText, String keyword) {
        String normalizedKeyword = normalizeText(keyword);
        
        // Exact match
        if (normalizedText.contains(normalizedKeyword)) {
            return true;
        }
        
        // Fuzzy match for obfuscation attempts
        String[] words = normalizedText.split("\\s+");
        for (String word : words) {
            if (similarityRatio(word, normalizedKeyword) >= 0.85) {
                return true;
            }
        }
        
        return false;
    }
    
    private double similarityRatio(String s1, String s2) {
        if (s1.equals(s2)) return 1.0;
        if (s1.isEmpty() || s2.isEmpty()) return 0.0;
        
        int maxLen = Math.max(s1.length(), s2.length());
        int distance = levenshteinDistance(s1, s2);
        return 1.0 - ((double) distance / maxLen);
    }
    
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= s2.length(); j++) dp[0][j] = j;
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                    Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                );
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
    
    // ========================================================================
    // CONTEXT DETECTION (Stolen from chefkix, adapted for books)
    // ========================================================================
    
    /**
     * Checks if content is in a book-related context (phrases or titles).
     * Prevents false positives on "To Kill a Mockingbird" reviews.
     */
    private boolean isBookContext(String text) {
        String textLower = text.toLowerCase();
        
        // Check phrase whitelist (e.g., "killer story", "bloody brilliant")
        boolean hasPhrase = bookWhitelist.stream()
            .anyMatch(term -> textLower.contains(term.toLowerCase()));
        
        // Check book titles whitelist (e.g., "To Kill a Mockingbird", "American Psycho")
        boolean hasTitle = bookTitlesWhitelist.stream()
            .anyMatch(title -> textLower.contains(title.toLowerCase()));
        
        return hasPhrase || hasTitle;
    }
    
    private boolean isFriendlyBanter(String text) {
        String textLower = text.toLowerCase();
        return BANTER_INDICATORS.stream().anyMatch(textLower::contains);
    }
    
    private boolean isSelfDeprecating(String text) {
        String textLower = text.toLowerCase();
        boolean hasSelfReference = Pattern.compile("\\b(i\\b|i'm\\b|i am\\b|me\\b|myself\\b)")
            .matcher(textLower).find();
        boolean hasInsult = Pattern.compile("\\b(idiot|stupid|dumb|moron|fool)\\b")
            .matcher(textLower).find();
        return hasSelfReference && hasInsult;
    }
    
    private boolean isTargetedAttack(String text) {
        String textLower = text.toLowerCase();
        List<Pattern> attackPatterns = List.of(
            Pattern.compile("\\byou\\s+are\\s+(a\\s+|an\\s+)?(stupid|idiot|moron|dumb|fool|loser)"),
            Pattern.compile("\\byou'?re\\s+(a\\s+|an\\s+)?(stupid|idiot|moron|dumb|fool|loser)"),
            Pattern.compile("\\bgo\\s+(die|kill yourself|f+u+c+k+ yourself)"),
            Pattern.compile("\\bkill yourself\\b"),
            Pattern.compile("\\bf+u+c+k+\\s+you\\b")
        );
        
        return attackPatterns.stream().anyMatch(p -> p.matcher(textLower).find());
    }
    
    // ========================================================================
    // INTERNAL TYPES
    // ========================================================================
    
    private record RuleResult(
        int score,
        ContentCategory category,
        ContentSeverity severity,
        List<String> matchedTerms,
        boolean shouldBlock
    ) {}
}
