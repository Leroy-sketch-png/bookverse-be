package com.example.bookverseserver.dto.request.AI;

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
public class MoodDiscoveryRequest {
    
    /**
     * Primary mood (required)
     * Examples: "adventurous", "cozy", "intellectual", "escapist"
     */
    String mood;
    
    /**
     * Optional intensity (1-5)
     * 1 = light, 5 = intense
     */
    Integer intensity;
    
    /**
     * Optional time commitment
     * Examples: "quick-read", "weekend-read", "deep-dive"
     */
    String timeCommitment;
    
    /**
     * Optional content preferences
     * Examples: ["no-violence", "happy-ending", "slow-burn"]
     */
    List<String> preferences;
    
    /**
     * Categories to include/focus on
     */
    List<Long> categoryIds;
    
    /**
     * Categories to exclude
     */
    List<Long> excludeCategoryIds;
    
    /**
     * Number of recommendations to return
     */
    @Builder.Default
    Integer limit = 10;
}
