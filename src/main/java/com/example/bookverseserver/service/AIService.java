package com.example.bookverseserver.service;

import com.example.bookverseserver.configuration.AIConfig;
import com.example.bookverseserver.dto.response.Product.ListingResponse;
import com.example.bookverseserver.entity.Product.Listing;
import com.example.bookverseserver.entity.Product.Review;
import com.example.bookverseserver.enums.ListingStatus;
import com.example.bookverseserver.mapper.ListingMapper;
import com.example.bookverseserver.repository.ListingRepository;
import com.example.bookverseserver.repository.ReviewRepository;
import com.example.bookverseserver.service.ai.AIProviderException;
import com.example.bookverseserver.service.ai.ProviderRotator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PRIVATE;

/**
 * AI Service — The Intelligence Layer
 * 
 * Provides AI-powered features with multi-provider rotation:
 * 1. Personalized book recommendations based on user preferences
 * 2. Natural language search parsing
 * 3. Review summarization
 * 4. Book description enhancement
 * 
 * Uses 7 AI providers with intelligent rotation for maximum uptime:
 * Groq → Mistral → OpenRouter → HuggingFace → Fireworks → Cohere → Gemini
 * 
 * IMPORTANT: Uses "snapshot" pattern for AI methods - fetch data in a SHORT 
 * transaction, close it, THEN call AI. This prevents connection leaks since 
 * AI calls can take 30+ seconds.
 */
@Service
@FieldDefaults(level = PRIVATE)
@Slf4j
public class AIService {
    
    final AIConfig aiConfig;
    final ProviderRotator providerRotator;
    final ListingRepository listingRepository;
    final ReviewRepository reviewRepository;
    final ListingMapper listingMapper;
    final ObjectMapper objectMapper;
    
    // Self-injection for proxy-based @Transactional to work on internal method calls
    AIService self;
    
    @Autowired
    public AIService(AIConfig aiConfig, ProviderRotator providerRotator, 
                     ListingRepository listingRepository, ReviewRepository reviewRepository,
                     ListingMapper listingMapper, ObjectMapper objectMapper) {
        this.aiConfig = aiConfig;
        this.providerRotator = providerRotator;
        this.listingRepository = listingRepository;
        this.reviewRepository = reviewRepository;
        this.listingMapper = listingMapper;
        this.objectMapper = objectMapper;
    }
    
    @Autowired
    @Lazy
    public void setSelf(AIService self) {
        this.self = self;
    }
    
    @PostConstruct
    public void initializeProviders() {
        if (!aiConfig.isEnabled()) {
            log.info("🤖 AI features disabled");
            return;
        }
        
        providerRotator.initialize(
                aiConfig.getGroqApiKey(),
                aiConfig.getMistralApiKey(),
                aiConfig.getOpenrouterApiKey(),
                aiConfig.getHuggingfaceApiKey(),
                aiConfig.getFireworksApiKey(),
                aiConfig.getCohereApiKey(),
                aiConfig.getGeminiApiKey()
        );
        
        if (providerRotator.isReady()) {
            log.info("🚀 AI Service ready with {} providers", providerRotator.getTotalProviderCount());
        } else {
            log.warn("⚠️ AI enabled but no providers configured");
        }
    }
    
    /**
     * Get personalized book recommendations based on user preferences.
     * 
     * @param preferencesJson User's preferences as JSON string
     * @param excludeListingIds Listings to exclude (already seen/purchased)
     * @param limit Max number of recommendations
     * @return List of recommended listings
     */
    @Transactional(readOnly = true)
    public List<ListingResponse> getPersonalizedRecommendations(
            String preferencesJson,
            List<Long> excludeListingIds,
            int limit
    ) {
        if (!aiConfig.isEnabled() || preferencesJson == null || preferencesJson.isBlank()) {
            // Fallback: return popular listings
            return getFallbackRecommendations(excludeListingIds, limit);
        }
        
        try {
            // Parse user preferences
            JsonNode prefs = objectMapper.readTree(preferencesJson);
            
            // Extract preferred categories
            List<String> preferredCategories = new ArrayList<>();
            if (prefs.has("categories") && prefs.get("categories").isArray()) {
                for (JsonNode cat : prefs.get("categories")) {
                    if (cat.has("slug") && "love".equals(cat.path("interestLevel").asText())) {
                        preferredCategories.add(cat.get("slug").asText());
                    }
                }
            }
            
            // Extract budget preference
            String budgetPref = prefs.path("readingHabits").path("budgetRange").asText("any");
            Double maxPrice = switch (budgetPref) {
                case "budget" -> 10.0;
                case "mid-range" -> 25.0;
                default -> null;
            };
            
            // Extract condition preference
            String conditionPref = prefs.path("readingHabits").path("conditionPreference").asText("any-condition");
            List<String> acceptableConditions = switch (conditionPref) {
                case "new-only" -> List.of("NEW");
                case "like-new" -> List.of("NEW", "LIKE_NEW");
                default -> List.of("NEW", "LIKE_NEW", "GOOD", "ACCEPTABLE");
            };
            
            // Query listings based on preferences
            List<Listing> candidates = listingRepository.findRecommendationCandidates(
                    preferredCategories.isEmpty() ? null : preferredCategories,
                    maxPrice,
                    acceptableConditions,
                    excludeListingIds == null ? List.of(-1L) : excludeListingIds,
                    ListingStatus.ACTIVE,
                    PageRequest.of(0, limit * 3) // Get more than needed for AI ranking
            );
            
            if (candidates.isEmpty()) {
                return getFallbackRecommendations(excludeListingIds, limit);
            }
            
            // If AI is available, use it to rank/personalize further
            // For now, return top matches directly
            return candidates.stream()
                    .limit(limit)
                    .map(listingMapper::toListingResponse)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error generating recommendations: {}", e.getMessage());
            return getFallbackRecommendations(excludeListingIds, limit);
        }
    }
    
    /**
     * Parse natural language search query into structured filters.
     * 
     * Example: "cheap mystery books under $15" → 
     *   { categories: ["mystery"], maxPrice: 15, condition: null }
     */
    public SearchFilters parseNaturalLanguageQuery(String query) {
        if (!aiConfig.isEnabled() || query == null || query.length() < 5) {
            return new SearchFilters(query, null, null, null, null);
        }
        
        try {
            String prompt = buildSearchParsingPrompt(query);
            String response = callLLM(prompt);
            
            // Parse JSON response
            JsonNode filters = objectMapper.readTree(response);
            
            return new SearchFilters(
                    filters.path("searchTerms").asText(query),
                    filters.path("categories").isArray() 
                            ? streamToList(filters.path("categories").elements()) : null,
                    filters.path("maxPrice").isNumber() 
                            ? filters.path("maxPrice").asDouble() : null,
                    filters.path("minRating").isNumber() 
                            ? filters.path("minRating").asDouble() : null,
                    filters.path("condition").isTextual() 
                            ? filters.path("condition").asText() : null
            );
            
        } catch (Exception e) {
            log.warn("Failed to parse natural language query: {}", e.getMessage());
            return new SearchFilters(query, null, null, null, null);
        }
    }
    
    /**
     * DTO to hold review data after transaction closes.
     * This prevents keeping DB connection open during AI calls.
     */
    public record ReviewSnapshot(
            int rating,
            String comment,
            String username
    ) {
        static ReviewSnapshot from(Review r) {
            return new ReviewSnapshot(
                    r.getRating(),
                    r.getComment(),
                    r.getUser() != null ? r.getUser().getUsername() : "Anonymous"
            );
        }
    }
    
    /**
     * Fetch reviews in a SHORT transaction and convert to snapshots.
     * Transaction closes immediately, then AI can process without holding connection.
     */
    @Transactional(readOnly = true)
    public List<ReviewSnapshot> fetchReviewSnapshots(Long bookId) {
        return reviewRepository.findByBookMetaIdOrderByCreatedAtDesc(bookId).stream()
                .map(ReviewSnapshot::from)
                .toList();
    }
    
    /**
     * Summarize reviews for a book using snapshot pattern.
     * Fetches reviews in a short transaction, then calls AI OUTSIDE the transaction.
     * 
     * @param bookId Book ID to summarize reviews for
     * @return AI-generated summary or null if not enough reviews
     */
    public ReviewSummary summarizeReviews(Long bookId) {
        // Fetch reviews via self-proxy to ensure @Transactional works
        List<ReviewSnapshot> reviews = self.fetchReviewSnapshots(bookId);
        
        if (reviews.size() < 3) {
            return null; // Not enough reviews to summarize
        }
        
        if (!aiConfig.isEnabled()) {
            // Fallback: simple stats-based summary
            return buildStatsSummaryFromSnapshots(reviews);
        }
        
        try {
            String prompt = buildReviewSummaryPromptFromSnapshots(reviews);
            // AI call happens OUTSIDE the transaction - no connection leak!
            String response = callLLM(prompt);
            
            JsonNode summary = objectMapper.readTree(response);
            
            return new ReviewSummary(
                    summary.path("overallSentiment").asText("mixed"),
                    summary.path("summary").asText(),
                    streamToList(summary.path("strengths").elements()),
                    streamToList(summary.path("weaknesses").elements()),
                    summary.path("recommendedFor").asText()
            );
            
        } catch (Exception e) {
            log.warn("Failed to summarize reviews: {}", e.getMessage());
            return buildStatsSummaryFromSnapshots(reviews);
        }
    }
    
    // ─────────────────────────────────────────────────────────────────────────────
    // Private Helpers
    // ─────────────────────────────────────────────────────────────────────────────
    
    private List<ListingResponse> getFallbackRecommendations(List<Long> excludeIds, int limit) {
        // Return popular active listings
        return listingRepository.findPopularListings(
                        ListingStatus.ACTIVE,
                        excludeIds == null ? List.of(-1L) : excludeIds,
                        PageRequest.of(0, limit)
                ).stream()
                .map(listingMapper::toListingResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Call LLM using multi-provider rotation.
     * Falls back gracefully through all available providers.
     */
    private String callLLM(String prompt) {
        if (!providerRotator.isReady()) {
            throw new IllegalStateException("No AI providers configured");
        }
        
        try {
            return providerRotator.generate(prompt, aiConfig.getTimeoutSeconds(), aiConfig.getMaxRetries());
        } catch (AIProviderException e) {
            log.error("All AI providers failed: {}", e.getMessage());
            throw new RuntimeException("AI service unavailable", e);
        }
    }
    
    /**
     * Generate raw text from a prompt (public API for other services)
     */
    public String generateRecommendation(String prompt) {
        return callLLM(prompt);
    }
    
    /**
     * Get status of all AI providers (for health checks and debugging)
     */
    public Map<String, ProviderRotator.ProviderStatusInfo> getProvidersStatus() {
        return providerRotator.getProvidersStatus();
    }
    
    /**
     * Check if AI is available
     */
    public boolean isAIAvailable() {
        return aiConfig.isEnabled() && providerRotator.isReady() && providerRotator.getAvailableProviderCount() > 0;
    }
    
    private String buildSearchParsingPrompt(String query) {
        return """
            Parse this book search query into structured filters. Return JSON only.
            
            Query: "%s"
            
            Extract:
            - searchTerms: the main search text (title/author keywords)
            - categories: array of category slugs if mentioned (fiction, mystery, sci-fi, etc)
            - maxPrice: number if price limit mentioned
            - minRating: number if rating mentioned
            - condition: NEW, LIKE_NEW, GOOD, ACCEPTABLE if mentioned
            
            Return ONLY valid JSON, no explanation:
            {"searchTerms": "...", "categories": [...], "maxPrice": null, "minRating": null, "condition": null}
            """.formatted(query);
    }
    
    private String buildReviewSummaryPrompt(List<Review> reviews) {
        StringBuilder reviewText = new StringBuilder();
        for (int i = 0; i < Math.min(10, reviews.size()); i++) {
            Review r = reviews.get(i);
            reviewText.append("Rating: ").append(r.getRating()).append("/5\n");
            reviewText.append("Review: ").append(r.getComment()).append("\n\n");
        }
        
        return """
            Summarize these book reviews. Return JSON only.
            
            Reviews:
            %s
            
            Return ONLY valid JSON:
            {
                "overallSentiment": "positive|mixed|negative",
                "summary": "2-3 sentence summary of what readers say",
                "strengths": ["strength1", "strength2"],
                "weaknesses": ["weakness1"],
                "recommendedFor": "who would enjoy this book"
            }
            """.formatted(reviewText.toString());
    }
    
    private ReviewSummary buildStatsSummary(List<Review> reviews) {
        double avgRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0);
        
        String sentiment = avgRating >= 4 ? "positive" : avgRating >= 3 ? "mixed" : "negative";
        String summary = String.format(
                "Based on %d reviews with an average rating of %.1f/5.",
                reviews.size(), avgRating
        );
        
        return new ReviewSummary(sentiment, summary, List.of(), List.of(), null);
    }
    
    // Snapshot-based helper methods (for use after transaction closes)
    
    private String buildReviewSummaryPromptFromSnapshots(List<ReviewSnapshot> reviews) {
        StringBuilder reviewText = new StringBuilder();
        for (int i = 0; i < Math.min(10, reviews.size()); i++) {
            ReviewSnapshot r = reviews.get(i);
            reviewText.append("Rating: ").append(r.rating()).append("/5\n");
            reviewText.append("Review: ").append(r.comment()).append("\n\n");
        }
        
        return """
            Summarize these book reviews. Return JSON only.
            
            Reviews:
            %s
            
            Return ONLY valid JSON:
            {
                "overallSentiment": "positive|mixed|negative",
                "summary": "2-3 sentence summary of what readers say",
                "strengths": ["strength1", "strength2"],
                "weaknesses": ["weakness1"],
                "recommendedFor": "who would enjoy this book"
            }
            """.formatted(reviewText.toString());
    }
    
    private ReviewSummary buildStatsSummaryFromSnapshots(List<ReviewSnapshot> reviews) {
        double avgRating = reviews.stream()
                .mapToInt(ReviewSnapshot::rating)
                .average()
                .orElse(0);
        
        String sentiment = avgRating >= 4 ? "positive" : avgRating >= 3 ? "mixed" : "negative";
        String summary = String.format(
                "Based on %d reviews with an average rating of %.1f/5.",
                reviews.size(), avgRating
        );
        
        return new ReviewSummary(sentiment, summary, List.of(), List.of(), null);
    }
    
    private List<String> streamToList(Iterator<JsonNode> iterator) {
        List<String> result = new ArrayList<>();
        while (iterator.hasNext()) {
            result.add(iterator.next().asText());
        }
        return result;
    }
    
    // ─────────────────────────────────────────────────────────────────────────────
    // DTOs
    // ─────────────────────────────────────────────────────────────────────────────
    
    public record SearchFilters(
            String searchTerms,
            List<String> categories,
            Double maxPrice,
            Double minRating,
            String condition
    ) {}
    
    public record ReviewSummary(
            String overallSentiment,
            String summary,
            List<String> strengths,
            List<String> weaknesses,
            String recommendedFor
    ) {}
}
