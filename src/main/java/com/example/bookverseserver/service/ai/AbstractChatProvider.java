package com.example.bookverseserver.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class for AI providers using OpenAI-compatible chat completion APIs.
 * Most modern LLM providers use this format.
 */
@Slf4j
public abstract class AbstractChatProvider implements AIProvider {
    
    protected final String name;
    protected final String apiKey;
    protected final String model;
    protected final int rpmLimit;
    protected final String baseUrl;
    protected final ProviderStats stats;
    protected final ObjectMapper objectMapper;
    protected final RestTemplate restTemplate;
    
    protected AbstractChatProvider(String name, String apiKey, String model, int rpmLimit, String baseUrl) {
        this.name = name;
        this.apiKey = apiKey;
        this.model = model;
        this.rpmLimit = rpmLimit;
        this.baseUrl = baseUrl;
        this.stats = new ProviderStats();
        this.objectMapper = new ObjectMapper();
        this.restTemplate = new RestTemplate();
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getModel() {
        return model;
    }
    
    @Override
    public int getRpmLimit() {
        return rpmLimit;
    }
    
    @Override
    public boolean isEnabled() {
        return apiKey != null && !apiKey.isBlank();
    }
    
    @Override
    public ProviderStatus getStatus() {
        if (!isEnabled()) {
            return ProviderStatus.DISABLED;
        }
        if (stats.isOnCooldown()) {
            return ProviderStatus.RATE_LIMITED;
        }
        if (stats.callsInLastMinute() >= rpmLimit) {
            return ProviderStatus.RATE_LIMITED;
        }
        return ProviderStatus.AVAILABLE;
    }
    
    @Override
    public boolean isAvailable() {
        return getStatus() == ProviderStatus.AVAILABLE;
    }
    
    @Override
    public ProviderStats getStats() {
        return stats;
    }
    
    @Override
    public void setCooldown(int seconds) {
        stats.setCooldown(seconds);
    }
    
    @Override
    public String generate(String prompt, int timeoutSeconds) throws AIProviderException {
        if (!isEnabled()) {
            throw new AIProviderException(name, "Provider not enabled (missing API key)");
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
            
            log.debug("[{}] Generated {} chars", name, result.length());
            return result;
            
        } catch (HttpClientErrorException e) {
            int statusCode = e.getStatusCode().value();
            String errorMsg = String.format("HTTP %d: %s", statusCode, e.getMessage());
            stats.recordCall(false, errorMsg);
            
            if (statusCode == 429) {
                setCooldown(60); // Rate limited - 1 minute cooldown
                throw new AIProviderException(name, "Rate limited", true, false);
            }
            
            throw new AIProviderException(name, errorMsg, e);
            
        } catch (HttpServerErrorException e) {
            String errorMsg = String.format("HTTP %d: %s", e.getStatusCode().value(), e.getMessage());
            stats.recordCall(false, errorMsg);
            setCooldown(30); // Server error - shorter cooldown
            throw new AIProviderException(name, errorMsg, e);
            
        } catch (ResourceAccessException e) {
            stats.recordCall(false, "Timeout");
            throw new AIProviderException(name, "Timeout", false, true);
            
        } catch (Exception e) {
            stats.recordCall(false, e.getMessage());
            throw new AIProviderException(name, e.getMessage(), e);
        }
    }
    
    /**
     * Build HTTP headers for the request. Override for provider-specific headers.
     */
    protected HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        return headers;
    }
    
    /**
     * Build the request body. Override for provider-specific format.
     */
    protected Map<String, Object> buildRequestBody(String prompt) {
        return Map.of(
                "model", model,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.7,
                "max_tokens", 2048
        );
    }
    
    /**
     * Parse the response. Override for provider-specific response format.
     */
    protected String parseResponse(JsonNode response) {
        if (response != null && response.has("choices")) {
            return response.path("choices").get(0)
                    .path("message")
                    .path("content").asText();
        }
        throw new RuntimeException("Invalid response format from " + name);
    }
}
