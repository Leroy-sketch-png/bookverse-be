package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.PayoutRequest;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.PagedResponse;
import com.example.bookverseserver.dto.response.PayoutResponse;
import com.example.bookverseserver.dto.response.SellerBalanceResponse;
import com.example.bookverseserver.service.PayoutService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sellers/me/payouts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@PreAuthorize("hasAnyRole('SELLER', 'PRO_SELLER')")
public class PayoutController {

    PayoutService payoutService;

    /**
     * Get seller's current balance
     */
    @GetMapping("/balance")
    public ApiResponse<SellerBalanceResponse> getBalance(Authentication authentication) {
        return ApiResponse.<SellerBalanceResponse>builder()
                .result(payoutService.getBalance(authentication))
                .build();
    }

    /**
     * Request a payout
     */
    @PostMapping
    public ApiResponse<PayoutResponse> requestPayout(
            @Valid @RequestBody PayoutRequest request,
            Authentication authentication) {
        return ApiResponse.<PayoutResponse>builder()
                .result(payoutService.requestPayout(request, authentication))
                .build();
    }

    /**
     * Get payout history
     */
    @GetMapping
    public ApiResponse<PagedResponse<PayoutResponse>> getPayoutHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication) {
        return ApiResponse.<PagedResponse<PayoutResponse>>builder()
                .result(payoutService.getPayoutHistory(page, limit, authentication))
                .build();
    }
}
