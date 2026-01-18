package com.example.bookverseserver.service.ai.providers;

import com.example.bookverseserver.service.ai.AbstractChatProvider;
import com.example.bookverseserver.service.ai.AIProviderException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

/**
 * HuggingFace Inference Providers API
 * 
 * Uses the new unified router endpoint (Jan 2026 update):
 * https://router.huggingface.co/v1/chat/completions
 * 
 * Free tier: Generous, auto-routes to available providers (Groq, Together, etc.)
 * Model: Qwen/Qwen2.5-72B-Instruct (well-supported, current)
 * 
 * The new API auto-selects the best provider for the model.
 * Add :fastest or :cheapest suffix to model for routing policy.
 */
public class HuggingFaceProvider extends AbstractChatProvider {
    
    // New unified endpoint - auto-routes to best available provider
    private static final String BASE_URL = "https://router.huggingface.co/v1/chat/completions";
    private static final String DEFAULT_MODEL = "Qwen/Qwen2.5-72B-Instruct";
    private static final int RPM_LIMIT = 60; // Conservative for free tier
    
    public HuggingFaceProvider(String apiKey) {
        super("huggingface", apiKey, DEFAULT_MODEL, RPM_LIMIT, BASE_URL);
    }
    
    public HuggingFaceProvider(String apiKey, String model) {
        super("huggingface", apiKey, model != null ? model : DEFAULT_MODEL, RPM_LIMIT, BASE_URL);
    }
    
    @Override
    protected Map<String, Object> buildRequestBody(String prompt) {
        // Using OpenAI-compatible format for /v1/chat/completions endpoint
        return Map.of(
                "model", model,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "max_tokens", 2048,
                "temperature", 0.7
        );
    }
    
    @Override
    protected String parseResponse(JsonNode response) {
        // OpenAI-compatible format: {"choices": [{"message": {"content": "..."}}]}
        if (response != null && response.has("choices")) {
            JsonNode choices = response.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                return choices.get(0).path("message").path("content").asText();
            }
        }
        throw new RuntimeException("Invalid response format from HuggingFace");
    }
}
