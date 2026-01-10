package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.Moderation.CreateReportRequest;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    
    @PostMapping
    @Operation(summary = "Submit a report", description = "Report a listing, seller, or review for moderation review")
    public ApiResponse<ReportSubmittedResponse> submitReport(
            @AuthenticationPrincipal User reporter,
            @Valid @RequestBody CreateReportRequest request) {
        
        log.info("User {} submitting report for {} {}", 
                reporter.getId(), request.getEntityType(), request.getEntityId());
        
        ReportSubmittedResponse response = reportService.createReport(reporter, request);
        
        return ApiResponse.<ReportSubmittedResponse>builder()
                .result(response)
                .message("Report submitted successfully. Our team will review it within 24-48 hours.")
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
