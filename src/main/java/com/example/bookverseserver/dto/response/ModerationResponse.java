package com.example.bookverseserver.dto.response;

import com.example.bookverseserver.enums.ContentCategory;
import com.example.bookverseserver.enums.ContentModerationDecision;
import com.example.bookverseserver.enums.ContentSeverity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

/**
 * Response from content moderation check
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ModerationResponse {
    
    /**
     * Decision: APPROVE, FLAG, or BLOCK
     */
    ContentModerationDecision decision;
    
    /**
     * Category of issue detected (or CLEAN)
     */
    ContentCategory category;
    
    /**
     * Severity level of the issue
     */
    ContentSeverity severity;
    
    /**
     * Confidence score 0-100
     */
    Integer score;
    
    /**
     * Terms that triggered the moderation
     */
    List<String> matchedTerms;
    
    /**
     * Human-readable reason for the decision
     */
    String reason;
    
    /**
     * Whether AI was used for this decision
     */
    Boolean aiUsed;
    
    /**
     * Processing time in milliseconds
     */
    Long processingTimeMs;
}
