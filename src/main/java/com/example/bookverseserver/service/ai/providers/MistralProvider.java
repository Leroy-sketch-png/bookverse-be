package com.example.bookverseserver.service.ai.providers;

import com.example.bookverseserver.service.ai.AbstractChatProvider;
import org.springframework.http.HttpHeaders;

/**
 * Mistral AI Provider — European excellence
 * 
 * Free tier: 1 billion tokens/month
 * Model: mistral-small-latest
 */
public class MistralProvider extends AbstractChatProvider {
    
    private static final String BASE_URL = "https://api.mistral.ai/v1/chat/completions";
    private static final String DEFAULT_MODEL = "mistral-small-latest";
    private static final int RPM_LIMIT = 60;
    
    public MistralProvider(String apiKey) {
        super("mistral", apiKey, DEFAULT_MODEL, RPM_LIMIT, BASE_URL);
    }
    
    public MistralProvider(String apiKey, String model) {
        super("mistral", apiKey, model != null ? model : DEFAULT_MODEL, RPM_LIMIT, BASE_URL);
    }
}
