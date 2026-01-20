package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.repository.UserRepository;
import com.example.bookverseserver.service.StripeConnectService;
import com.example.bookverseserver.service.SubscriptionService;
import com.example.bookverseserver.util.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Subscription Controller
 * 
 * Manages PRO Seller subscriptions and Stripe Connect seller onboarding.
 * Requires SELLER or PRO_SELLER role.
 */
@RestController
@RequestMapping("/api/seller/subscription")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SubscriptionController {

    SubscriptionService subscriptionService;
    StripeConnectService stripeConnectService;
    UserRepository userRepository;
    SecurityUtils securityUtils;

    /**
     * Get current subscription status
     */
    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('SELLER', 'PRO_SELLER')")
    public ApiResponse<Map<String, Object>> getSubscriptionStatus(Authentication authentication) {
        User seller = getCurrentUser(authentication);
        Map<String, Object> status = subscriptionService.getSubscriptionStatus(seller);
        
        return ApiResponse.<Map<String, Object>>builder()
                .result(status)
                .build();
    }

    /**
     * Create checkout session to upgrade to PRO
     * Returns the Stripe Checkout URL
     */
    @PostMapping("/upgrade")
    @PreAuthorize("hasAnyRole('SELLER', 'PRO_SELLER')")
    public ApiResponse<Map<String, String>> upgradeToProSeller(Authentication authentication) {
        User seller = getCurrentUser(authentication);
        
        // Check if already PRO
        if (seller.getUserProfile() != null && 
            "PRO_SELLER".equals(seller.getUserProfile().getAccountType())) {
            throw new AppException(ErrorCode.ALREADY_PRO_SELLER);
        }
        
        String checkoutUrl = subscriptionService.createSubscriptionCheckout(seller);
        
        return ApiResponse.<Map<String, String>>builder()
                .result(Map.of("checkoutUrl", checkoutUrl))
                .build();
    }

    /**
     * Cancel PRO subscription (will downgrade at period end)
     */
    @PostMapping("/cancel")
    @PreAuthorize("hasRole('PRO_SELLER')")
    public ApiResponse<Map<String, Object>> cancelSubscription(Authentication authentication) {
        User seller = getCurrentUser(authentication);
        boolean cancelled = subscriptionService.cancelSubscription(seller);
        
        return ApiResponse.<Map<String, Object>>builder()
                .result(Map.of(
                        "success", cancelled,
                        "message", cancelled ? "Subscription will be cancelled at the end of the billing period" : "Failed to cancel subscription"
                ))
                .build();
    }

    /**
     * Resume cancelled subscription (if still in grace period)
     */
    @PostMapping("/resume")
    @PreAuthorize("hasAnyRole('SELLER', 'PRO_SELLER')")
    public ApiResponse<Map<String, Object>> resumeSubscription(Authentication authentication) {
        User seller = getCurrentUser(authentication);
        boolean resumed = subscriptionService.resumeSubscription(seller);
        
        return ApiResponse.<Map<String, Object>>builder()
                .result(Map.of(
                        "success", resumed,
                        "message", resumed ? "Subscription resumed successfully" : "Failed to resume subscription"
                ))
                .build();
    }

    /**
     * Get billing portal URL for subscription management
     */
    @GetMapping("/billing-portal")
    @PreAuthorize("hasAnyRole('SELLER', 'PRO_SELLER')")
    public ApiResponse<Map<String, String>> getBillingPortal(Authentication authentication) {
        User seller = getCurrentUser(authentication);
        String portalUrl = subscriptionService.createBillingPortalSession(seller);
        
        return ApiResponse.<Map<String, String>>builder()
                .result(Map.of("portalUrl", portalUrl != null ? portalUrl : ""))
                .build();
    }

    // ==================== Stripe Connect (Payouts) ====================

    /**
     * Get Connect account status
     */
    @GetMapping("/connect/status")
    @PreAuthorize("hasAnyRole('SELLER', 'PRO_SELLER')")
    public ApiResponse<Map<String, Object>> getConnectStatus(Authentication authentication) {
        User seller = getCurrentUser(authentication);
        String accountId = seller.getUserProfile() != null ? 
                seller.getUserProfile().getStripeAccountId() : null;
        
        Map<String, Object> status = stripeConnectService.getAccountStatus(accountId);
        
        return ApiResponse.<Map<String, Object>>builder()
                .result(status)
                .build();
    }

    /**
     * Start Connect onboarding (get onboarding URL)
     */
    @PostMapping("/connect/onboard")
    @PreAuthorize("hasAnyRole('SELLER', 'PRO_SELLER')")
    public ApiResponse<Map<String, String>> startConnectOnboarding(Authentication authentication) {
        User seller = getCurrentUser(authentication);
        String onboardingUrl = stripeConnectService.createConnectAccount(seller);
        
        return ApiResponse.<Map<String, String>>builder()
                .result(Map.of("onboardingUrl", onboardingUrl != null ? onboardingUrl : ""))
                .build();
    }

    /**
     * Get Connect dashboard link
     */
    @GetMapping("/connect/dashboard")
    @PreAuthorize("hasAnyRole('SELLER', 'PRO_SELLER')")
    public ApiResponse<Map<String, String>> getConnectDashboard(Authentication authentication) {
        User seller = getCurrentUser(authentication);
        String accountId = seller.getUserProfile() != null ? 
                seller.getUserProfile().getStripeAccountId() : null;
        
        if (accountId == null) {
            return ApiResponse.<Map<String, String>>builder()
                    .result(Map.of("dashboardUrl", ""))
                    .build();
        }
        
        String dashboardUrl = stripeConnectService.createDashboardLink(accountId);
        
        return ApiResponse.<Map<String, String>>builder()
                .result(Map.of("dashboardUrl", dashboardUrl != null ? dashboardUrl : ""))
                .build();
    }

    /**
     * Get seller's Stripe balance
     */
    @GetMapping("/connect/balance")
    @PreAuthorize("hasAnyRole('SELLER', 'PRO_SELLER')")
    public ApiResponse<Map<String, Object>> getConnectBalance(Authentication authentication) {
        User seller = getCurrentUser(authentication);
        String accountId = seller.getUserProfile() != null ? 
                seller.getUserProfile().getStripeAccountId() : null;
        
        Map<String, Object> balance = stripeConnectService.getSellerBalance(accountId);
        
        return ApiResponse.<Map<String, Object>>builder()
                .result(balance)
                .build();
    }

    // ==================== Helper ====================

    private User getCurrentUser(Authentication authentication) {
        Long userId = securityUtils.getCurrentUserId(authentication);
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
