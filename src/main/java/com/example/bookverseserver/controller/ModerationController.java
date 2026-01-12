package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.Moderation.FlagListingRequest;
import com.example.bookverseserver.dto.request.Moderation.ModerationActionRequest;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.Moderation.*;
import com.example.bookverseserver.dto.response.PagedResponse;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.*;
import com.example.bookverseserver.service.ModerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Moderation Controller - per Vision features/moderation.md.
 * 
 * Endpoints:
 * - GET  /api/moderation/summary           - Dashboard summary
 * - GET  /api/moderation/listings          - Flagged listings queue
 * - POST /api/moderation/listings/{id}     - Review flagged listing
 * - GET  /api/moderation/reports           - User reports queue
 * - POST /api/moderation/reports/{id}      - Take action on report
 * - GET  /api/moderation/disputes          - Disputes queue
 * - POST /api/moderation/disputes/{id}     - Resolve dispute
 */
@RestController
@RequestMapping("/api/moderation")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Moderation", description = "Content moderation and trust & safety")
@PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
public class ModerationController {
    
    ModerationService moderationService;
    
    // ============ Dashboard ============
    
    @GetMapping("/summary")
    @Operation(summary = "Get moderation dashboard summary")
    public ApiResponse<ModerationSummary> getSummary() {
        ModerationSummary summary = moderationService.getSummary();
        
        return ApiResponse.<ModerationSummary>builder()
                .result(summary)
                .build();
    }
    
    // ============ Flagged Listings ============
    
    @PostMapping("/listings/flag")
    @Operation(summary = "Flag a listing for moderation review")
    public ApiResponse<FlaggedListingResponse> flagListing(
            @AuthenticationPrincipal User moderator,
            @Valid @RequestBody FlagListingRequest request) {
        
        log.info("Moderator {} flagging listing {}", moderator.getId(), request.getListingId());
        FlaggedListingResponse response = moderationService.flagListing(request, moderator.getId());
        
        return ApiResponse.<FlaggedListingResponse>builder()
                .result(response)
                .build();
    }
    
    @GetMapping("/listings")
    @Operation(summary = "Get flagged listings queue")
    public ApiResponse<PagedResponse<FlaggedListingResponse>> getFlaggedListings(
            @RequestParam(required = false) FlagStatus status,
            @RequestParam(required = false) FlagSeverity severity,
            @RequestParam(required = false) FlagType flagType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        
        PagedResponse<FlaggedListingResponse> response = moderationService.getFlaggedListings(
                status, severity, flagType, page, limit);
        
        return ApiResponse.<PagedResponse<FlaggedListingResponse>>builder()
                .result(response)
                .build();
    }
    
    @PostMapping("/listings/{flagId}/review")
    @Operation(summary = "Review and take action on flagged listing")
    public ApiResponse<FlaggedListingResponse> reviewFlaggedListing(
            @AuthenticationPrincipal User moderator,
            @PathVariable Long flagId,
            @Valid @RequestBody ModerationActionRequest request) {
        
        log.info("Moderator {} reviewing flagged listing {}", moderator.getId(), flagId);
        FlaggedListingResponse response = moderationService.reviewFlaggedListing(
                moderator.getId(), flagId, request);
        
        return ApiResponse.<FlaggedListingResponse>builder()
                .result(response)
                .build();
    }
    
    // ============ User Reports ============
    
    @GetMapping("/reports")
    @Operation(summary = "Get user reports queue")
    public ApiResponse<PagedResponse<UserReportResponse>> getReports(
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) ReportType type,
            @RequestParam(required = false) ReportPriority priority,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        
        PagedResponse<UserReportResponse> response = moderationService.getReports(
                status, type, priority, page, limit);
        
        return ApiResponse.<PagedResponse<UserReportResponse>>builder()
                .result(response)
                .build();
    }
    
    @PostMapping("/reports/{reportId}/action")
    @Operation(summary = "Take action on user report")
    public ApiResponse<UserReportResponse> takeActionOnReport(
            @AuthenticationPrincipal User moderator,
            @PathVariable Long reportId,
            @Valid @RequestBody ModerationActionRequest request) {
        
        log.info("Moderator {} taking action on report {}", moderator.getId(), reportId);
        UserReportResponse response = moderationService.takeActionOnReport(
                moderator.getId(), reportId, request);
        
        return ApiResponse.<UserReportResponse>builder()
                .result(response)
                .build();
    }
    
    // ============ Disputes ============
    
    @GetMapping("/disputes")
    @Operation(summary = "Get disputes queue")
    public ApiResponse<PagedResponse<DisputeResponse>> getDisputes(
            @RequestParam(required = false) DisputeStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        
        PagedResponse<DisputeResponse> response = moderationService.getDisputes(
                status, page, limit);
        
        return ApiResponse.<PagedResponse<DisputeResponse>>builder()
                .result(response)
                .build();
    }
    
    @PostMapping("/disputes/{disputeId}/resolve")
    @Operation(summary = "Resolve a dispute")
    public ApiResponse<DisputeResponse> resolveDispute(
            @AuthenticationPrincipal User moderator,
            @PathVariable Long disputeId,
            @Valid @RequestBody ModerationActionRequest request) {
        
        log.info("Moderator {} resolving dispute {}", moderator.getId(), disputeId);
        DisputeResponse response = moderationService.resolveDispute(
                moderator.getId(), disputeId, request);
        
        return ApiResponse.<DisputeResponse>builder()
                .result(response)
                .build();
    }
}
