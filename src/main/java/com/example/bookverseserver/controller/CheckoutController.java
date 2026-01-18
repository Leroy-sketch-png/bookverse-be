package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.Order.*;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.Order.*;
import com.example.bookverseserver.service.CheckoutService;
import com.example.bookverseserver.util.SecurityUtils;
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
 * Checkout Controller - Complete checkout flow per Vision buyer-flow.md
 * 
 * Flow:
 * 1. POST /checkout/session - Create session from cart
 * 2. PATCH /checkout/{sessionId} - Update shipping address
 * 3. POST /checkout/{sessionId}/voucher - Apply voucher
 * 4. DELETE /checkout/{sessionId}/voucher - Remove voucher
 * 5. POST /checkout/{sessionId}/complete - Create payment intent and finalize
 */
@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Checkout", description = "Complete checkout flow with Stripe payment integration")
public class CheckoutController {

    CheckoutService checkoutService;
    SecurityUtils securityUtils;

    /**
     * Step 1: Create checkout session from current cart
     * Cart contents are snapshotted, session expires in 24h
     */
    @PostMapping("/session")
    @Operation(summary = "Create checkout session", description = "Creates a new checkout session from user's cart")
    public ApiResponse<CheckoutSessionResponse> createSession(Authentication authentication) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        log.info("Creating checkout session for user {}", userId);
        
        CheckoutSessionResponse response = checkoutService.createSession(userId);
        
        return ApiResponse.<CheckoutSessionResponse>builder()
                .message("Checkout session created successfully")
                .result(response)
                .build();
    }

    /**
     * Get current checkout session details
     */
    @GetMapping("/{sessionId}")
    @Operation(summary = "Get checkout session", description = "Retrieves checkout session details")
    public ApiResponse<CheckoutSessionResponse> getSession(
            @PathVariable Long sessionId,
            Authentication authentication) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        
        CheckoutSessionResponse response = checkoutService.getSession(userId, sessionId);
        
        return ApiResponse.<CheckoutSessionResponse>builder()
                .result(response)
                .build();
    }

    /**
     * Step 2: Update checkout session (set shipping address)
     */
    @PatchMapping("/{sessionId}")
    @Operation(summary = "Update checkout session", description = "Update shipping address or other session details")
    public ApiResponse<CheckoutSessionResponse> updateSession(
            @PathVariable Long sessionId,
            @Valid @RequestBody UpdateCheckoutSessionRequest request,
            Authentication authentication) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        log.info("Updating checkout session {} for user {}", sessionId, userId);
        
        CheckoutSessionResponse response = checkoutService.updateSession(userId, sessionId, request);
        
        return ApiResponse.<CheckoutSessionResponse>builder()
                .message("Checkout session updated")
                .result(response)
                .build();
    }

    /**
     * Step 3: Apply voucher code
     */
    @PostMapping("/{sessionId}/voucher")
    @Operation(summary = "Apply voucher", description = "Apply a discount voucher to the checkout session")
    public ApiResponse<ApplyVoucherResponse> applyVoucher(
            @PathVariable Long sessionId,
            @Valid @RequestBody ApplyVoucherRequest request,
            Authentication authentication) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        log.info("Applying voucher {} to session {} for user {}", request.getCode(), sessionId, userId);
        
        ApplyVoucherResponse response = checkoutService.applyVoucher(userId, sessionId, request.getCode());
        
        return ApiResponse.<ApplyVoucherResponse>builder()
                .message("Voucher applied successfully")
                .result(response)
                .build();
    }

    /**
     * Remove applied voucher
     */
    @DeleteMapping("/{sessionId}/voucher")
    @Operation(summary = "Remove voucher", description = "Remove applied voucher from checkout session")
    public ApiResponse<CheckoutSessionResponse> removeVoucher(
            @PathVariable Long sessionId,
            Authentication authentication) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        log.info("Removing voucher from session {} for user {}", sessionId, userId);
        
        CheckoutSessionResponse response = checkoutService.removeVoucher(userId, sessionId);
        
        return ApiResponse.<CheckoutSessionResponse>builder()
                .message("Voucher removed")
                .result(response)
                .build();
    }

    /**
     * Step 4: Complete checkout - creates order and Stripe payment intent
     * Returns clientSecret for frontend Stripe.js integration
     */
    @PostMapping("/{sessionId}/complete")
    @Operation(summary = "Complete checkout", description = "Finalize checkout, create order and Stripe payment intent")
    public ApiResponse<CompleteCheckoutResponse> completeCheckout(
            @PathVariable Long sessionId,
            @Valid @RequestBody(required = false) CompleteCheckoutRequest request,
            Authentication authentication) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        log.info("Completing checkout session {} for user {}", sessionId, userId);
        
        // Default to Stripe if no payment method specified
        String paymentMethod = (request != null && request.getPaymentMethod() != null) 
                ? request.getPaymentMethod() 
                : "stripe";
        
        CompleteCheckoutResponse response = checkoutService.completeCheckout(userId, sessionId, paymentMethod);
        
        return ApiResponse.<CompleteCheckoutResponse>builder()
                .message("Checkout completed - proceed to payment")
                .result(response)
                .build();
    }

    // ============ Legacy endpoint for backward compatibility ============
    
    /**
     * @deprecated Use POST /checkout/session instead. This endpoint will return 410 GONE in future versions.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    @PostMapping
    @Operation(summary = "Create checkout (legacy)", description = "Legacy endpoint - use POST /checkout/session instead", deprecated = true)
    public ApiResponse<CheckoutResponse> createCheckoutSession(
            @RequestBody CreateCheckoutRequest request,
            Authentication authentication) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        CheckoutResponse checkoutResponse = checkoutService.createCheckoutSession(userId, request);
        return ApiResponse.<CheckoutResponse>builder()
                .message("Checkout session created successfully")
                .result(checkoutResponse)
                .build();
    }
}
