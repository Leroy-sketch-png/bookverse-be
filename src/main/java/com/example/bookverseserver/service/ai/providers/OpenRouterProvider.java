package com.example.bookverseserver.service.ai.providers;

import com.example.bookverseserver.service.ai.AbstractChatProvider;
import org.springframework.http.HttpHeaders;

/**
 * OpenRouter Provider — Access to multiple free models
 * 
 * Free tier: Variable by model, typically 50-200 RPD for :free models
 * Model: qwen/qwen3-next-80b-a3b-instruct:free (262K context, high quality)
 * 
 * Updated Jan 19 2026: Switched from deepseek-r1:free (404) to Qwen3 80B
 * Alternative free models: nvidia/nemotron-3-nano-30b-a3b:free, mistralai/devstral-2512:free
 */
public class OpenRouterProvider extends AbstractChatProvider {
    
    private static final String BASE_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String DEFAULT_MODEL = "qwen/qwen3-next-80b-a3b-instruct:free";
    private static final int RPM_LIMIT = 20; // Conservative for free tier
    
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
