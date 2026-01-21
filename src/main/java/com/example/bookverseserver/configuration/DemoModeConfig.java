package com.example.bookverseserver.configuration;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Demo Mode Configuration
 * 
 * When DEMO_MODE=true, the application simulates all external integrations:
 * - Stripe payments: Instant success, fake payment intents
 * - GHN shipping: Fixed rates, fake tracking numbers
 * - SMS/Email: Logged only, not sent
 * 
 * This allows offline demos with full lifecycle visibility.
 */
@Configuration
@Getter
@Slf4j
public class DemoModeConfig {

    @Value("${demo.mode:false}")
    private boolean enabled;

    @PostConstruct
    public void logDemoMode() {
        if (enabled) {
            log.warn("═══════════════════════════════════════════════════════════════");
            log.warn("  🎓 DEMO MODE ENABLED - All external integrations simulated   ");
            log.warn("  Stripe, GHN, SMS will be bypassed with fake responses        ");
            log.warn("═══════════════════════════════════════════════════════════════");
        }
    }

    /**
     * Generate a fake Stripe-like payment intent ID
     */
    public String generateFakePaymentIntentId() {
        return "pi_demo_" + System.currentTimeMillis() + "_sim";
    }

    /**
     * Generate a fake Stripe-like client secret
     */
    public String generateFakeClientSecret() {
        return "pi_demo_" + System.currentTimeMillis() + "_secret_sim";
    }

    /**
     * Generate a fake GHN tracking number
     */
    public String generateFakeTrackingNumber() {
        return "DEMO" + System.currentTimeMillis();
    }
}
