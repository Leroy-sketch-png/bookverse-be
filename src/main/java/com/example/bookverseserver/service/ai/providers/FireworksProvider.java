package com.example.bookverseserver.service.ai.providers;

import com.example.bookverseserver.service.ai.AbstractChatProvider;

import java.util.List;
import java.util.Map;

/**
 * Fireworks AI Provider — Fast inference
 * 
 * Free tier: Limited but useful as backup
 * Model: llama-v3p3-70b-instruct (latest Llama 3.3)
 * 
 * Updated Jan 2026: Use latest Llama model
 */
public class FireworksProvider extends AbstractChatProvider {
    
    private static final String BASE_URL = "https://api.fireworks.ai/inference/v1/chat/completions";
    private static final String DEFAULT_MODEL = "accounts/fireworks/models/llama-v3p3-70b-instruct";
    private static final int RPM_LIMIT = 20; // Conservative
    
    public FireworksProvider(String apiKey) {
        super("fireworks", apiKey, DEFAULT_MODEL, RPM_LIMIT, BASE_URL);
    }
    
    public FireworksProvider(String apiKey, String model) {
        super("fireworks", apiKey, model != null ? model : DEFAULT_MODEL, RPM_LIMIT, BASE_URL);
    }
    
    @Override
    protected Map<String, Object> buildRequestBody(String prompt) {
        return Map.of(
                "model", model,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "max_tokens", 4096
        );
    }
}
