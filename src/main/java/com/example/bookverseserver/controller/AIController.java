package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.Product.ListingResponse;
import com.example.bookverseserver.entity.User.UserProfile;
import com.example.bookverseserver.repository.UserProfileRepository;
import com.example.bookverseserver.service.AIService;
import com.example.bookverseserver.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

/**
 * AI Controller — Intelligence Endpoints
 * 
 * Provides AI-powered features:
 * - Personalized recommendations
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
    UserProfileRepository userProfileRepository;
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
        String preferences = null;
        
        if (auth != null && auth.isAuthenticated()) {
            Long userId = securityUtils.getCurrentUserId(auth);
            UserProfile profile = userProfileRepository.findByUserId(userId).orElse(null);
            if (profile != null) {
                preferences = profile.getPreferences();
            }
        }
        
        List<ListingResponse> recommendations = aiService.getPersonalizedRecommendations(
                preferences,
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
}
