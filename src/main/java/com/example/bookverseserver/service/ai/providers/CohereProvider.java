package com.example.bookverseserver.service.ai.providers;

import com.example.bookverseserver.service.ai.AbstractChatProvider;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Map;

/**
 * Cohere Provider — Command models
 * 
 * Free tier: 1000 requests/month
 * Model: command-r-plus (command-r deprecated Sept 2025)
 * 
 * Updated Jan 2026: Migrated to command-r-plus
 */
public class CohereProvider extends AbstractChatProvider {
    
    private static final String BASE_URL = "https://api.cohere.ai/v2/chat";
    private static final String DEFAULT_MODEL = "command-r-plus";
    private static final int RPM_LIMIT = 20; // 1000/month = ~30/day = be conservative
    
    public CohereProvider(String apiKey) {
        super("cohere", apiKey, DEFAULT_MODEL, RPM_LIMIT, BASE_URL);
    }
    
    public CohereProvider(String apiKey, String model) {
        super("cohere", apiKey, model != null ? model : DEFAULT_MODEL, RPM_LIMIT, BASE_URL);
    }
    
    @Override
    protected Map<String, Object> buildRequestBody(String prompt) {
        return Map.of(
                "model", model,
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );
    }
    
    @Override
    protected String parseResponse(JsonNode response) {
        // Cohere v2 format: {"message": {"content": [{"text": "..."}]}}
        if (response != null && response.has("message")) {
            JsonNode content = response.path("message").path("content");
            if (content.isArray() && content.size() > 0) {
                return content.get(0).path("text").asText();
            }
        }
        throw new RuntimeException("Invalid response format from Cohere");
    }
}
