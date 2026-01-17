package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.AI.MoodDiscoveryRequest;
import com.example.bookverseserver.dto.response.AI.MoodDiscoveryResponse;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.Product.ListingResponse;
import com.example.bookverseserver.service.AIService;
import com.example.bookverseserver.service.MoodDiscoveryService;
import com.example.bookverseserver.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static lombok.AccessLevel.PRIVATE;

/**
 * AI Controller — Intelligence Endpoints
 * 
 * Provides AI-powered features:
 * - Personalized recommendations
 * - Mood-based discovery
 * - Natural language search
 * - Review summarization
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "AI", description = "AI-powered features for personalized experiences")
public class AIController {
    
    AIService aiService;
    MoodDiscoveryService moodDiscoveryService;
    SecurityUtils securityUtils;
    
    /**
     * Get personalized book recommendations for the authenticated user.
     */
    @GetMapping("/recommendations")
    @Operation(summary = "Get personalized recommendations", 
               description = "Returns book recommendations based on user's reading preferences")
    public ApiResponse<List<ListingResponse>> getRecommendations(
            Authentication auth,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) List<Long> excludeIds
    ) {
        Long userId = null;
        if (auth != null && auth.isAuthenticated()) {
            userId = securityUtils.getCurrentUserId(auth);
        }
        
        List<ListingResponse> recommendations = aiService.getPersonalizedRecommendationsForUser(
                userId,
                excludeIds,
                limit
        );
        
        return ApiResponse.<List<ListingResponse>>builder()
                .result(recommendations)
                .build();
    }
    
    /**
     * Parse natural language search query into structured filters.
     */
    @GetMapping("/search/parse")
    @Operation(summary = "Parse natural language search", 
               description = "Converts natural language query to structured search filters")
    public ApiResponse<AIService.SearchFilters> parseSearch(@RequestParam String query) {
        AIService.SearchFilters filters = aiService.parseNaturalLanguageQuery(query);
        
        return ApiResponse.<AIService.SearchFilters>builder()
                .result(filters)
                .build();
    }
    
    /**
     * Get AI-generated review summary for a book.
     */
    @GetMapping("/reviews/summary/{bookId}")
    @Operation(summary = "Get review summary", 
               description = "Returns AI-generated summary of reviews for a book")
    public ApiResponse<AIService.ReviewSummary> getReviewSummary(@PathVariable Long bookId) {
        AIService.ReviewSummary summary = aiService.summarizeReviews(bookId);
        
        return ApiResponse.<AIService.ReviewSummary>builder()
                .result(summary)
                .build();
    }
    
    /**
     * Get status of all AI providers.
     * Useful for health checks and debugging.
     */
    @GetMapping("/providers/status")
    @Operation(summary = "Get AI providers status", 
               description = "Returns status, availability, and statistics for all AI providers")
    public ApiResponse<AIProvidersStatusResponse> getProvidersStatus() {
        var status = aiService.getProvidersStatus();
        boolean available = aiService.isAIAvailable();
        
        return ApiResponse.<AIProvidersStatusResponse>builder()
                .result(new AIProvidersStatusResponse(
                        available,
                        status.size(),
                        (int) status.values().stream().filter(s -> s.available()).count(),
                        status
                ))
                .build();
    }
    
    /**
     * Response for providers status endpoint
     */
    public record AIProvidersStatusResponse(
            boolean aiAvailable,
            int totalProviders,
            int availableProviders,
            java.util.Map<String, com.example.bookverseserver.service.ai.ProviderRotator.ProviderStatusInfo> providers
    ) {}
    
    // ============================================================================
    // MOOD-BASED DISCOVERY
    // ============================================================================
    
    /**
     * Get available reading moods with their definitions
     */
    @GetMapping("/moods")
    @Operation(summary = "Get available reading moods", 
               description = "Returns all available moods with emoji, tagline, and description")
    public ApiResponse<List<Map<String, Object>>> getAvailableMoods() {
        return ApiResponse.<List<Map<String, Object>>>builder()
                .result(moodDiscoveryService.getAvailableMoods())
                .build();
    }
    
    /**
     * Discover books by mood
     */
    @PostMapping("/moods/discover")
    @Operation(summary = "Discover books by mood", 
               description = "Returns AI-curated book recommendations based on reading mood")
    public ApiResponse<MoodDiscoveryResponse> discoverByMood(@RequestBody MoodDiscoveryRequest request) {
        MoodDiscoveryResponse response = moodDiscoveryService.discoverByMood(request);
        return ApiResponse.<MoodDiscoveryResponse>builder()
                .result(response)
                .build();
    }
    
    /**
     * Quick mood discovery (GET for simple use)
     */
    @GetMapping("/moods/{mood}/books")
    @Operation(summary = "Quick mood-based discovery", 
               description = "Simple endpoint to get books for a mood")
    public ApiResponse<MoodDiscoveryResponse> quickMoodDiscovery(
            @PathVariable String mood,
            @RequestParam(defaultValue = "10") int limit
    ) {
        MoodDiscoveryRequest request = MoodDiscoveryRequest.builder()
                .mood(mood)
                .limit(limit)
                .build();
        return ApiResponse.<MoodDiscoveryResponse>builder()
                .result(moodDiscoveryService.discoverByMood(request))
                .build();
    }
}
