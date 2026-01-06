package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.Moderation.ModerationActionRequest;
import com.example.bookverseserver.dto.response.Admin.PlatformStatsResponse;
import com.example.bookverseserver.dto.response.Admin.ProApplicationDetailResponse;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.PagedResponse;
import com.example.bookverseserver.dto.response.User.ProSellerApplicationResponse;
import com.example.bookverseserver.enums.ApplicationStatus;
import com.example.bookverseserver.service.AdminService;
import com.example.bookverseserver.service.ProSellerService;
import com.example.bookverseserver.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Admin API for platform management.
 * Per Vision features/admin.md and API_CONTRACTS.md
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Admin", description = "Platform administration APIs")
public class AdminController {

    AdminService adminService;
    ProSellerService proSellerService;
    SecurityUtils securityUtils;

    // ============ Platform Stats ============

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get platform statistics",
               description = "Returns aggregated platform stats for admin dashboard")
    public ApiResponse<PlatformStatsResponse> getPlatformStats(
            @RequestParam(defaultValue = "30") int period) {
        return ApiResponse.<PlatformStatsResponse>builder()
                .message("Platform stats retrieved successfully")
                .result(adminService.getPlatformStats(period))
                .build();
    }

    // ============ PRO Seller Applications ============

    @GetMapping("/pro-applications")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List PRO seller applications",
               description = "Get paginated list of PRO seller applications for review")
    public ApiResponse<PagedResponse<ProApplicationDetailResponse>> getProApplications(
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.<PagedResponse<ProApplicationDetailResponse>>builder()
                .message("PRO applications retrieved successfully")
                .result(adminService.getProApplications(status, page, limit))
                .build();
    }

    @PostMapping("/pro-applications/{applicationId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve PRO seller application",
               description = "Approve a PRO seller application and upgrade user to PRO_SELLER")
    public ApiResponse<ProSellerApplicationResponse> approveApplication(
            @PathVariable Long applicationId,
            @RequestBody(required = false) ReviewRequest request,
            Authentication authentication) {
        Long adminId = securityUtils.getCurrentUserId(authentication);
        String notes = request != null ? request.notes : null;
        return ApiResponse.<ProSellerApplicationResponse>builder()
                .message("PRO application approved successfully")
                .result(proSellerService.approveApplication(applicationId, adminId, notes))
                .build();
    }

    @PostMapping("/pro-applications/{applicationId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reject PRO seller application",
               description = "Reject a PRO seller application with reason")
    public ApiResponse<ProSellerApplicationResponse> rejectApplication(
            @PathVariable Long applicationId,
            @RequestBody ReviewRequest request,
            Authentication authentication) {
        Long adminId = securityUtils.getCurrentUserId(authentication);
        return ApiResponse.<ProSellerApplicationResponse>builder()
                .message("PRO application rejected")
                .result(proSellerService.rejectApplication(applicationId, adminId, request.notes))
                .build();
    }

    /**
     * Simple request body for review actions.
     */
    public record ReviewRequest(String notes) {}
}
