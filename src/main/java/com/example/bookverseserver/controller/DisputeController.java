package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.DisputeRequest;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.Moderation.DisputeResponse;
import com.example.bookverseserver.dto.response.PagedResponse;
import com.example.bookverseserver.service.ModerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * DisputeController - Buyer-facing dispute management.
 * Per CRITICAL_GAPS.md P1: Buyer Dispute Form.
 */
@RestController
@RequestMapping("/api/disputes")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Disputes", description = "Buyer-facing dispute filing and tracking")
public class DisputeController {

    ModerationService moderationService;

    /**
     * File a new dispute for an order.
     * Only the buyer of the order can file a dispute.
     */
    @PostMapping
    @Operation(summary = "File a dispute", description = "Submit a dispute for a shipped or delivered order")
    public ApiResponse<DisputeResponse> createDispute(
            @Valid @RequestBody DisputeRequest request,
            Authentication authentication) {
        return ApiResponse.<DisputeResponse>builder()
                .result(moderationService.createDispute(request, authentication))
                .build();
    }

    /**
     * Get disputes filed by the current user.
     */
    @GetMapping("/my")
    @Operation(summary = "Get my disputes", description = "Get a list of disputes filed by the current user")
    public ApiResponse<PagedResponse<DisputeResponse>> getMyDisputes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication) {
        return ApiResponse.<PagedResponse<DisputeResponse>>builder()
                .result(moderationService.getMyDisputes(authentication, page, limit))
                .build();
    }
}
