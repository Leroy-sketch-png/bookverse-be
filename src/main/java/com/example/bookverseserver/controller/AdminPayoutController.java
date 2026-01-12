package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.PagedResponse;
import com.example.bookverseserver.dto.response.PayoutResponse;
import com.example.bookverseserver.service.PayoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/payouts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Payouts", description = "Admin payout approval and management")
public class AdminPayoutController {

    PayoutService payoutService;

    /**
     * Get pending payout requests
     */
    @GetMapping("/pending")
    @Operation(summary = "Get pending payouts", description = "List all pending payout requests for admin approval")
    public ApiResponse<PagedResponse<PayoutResponse>> getPendingPayouts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.<PagedResponse<PayoutResponse>>builder()
                .result(payoutService.getPendingPayouts(page, limit))
                .build();
    }

    /**
     * Approve a payout
     */
    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve payout", description = "Approve a pending payout request")
    public ApiResponse<PayoutResponse> approvePayout(@PathVariable Long id) {
        return ApiResponse.<PayoutResponse>builder()
                .result(payoutService.approvePayout(id))
                .build();
    }

    /**
     * Reject a payout with mandatory reason for seller transparency
     */
    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject payout", description = "Reject a pending payout request with reason")
    public ApiResponse<PayoutResponse> rejectPayout(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", "No reason provided") : "No reason provided";
        return ApiResponse.<PayoutResponse>builder()
                .result(payoutService.rejectPayout(id, reason))
                .build();
    }

    /**
     * Get pending payout count (for dashboard badge)
     */
    @GetMapping("/pending/count")
    @Operation(summary = "Get pending count", description = "Get the number of pending payout requests")
    public ApiResponse<Map<String, Long>> getPendingPayoutCount() {
        return ApiResponse.<Map<String, Long>>builder()
                .result(Map.of("count", payoutService.getPendingPayoutCount()))
                .build();
    }

    /**
     * Get payout history for a specific seller
     */
    @GetMapping("/seller/{sellerId}")
    @Operation(summary = "Get seller payout history", description = "Get all payouts for a specific seller")
    public ApiResponse<PagedResponse<PayoutResponse>> getSellerPayoutHistory(
            @PathVariable Long sellerId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.<PagedResponse<PayoutResponse>>builder()
                .result(payoutService.getPayoutHistoryBySellerId(sellerId, page, limit))
                .build();
    }
}
