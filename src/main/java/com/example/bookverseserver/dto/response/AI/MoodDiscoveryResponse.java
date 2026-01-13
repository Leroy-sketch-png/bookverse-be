package com.example.bookverseserver.dto.response.AI;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MoodDiscoveryResponse {
    
    /**
     * The mood that was analyzed
     */
    String mood;
    
    /**
     * AI-generated mood interpretation
     */
    String moodDescription;
    
    /**
     * Curated reading atmosphere suggestion
     */
    String atmosphereSuggestion;
    
    /**
     * Recommended books with mood-specific reasoning
     */
    List<MoodRecommendation> recommendations;
    
    /**
     * Related moods the user might also enjoy
     */
    List<RelatedMood> relatedMoods;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class MoodRecommendation {
        Long listingId;
        String title;
        String author;
        String coverUrl;
        Double price;
        String condition;
        Double matchScore; // 0-100 how well it matches the mood
        String whyThisFits; // AI explanation
        List<String> moodTags; // e.g., ["cozy", "heartwarming", "slow-paced"]
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class RelatedMood {
        String name;
        String emoji;
        String description;
    }
}
