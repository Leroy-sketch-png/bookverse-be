package com.example.bookverseserver.configuration;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

    @Value("${stripe.api.key}")
    private String apiKey;

    @PostConstruct
    public void init() {
        // Khởi tạo Stripe API Key khi ứng dụng start
        Stripe.apiKey = apiKey;
    }
}