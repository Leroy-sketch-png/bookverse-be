package com.example.bookverseserver.service.ai.providers;

import com.example.bookverseserver.service.ai.AbstractChatProvider;
import org.springframework.http.HttpHeaders;

/**
 * OpenRouter Provider — Access to multiple free models
 * 
 * Free tier: Variable by model
 * Model: google/gemma-2-9b-it:free (reliable, well-supported)
 * 
 * Updated Jan 2026: Removed deepseek-r1:free (discontinued)
 */
public class OpenRouterProvider extends AbstractChatProvider {
    
    private static final String BASE_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String DEFAULT_MODEL = "google/gemma-2-9b-it:free";
    private static final int RPM_LIMIT = 50; // Conservative for free tier
    
    public OpenRouterProvider(String apiKey) {
        super("openrouter", apiKey, DEFAULT_MODEL, RPM_LIMIT, BASE_URL);
    }
    
    public OpenRouterProvider(String apiKey, String model) {
        super("openrouter", apiKey, model != null ? model : DEFAULT_MODEL, RPM_LIMIT, BASE_URL);
    }
    
    @Override
    protected HttpHeaders buildHeaders() {
        HttpHeaders headers = super.buildHeaders();
        headers.set("HTTP-Referer", "https://bookverse.app");
        headers.set("X-Title", "Bookverse AI");
        return headers;
    }
}
