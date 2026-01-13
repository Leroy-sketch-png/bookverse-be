package com.example.bookverseserver.service.ai.providers;

import com.example.bookverseserver.service.ai.AbstractChatProvider;
import org.springframework.http.HttpHeaders;

import java.util.Map;

/**
 * Groq Provider — Lightning fast inference on Llama models
 * 
 * Free tier: 100+ RPM (most generous!)
 * Model: llama-3.3-70b-versatile
 */
public class GroqProvider extends AbstractChatProvider {
    
    private static final String BASE_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String DEFAULT_MODEL = "llama-3.3-70b-versatile";
    private static final int RPM_LIMIT = 100;
    
    public GroqProvider(String apiKey) {
        super("groq", apiKey, DEFAULT_MODEL, RPM_LIMIT, BASE_URL);
    }
    
    public GroqProvider(String apiKey, String model) {
        super("groq", apiKey, model != null ? model : DEFAULT_MODEL, RPM_LIMIT, BASE_URL);
    }
}
