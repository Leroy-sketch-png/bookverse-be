package com.example.bookverseserver.service;

import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.entity.User.UserProfile;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.repository.UserProfileRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.param.*;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stripe Subscription Service
 * 
 * Manages PRO Seller subscriptions using Stripe Billing.
 * PRO Sellers pay a monthly fee for reduced commission rates and premium features.
 * 
 * Flow:
 * 1. Seller clicks "Upgrade to PRO"
 * 2. Redirect to Stripe Checkout for subscription
 * 3. Webhook confirms subscription active
 * 4. User profile updated to PRO_SELLER
 * 
 * Test Mode: Uses test API keys. No real charges.
 * 
 * @see <a href="https://stripe.com/docs/billing/subscriptions/overview">Stripe Subscriptions</a>
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@Transactional(readOnly = true)  // Default read-only, override for write methods
public class SubscriptionService {

    final UserProfileRepository userProfileRepository;

    @Value("${stripe.api.key}")
    String stripeSecretKey;

    @Value("${stripe.subscription.enabled:false}")
    boolean subscriptionEnabled;

    @Value("${stripe.pro.price.id:price_PLACEHOLDER_REPLACE_ME}")
    String proPriceId;

    @Value("${app.frontend.url:http://localhost:3000}")
    String frontendUrl;

    // PRO Seller monthly price in VND (for display)
    private static final long PRO_MONTHLY_PRICE_VND = 699000; // ~$29.99 USD equivalent

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    /**
     * Create a Stripe Checkout session for PRO subscription
     * 
     * @param seller The seller upgrading to PRO
     * @return Checkout session URL
     */
    @Transactional
    public String createSubscriptionCheckout(User seller) {
        if (!subscriptionEnabled) {
            log.info("[Stripe Subscription DISABLED] Would create checkout for seller {}", seller.getId());
            // In disabled mode, just upgrade the user directly (for testing)
            upgradeToProSeller(seller, "sim_sub_" + System.currentTimeMillis());
            return frontendUrl + "/home/dashboard/seller?tab=analytics&upgraded=true";
        }

        if (proPriceId.contains("PLACEHOLDER")) {
            log.warn("Stripe PRO price ID not configured. Set stripe.pro.price.id in properties.");
            // Fall back to instant upgrade for demo
            upgradeToProSeller(seller, "demo_sub_" + System.currentTimeMillis());
            return frontendUrl + "/home/dashboard/seller?tab=analytics&upgraded=true";
        }

        try {
            // Get or create Stripe customer
            String customerId = getOrCreateCustomer(seller);

            // Create checkout session
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setCustomer(customerId)
                    .setSuccessUrl(frontendUrl + "/home/dashboard/seller?tab=analytics&subscription=success&session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(frontendUrl + "/seller/upgrade-to-pro?cancelled=true")
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setPrice(proPriceId)
                                    .setQuantity(1L)
                                    .build()
                    )
                    .setSubscriptionData(
                            SessionCreateParams.SubscriptionData.builder()
                                    .putMetadata("seller_id", seller.getId().toString())
                                    .build()
                    )
                    .putMetadata("seller_id", seller.getId().toString())
                    .build();

            Session session = Session.create(params);
            log.info("Created subscription checkout session {} for seller {}", session.getId(), seller.getId());

            return session.getUrl();

        } catch (StripeException e) {
            log.error("Error creating subscription checkout: {}", e.getMessage());
            throw new AppException(ErrorCode.STRIPE_SUBSCRIPTION_ERROR);
        }
    }

    /**
     * Get or create a Stripe customer for the seller
     */
    private String getOrCreateCustomer(User seller) throws StripeException {
        UserProfile profile = seller.getUserProfile();
        
        // Check if customer already exists
        if (profile != null && profile.getStripeCustomerId() != null) {
            return profile.getStripeCustomerId();
        }

        // Create new customer
        CustomerCreateParams params = CustomerCreateParams.builder()
                .setEmail(seller.getEmail())
                .setName(profile != null ? profile.getDisplayName() : seller.getUsername())
                .putMetadata("user_id", seller.getId().toString())
                .build();

        Customer customer = Customer.create(params);
        
        // Save customer ID
        if (profile == null) {
            profile = new UserProfile();
            profile.setUser(seller);
        }
        profile.setStripeCustomerId(customer.getId());
        userProfileRepository.save(profile);

        log.info("Created Stripe customer {} for seller {}", customer.getId(), seller.getId());
        return customer.getId();
    }

    /**
     * Handle successful subscription (called from webhook)
     */
    @Transactional
    public void handleSubscriptionCreated(Subscription subscription) {
        String customerId = subscription.getCustomer();
        
        try {
            Customer customer = Customer.retrieve(customerId);
            String sellerId = customer.getMetadata().get("user_id");
            
            if (sellerId == null) {
                // Try to find by metadata on subscription
                sellerId = subscription.getMetadata().get("seller_id");
            }

            if (sellerId != null) {
                UserProfile profile = userProfileRepository.findByUserId(Long.parseLong(sellerId))
                        .orElse(null);
                
                if (profile != null) {
                    profile.setAccountType("PRO_SELLER");
                    profile.setStripeSubscriptionId(subscription.getId());
                    profile.setSubscriptionStatus(subscription.getStatus());
                    userProfileRepository.save(profile);
                    
                    log.info("Upgraded seller {} to PRO via subscription {}", sellerId, subscription.getId());
                }
            }

        } catch (StripeException e) {
            log.error("Error handling subscription created: {}", e.getMessage());
        }
    }

    /**
     * Handle subscription cancelled/expired
     */
    @Transactional
    public void handleSubscriptionEnded(Subscription subscription) {
        try {
            String subscriptionId = subscription.getId();
            
            UserProfile profile = userProfileRepository.findByStripeSubscriptionId(subscriptionId)
                    .orElse(null);
            
            if (profile != null) {
                profile.setAccountType("CASUAL_SELLER"); // Downgrade
                profile.setSubscriptionStatus("cancelled");
                userProfileRepository.save(profile);
                
                log.info("Downgraded seller {} from PRO - subscription {} ended", 
                        profile.getUser().getId(), subscriptionId);
            }

        } catch (Exception e) {
            log.error("Error handling subscription ended: {}", e.getMessage());
        }
    }

    /**
     * Cancel subscription
     */
    @Transactional
    public boolean cancelSubscription(User seller) {
        UserProfile profile = seller.getUserProfile();
        
        if (profile == null || profile.getStripeSubscriptionId() == null) {
            return false;
        }

        if (!subscriptionEnabled) {
            // Simulate cancellation
            profile.setAccountType("CASUAL_SELLER");
            profile.setSubscriptionStatus("cancelled");
            userProfileRepository.save(profile);
            return true;
        }

        try {
            Subscription subscription = Subscription.retrieve(profile.getStripeSubscriptionId());
            
            // Cancel at period end (don't charge again, but access continues until period ends)
            SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                    .setCancelAtPeriodEnd(true)
                    .build();
            
            subscription.update(params);
            
            profile.setSubscriptionStatus("canceling");
            userProfileRepository.save(profile);
            
            log.info("Scheduled cancellation for subscription {} seller {}", 
                    profile.getStripeSubscriptionId(), seller.getId());
            return true;

        } catch (StripeException e) {
            log.error("Error cancelling subscription: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Resume cancelled subscription (if still in grace period)
     */
    @Transactional
    public boolean resumeSubscription(User seller) {
        UserProfile profile = seller.getUserProfile();
        
        if (profile == null || profile.getStripeSubscriptionId() == null) {
            return false;
        }

        if (!subscriptionEnabled) {
            profile.setAccountType("PRO_SELLER");
            profile.setSubscriptionStatus("active");
            userProfileRepository.save(profile);
            return true;
        }

        try {
            Subscription subscription = Subscription.retrieve(profile.getStripeSubscriptionId());
            
            SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                    .setCancelAtPeriodEnd(false)
                    .build();
            
            subscription.update(params);
            
            profile.setSubscriptionStatus("active");
            userProfileRepository.save(profile);
            
            log.info("Resumed subscription {} for seller {}", 
                    profile.getStripeSubscriptionId(), seller.getId());
            return true;

        } catch (StripeException e) {
            log.error("Error resuming subscription: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get subscription status
     */
    public Map<String, Object> getSubscriptionStatus(User seller) {
        Map<String, Object> status = new HashMap<>();
        UserProfile profile = seller.getUserProfile();

        status.put("isPro", profile != null && "PRO_SELLER".equals(profile.getAccountType()));
        status.put("monthlyPrice", PRO_MONTHLY_PRICE_VND);
        status.put("commissionRate", status.get("isPro").equals(true) ? 3 : 8);

        if (profile == null || profile.getStripeSubscriptionId() == null) {
            status.put("hasSubscription", false);
            return status;
        }

        status.put("hasSubscription", true);
        status.put("subscriptionStatus", profile.getSubscriptionStatus());

        if (!subscriptionEnabled) {
            status.put("currentPeriodEnd", null);
            status.put("cancelAtPeriodEnd", false);
            return status;
        }

        try {
            Subscription subscription = Subscription.retrieve(profile.getStripeSubscriptionId());
            status.put("currentPeriodEnd", subscription.getCurrentPeriodEnd());
            status.put("cancelAtPeriodEnd", subscription.getCancelAtPeriodEnd());

        } catch (StripeException e) {
            log.error("Error getting subscription status: {}", e.getMessage());
        }

        return status;
    }

    /**
     * Create Billing Portal session for subscription management
     */
    public String createBillingPortalSession(User seller) {
        UserProfile profile = seller.getUserProfile();
        
        if (profile == null || profile.getStripeCustomerId() == null) {
            return null;
        }

        if (!subscriptionEnabled) {
            return frontendUrl + "/seller/subscription";
        }

        try {
            com.stripe.param.billingportal.SessionCreateParams params = 
                    com.stripe.param.billingportal.SessionCreateParams.builder()
                            .setCustomer(profile.getStripeCustomerId())
                            .setReturnUrl(frontendUrl + "/seller/subscription")
                            .build();

            com.stripe.model.billingportal.Session session = 
                    com.stripe.model.billingportal.Session.create(params);
            
            return session.getUrl();

        } catch (StripeException e) {
            log.error("Error creating billing portal session: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Upgrade user to PRO seller (internal method)
     */
    @Transactional
    public void upgradeToProSeller(User seller, String subscriptionId) {
        UserProfile profile = seller.getUserProfile();
        if (profile == null) {
            profile = new UserProfile();
            profile.setUser(seller);
        }
        
        profile.setAccountType("PRO_SELLER");
        profile.setStripeSubscriptionId(subscriptionId);
        profile.setSubscriptionStatus("active");
        userProfileRepository.save(profile);
        
        log.info("Upgraded seller {} to PRO_SELLER with subscription {}", seller.getId(), subscriptionId);
    }
}
