package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.Moderation.CreateReportRequest;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.Moderation.UserReportResponse;
import com.example.bookverseserver.dto.response.PagedResponse;
import com.example.bookverseserver.service.ReportService;
import com.example.bookverseserver.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * ReportController - Public endpoint for users to submit reports.
 * 
 * Any authenticated user can report:
 * - Fraudulent listings
 * - Problematic sellers
 * - Inappropriate reviews
 * 
 * Reports go to moderation queue for review.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Reports", description = "User-submitted reports for trust & safety")
public class ReportController {
    
    ReportService reportService;
    SecurityUtils securityUtils;
    
    @PostMapping
    @Operation(summary = "Submit a report", description = "Report a listing, seller, or review for moderation review")
    public ApiResponse<ReportSubmittedResponse> submitReport(
            Authentication authentication,
            @Valid @RequestBody CreateReportRequest request) {
        
        Long reporterId = securityUtils.getCurrentUserId(authentication);
        log.info("User {} submitting report for {} {}", 
                reporterId, request.getEntityType(), request.getEntityId());
        
        ReportSubmittedResponse response = reportService.createReport(reporterId, request);
        
        return ApiResponse.<ReportSubmittedResponse>builder()
                .result(response)
                .message("Report submitted successfully. Our team will review it within 24-48 hours.")
                .build();
    }
    
    @GetMapping("/my")
    @Operation(summary = "Get my reports", description = "Get reports submitted by the current user")
    public ApiResponse<PagedResponse<UserReportResponse>> getMyReports(
            Authentication authentication,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        
        Long reporterId = securityUtils.getCurrentUserId(authentication);
        Page<UserReportResponse> reports = reportService.getMyReports(reporterId, page, limit);
        
        PagedResponse<UserReportResponse> pagedResponse = PagedResponse.<UserReportResponse>builder()
                .data(reports.getContent())
                .meta(PagedResponse.PaginationMeta.builder()
                        .page(reports.getNumber() + 1)
                        .limit(reports.getSize())
                        .totalItems(reports.getTotalElements())
                        .totalPages(reports.getTotalPages())
                        .hasNext(reports.hasNext())
                        .hasPrev(reports.hasPrevious())
                        .build())
                .build();
        
        return ApiResponse.<PagedResponse<UserReportResponse>>builder()
                .result(pagedResponse)
                .build();
    }
    
    /**
     * Simple response for report submission.
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReportSubmittedResponse {
        Long reportId;
        String status;
        String message;
    }
}
