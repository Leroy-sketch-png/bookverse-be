package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.request.ModerationRequest;
import com.example.bookverseserver.dto.response.ModerationResponse;
import com.example.bookverseserver.enums.ContentModerationDecision;
import com.example.bookverseserver.service.ContentModerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * Content Moderation Controller — UGC validation before submission
 * 
 * Provides content moderation endpoints for checking user-generated content
 * before submission (reviews, listings, messages).
 * 
 * Hybrid approach: Fast rule-based checks + AI for uncertain cases
 * Pattern stolen from chefkix-ai-service, adapted for Bookverse
 */
@RestController
@RequestMapping("/api/content-moderation")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Content Moderation", description = "Hybrid rule + AI content moderation for UGC")
public class ContentModerationController {
    
    ContentModerationService contentModerationService;
    
    /**
     * Check content for policy violations
     * 
     * Returns decision (APPROVE/FLAG/BLOCK), category, severity, and matched terms.
     * Use this before submitting reviews or listing descriptions.
     */
    @PostMapping("/check")
    @Operation(summary = "Check content for policy violations")
    public ApiResponse<ModerationResponse> checkContent(
            @Valid @RequestBody ModerationRequest request) {
        
        log.debug("Content moderation check for {} chars of type: {}", 
            request.getText().length(), request.getContentType());
        
        ModerationResponse response = contentModerationService.moderate(request);
        
        if (response.getDecision() != ContentModerationDecision.APPROVE) {
            log.info("🚨 Content {} (score: {}, category: {})", 
                response.getDecision(), response.getScore(), response.getCategory());
        }
        
        return ApiResponse.<ModerationResponse>builder()
            .result(response)
            .build();
    }
    
    /**
     * Quick check - returns just approve/reject boolean
     * 
     * Lightweight endpoint for inline validation (e.g., as user types)
     */
    @PostMapping("/quick-check")
    @Operation(summary = "Quick approve/reject check")
    public ApiResponse<QuickCheckResponse> quickCheck(
            @RequestParam String text) {
        
        boolean blocked = contentModerationService.shouldBlock(text);
        boolean needsReview = !blocked && contentModerationService.needsReview(text);
        
        return ApiResponse.<QuickCheckResponse>builder()
            .result(new QuickCheckResponse(!blocked, needsReview))
            .build();
    }
    
    public record QuickCheckResponse(boolean allowed, boolean needsReview) {}
}
