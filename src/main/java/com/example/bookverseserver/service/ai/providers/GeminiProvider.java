package com.example.bookverseserver.service.ai.providers;

import com.example.bookverseserver.service.ai.AbstractChatProvider;
import com.example.bookverseserver.service.ai.AIProviderException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;
import java.util.Map;

/**
 * Google Gemini Provider — Backup with limited free tier
 * 
 * Free tier: 15 RPM / 1500 RPD (strictly enforced)
 * Model: gemini-2.0-flash (stable, production-ready)
 * 
 * Note: Uses Google's native API format, not OpenAI-compatible
 * Updated Jan 2026: Using stable flash model instead of -exp variant
 */
public class GeminiProvider extends AbstractChatProvider {
    
    private static final String DEFAULT_MODEL = "gemini-2.0-flash";
    private static final int RPM_LIMIT = 3; // Very conservative - last resort backup
    
    public GeminiProvider(String apiKey) {
        super("gemini", apiKey, DEFAULT_MODEL, RPM_LIMIT, buildUrl(DEFAULT_MODEL, apiKey));
    }
    
    public GeminiProvider(String apiKey, String model) {
        super("gemini", apiKey, model != null ? model : DEFAULT_MODEL, RPM_LIMIT, 
              buildUrl(model != null ? model : DEFAULT_MODEL, apiKey));
    }
    
    private static String buildUrl(String model, String apiKey) {
        return String.format(
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
            model, apiKey
        );
    }
    
    @Override
    protected HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // API key is in URL, not header
        return headers;
    }
    
    @Override
    protected Map<String, Object> buildRequestBody(String prompt) {
        return Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                ),
                "generationConfig", Map.of(
                        "temperature", 0.7,
                        "maxOutputTokens", 4096
                )
        );
    }
    
    @Override
    protected String parseResponse(JsonNode response) {
        // Gemini format: {"candidates": [{"content": {"parts": [{"text": "..."}]}}]}
        if (response != null && response.has("candidates")) {
            return response.path("candidates").get(0)
                    .path("content")
                    .path("parts").get(0)
                    .path("text").asText();
        }
        throw new RuntimeException("Invalid response format from Gemini");
    }
    
    @Override
    public String generate(String prompt, int timeoutSeconds) throws AIProviderException {
        // Override because URL includes API key
        if (!isEnabled()) {
            throw new AIProviderException(getName(), "Provider not enabled (missing API key)");
        }
        
        try {
            HttpHeaders headers = buildHeaders();
            Map<String, Object> body = buildRequestBody(prompt);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    baseUrl,
                    HttpMethod.POST,
                    request,
                    JsonNode.class
            );
            
            String result = parseResponse(response.getBody());
            stats.recordCall(true, null);
            
            return result;
            
        } catch (HttpClientErrorException e) {
            int statusCode = e.getStatusCode().value();
            String errorMsg = String.format("HTTP %d: %s", statusCode, e.getMessage());
            stats.recordCall(false, errorMsg);
            
            if (statusCode == 429) {
                setCooldown(120); // Gemini rate limits are strict - 2 min cooldown
                throw new AIProviderException(getName(), "Rate limited", true, false);
            }
            
            throw new AIProviderException(getName(), errorMsg, e);
            
        } catch (HttpServerErrorException e) {
            String errorMsg = String.format("HTTP %d: %s", e.getStatusCode().value(), e.getMessage());
            stats.recordCall(false, errorMsg);
            setCooldown(30);
            throw new AIProviderException(getName(), errorMsg, e);
            
        } catch (ResourceAccessException e) {
            stats.recordCall(false, "Timeout");
            throw new AIProviderException(getName(), "Timeout", false, true);
            
        } catch (Exception e) {
            stats.recordCall(false, e.getMessage());
            throw new AIProviderException(getName(), e.getMessage(), e);
        }
    }
}
