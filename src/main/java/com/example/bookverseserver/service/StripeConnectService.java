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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Stripe Connect Service
 * 
 * Manages seller accounts and payouts using Stripe Connect (Express).
 * Stripe Connect allows marketplace platforms to:
 * - Onboard sellers with Express accounts
 * - Split payments between platform and sellers
 * - Handle seller payouts automatically
 * 
 * Test Mode: All operations use test API keys.
 * Express Accounts: Sellers manage their own bank details via Stripe-hosted dashboard.
 * 
 * @see <a href="https://stripe.com/docs/connect/express-accounts">Stripe Connect Express</a>
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@Transactional(readOnly = true)  // Default read-only, override for write methods
public class StripeConnectService {

    final UserProfileRepository userProfileRepository;
    final com.example.bookverseserver.configuration.DemoModeConfig demoModeConfig;

    @Value("${stripe.api.key}")
    String stripeSecretKey;

    @Value("${stripe.connect.enabled:false}")
    boolean connectEnabled;

    @Value("${app.frontend.url:http://localhost:3000}")
    String frontendUrl;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    /**
     * Create a Stripe Connect Express account for a seller
     * 
     * @param seller The user becoming a seller
     * @return Onboarding URL for the seller to complete account setup
     */
    @Transactional
    public String createConnectAccount(User seller) {
        // Demo mode: Simulate successful Connect onboarding
        if (demoModeConfig.isEnabled()) {
            log.info("[DEMO MODE] Simulating Stripe Connect onboarding for seller {}", seller.getId());
            UserProfile profile = seller.getUserProfile();
            if (profile == null) {
                profile = new UserProfile();
                profile.setUser(seller);
            }
            // Set fake account ID so payout requests work
            String fakeAccountId = demoModeConfig.generateFakeConnectAccountId();
            profile.setStripeAccountId(fakeAccountId);
            userProfileRepository.save(profile);
            log.info("[DEMO MODE] Created fake Stripe account {} for seller {}", fakeAccountId, seller.getId());
            // Return success page URL instead of Stripe onboarding
            return frontendUrl + "/home/dashboard/seller?tab=settings&payout=success";
        }

        if (!connectEnabled) {
            log.info("[Stripe Connect DISABLED] Would create account for seller {}", seller.getId());
            return null;
        }

        try {
            // Check if seller already has a Connect account
            UserProfile profile = seller.getUserProfile();
            if (profile != null && profile.getStripeAccountId() != null) {
                log.info("Seller {} already has Stripe account: {}", seller.getId(), profile.getStripeAccountId());
                return createAccountLink(profile.getStripeAccountId());
            }

            // Create Express account
            AccountCreateParams params = AccountCreateParams.builder()
                    .setType(AccountCreateParams.Type.EXPRESS)
                    .setCountry("VN") // Vietnam
                    .setEmail(seller.getEmail())
                    .setCapabilities(
                            AccountCreateParams.Capabilities.builder()
                                    .setTransfers(AccountCreateParams.Capabilities.Transfers.builder()
                                            .setRequested(true)
                                            .build())
                                    .build()
                    )
                    .setBusinessProfile(
                            AccountCreateParams.BusinessProfile.builder()
                                    .setMcc("5942") // Book stores
                                    .setName(profile != null ? profile.getDisplayName() : seller.getUsername())
                                    .build()
                    )
                    .putMetadata("seller_id", seller.getId().toString())
                    .putMetadata("platform", "bookverse")
                    .build();

            Account account = Account.create(params);
            
            // Save account ID to seller profile
            if (profile == null) {
                profile = new UserProfile();
                profile.setUser(seller);
            }
            profile.setStripeAccountId(account.getId());
            userProfileRepository.save(profile);

            log.info("Created Stripe Connect account {} for seller {}", account.getId(), seller.getId());

            // Return onboarding link
            return createAccountLink(account.getId());

        } catch (StripeException e) {
            log.error("Error creating Stripe Connect account: {}", e.getMessage());
            throw new AppException(ErrorCode.STRIPE_CONNECT_ERROR);
        }
    }

    /**
     * Create an account onboarding link for seller to complete setup
     */
    public String createAccountLink(String accountId) {
        try {
            AccountLinkCreateParams params = AccountLinkCreateParams.builder()
                    .setAccount(accountId)
                    .setRefreshUrl(frontendUrl + "/home/dashboard/seller?tab=settings&payout=refresh")
                    .setReturnUrl(frontendUrl + "/home/dashboard/seller?tab=settings&payout=success")
                    .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                    .build();

            AccountLink link = AccountLink.create(params);
            return link.getUrl();

        } catch (StripeException e) {
            log.error("Error creating account link: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Create a login link for seller to access their Stripe dashboard
     */
    public String createDashboardLink(String accountId) {
        try {
            LoginLinkCreateOnAccountParams params = LoginLinkCreateOnAccountParams.builder().build();
            LoginLink link = LoginLink.createOnAccount(accountId, params, (com.stripe.net.RequestOptions) null);
            return link.getUrl();

        } catch (StripeException e) {
            log.error("Error creating dashboard link: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get Connect account status
     */
    public Map<String, Object> getAccountStatus(String accountId) {
        Map<String, Object> status = new HashMap<>();
        
        // Demo mode: Return fully connected status if seller has demo account
        if (demoModeConfig.isEnabled() && accountId != null && accountId.startsWith("acct_demo_")) {
            log.info("[DEMO MODE] Returning simulated Connect status for {}", accountId);
            status.put("connected", true);
            status.put("hasAccount", true);
            status.put("accountId", accountId);
            status.put("payoutsEnabled", true);
            status.put("chargesEnabled", true);
            status.put("onboardingComplete", true);
            status.put("detailsSubmitted", true);
            status.put("message", "[DEMO] Bank account connected successfully");
            return status;
        }
        
        if (!connectEnabled || accountId == null) {
            status.put("connected", false);
            status.put("payoutsEnabled", false);
            status.put("message", "Stripe Connect not configured");
            return status;
        }

        try {
            Account account = Account.retrieve(accountId);
            
            status.put("connected", true);
            status.put("accountId", account.getId());
            status.put("payoutsEnabled", account.getPayoutsEnabled());
            status.put("chargesEnabled", account.getChargesEnabled());
            status.put("detailsSubmitted", account.getDetailsSubmitted());
            
            if (!account.getDetailsSubmitted()) {
                status.put("message", "Please complete your account setup");
                status.put("onboardingUrl", createAccountLink(accountId));
            } else if (!account.getPayoutsEnabled()) {
                status.put("message", "Your account is under review");
            } else {
                status.put("message", "Account ready for payouts");
            }

            return status;

        } catch (StripeException e) {
            log.error("Error getting account status: {}", e.getMessage());
            status.put("connected", false);
            status.put("error", e.getMessage());
            return status;
        }
    }

    /**
     * Get seller's Stripe balance
     */
    public Map<String, Object> getSellerBalance(String accountId) {
        Map<String, Object> result = new HashMap<>();
        
        if (!connectEnabled || accountId == null) {
            result.put("available", BigDecimal.ZERO);
            result.put("pending", BigDecimal.ZERO);
            return result;
        }

        try {
            Balance balance = Balance.retrieve(
                    BalanceRetrieveParams.builder().build(),
                    com.stripe.net.RequestOptions.builder()
                            .setStripeAccount(accountId)
                            .build()
            );

            long available = 0;
            long pending = 0;

            for (Balance.Available av : balance.getAvailable()) {
                available += av.getAmount();
            }
            for (Balance.Pending pn : balance.getPending()) {
                pending += pn.getAmount();
            }

            // Convert from cents to VND (no decimals for VND)
            result.put("available", BigDecimal.valueOf(available));
            result.put("pending", BigDecimal.valueOf(pending));
            result.put("currency", "vnd");

            return result;

        } catch (StripeException e) {
            log.error("Error getting seller balance: {}", e.getMessage());
            result.put("available", BigDecimal.ZERO);
            result.put("pending", BigDecimal.ZERO);
            result.put("error", e.getMessage());
            return result;
        }
    }

    /**
     * Transfer funds to a seller's Connect account
     * Called after successful order payment
     * 
     * @param accountId Seller's Stripe Connect account ID
     * @param amount Amount in VND (smallest unit)
     * @param orderNumber Reference order number
     * @return Transfer ID
     */
    public String transferToSeller(String accountId, long amount, String orderNumber) {
        if (!connectEnabled) {
            log.info("[Stripe Connect DISABLED] Would transfer {} to account {}", amount, accountId);
            return "sim_transfer_" + System.currentTimeMillis();
        }

        if (accountId == null) {
            log.warn("Cannot transfer: seller has no Connect account");
            return null;
        }

        try {
            TransferCreateParams params = TransferCreateParams.builder()
                    .setAmount(amount)
                    .setCurrency("vnd")
                    .setDestination(accountId)
                    .setDescription("Payment for order " + orderNumber)
                    .putMetadata("order_number", orderNumber)
                    .build();

            Transfer transfer = Transfer.create(params);
            log.info("Created transfer {} of {} VND to account {}", transfer.getId(), amount, accountId);
            
            return transfer.getId();

        } catch (StripeException e) {
            log.error("Error creating transfer: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Create a payout from seller's Stripe balance to their bank account
     * (Stripe handles this automatically for Express accounts with scheduled payouts)
     */
    public String createPayout(String accountId, long amount) {
        if (!connectEnabled) {
            log.info("[Stripe Connect DISABLED] Would create payout of {} from account {}", amount, accountId);
            return "sim_payout_" + System.currentTimeMillis();
        }

        try {
            PayoutCreateParams params = PayoutCreateParams.builder()
                    .setAmount(amount)
                    .setCurrency("vnd")
                    .build();

            Payout payout = Payout.create(
                    params,
                    com.stripe.net.RequestOptions.builder()
                            .setStripeAccount(accountId)
                            .build()
            );

            log.info("Created payout {} of {} VND from account {}", payout.getId(), amount, accountId);
            return payout.getId();

        } catch (StripeException e) {
            log.error("Error creating payout: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get recent transfers to a seller
     */
    public List<Transfer> getSellerTransfers(String accountId, int limit) {
        if (!connectEnabled || accountId == null) {
            return List.of();
        }

        try {
            TransferListParams params = TransferListParams.builder()
                    .setDestination(accountId)
                    .setLimit((long) limit)
                    .build();

            return Transfer.list(params).getData();

        } catch (StripeException e) {
            log.error("Error listing transfers: {}", e.getMessage());
            return List.of();
        }
    }
}
