package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.User.ProSellerApplicationRequest;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.User.ProSellerApplicationResponse;
import com.example.bookverseserver.service.ProSellerService;
import com.example.bookverseserver.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * PRO Seller Application API.
 * Per Vision features/pro-seller.md — Handles PRO seller application workflow.
 */
@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "PRO Seller", description = "APIs for PRO seller application and management")
public class ProSellerController {

    ProSellerService proSellerService;
    SecurityUtils securityUtils;

    @PostMapping("/pro-application")
    @PreAuthorize("hasAnyRole('SELLER', 'USER')")
    @Operation(summary = "Submit PRO seller application",
               description = "Submit application for PRO seller status with business documentation")
    public ApiResponse<ProSellerApplicationResponse> submitApplication(
            @Valid @RequestBody ProSellerApplicationRequest request,
            Authentication authentication) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        return ApiResponse.<ProSellerApplicationResponse>builder()
                .message("PRO seller application submitted successfully")
                .result(proSellerService.submitApplication(userId, request))
                .build();
    }

    @GetMapping("/pro-application")
    @PreAuthorize("hasAnyRole('SELLER', 'USER')")
    @Operation(summary = "Get PRO application status",
               description = "Get the status of current user's PRO seller application")
    public ApiResponse<ProSellerApplicationResponse> getApplicationStatus(
            Authentication authentication) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        return ApiResponse.<ProSellerApplicationResponse>builder()
                .message("Application status retrieved successfully")
                .result(proSellerService.getApplicationStatus(userId))
                .build();
    }

    // Admin endpoints for application review will be in AdminController
}
