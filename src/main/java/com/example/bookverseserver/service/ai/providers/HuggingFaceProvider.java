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
 * HuggingFace Inference API Provider
 * 
 * Free tier: ~300 requests/hour
 * Model: Qwen/Qwen2.5-72B-Instruct (well-supported, current)
 * 
 * Updated Jan 2026: Phi-3-mini deprecated, using Qwen2.5
 */
public class HuggingFaceProvider extends AbstractChatProvider {
    
    private static final String DEFAULT_MODEL = "Qwen/Qwen2.5-72B-Instruct";
    private static final int RPM_LIMIT = 60; // ~300/hour = 5/min but be safe
    
    public HuggingFaceProvider(String apiKey) {
        super("huggingface", apiKey, DEFAULT_MODEL, RPM_LIMIT, 
              "https://router.huggingface.co/hf-inference/models/" + DEFAULT_MODEL + "/v1/chat/completions");
    }
    
    public HuggingFaceProvider(String apiKey, String model) {
        super("huggingface", apiKey, model != null ? model : DEFAULT_MODEL, RPM_LIMIT,
              "https://router.huggingface.co/hf-inference/models/" + (model != null ? model : DEFAULT_MODEL) + "/v1/chat/completions");
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
