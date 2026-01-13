package com.example.bookverseserver.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for AI/LLM integration.
 * 
 * We support multiple providers through OpenRouter or direct API calls.
 * Free tier models available:
 * - google/gemini-2.0-flash-exp:free
 * - meta-llama/llama-3.2-3b-instruct:free
 * - mistralai/mistral-7b-instruct:free
 */
@Configuration
@ConfigurationProperties(prefix = "app.ai")
@Data
public class AIConfig {
    
    /**
     * Whether AI features are enabled
     */
    private boolean enabled = false;
    
    /**
     * OpenRouter API key (supports many models with single key)
     * Get free key at: https://openrouter.ai
     */
    private String openrouterApiKey;
    
    /**
     * Default model to use for AI features
     * Free options:
     * - google/gemini-2.0-flash-exp:free (recommended)
     * - meta-llama/llama-3.2-3b-instruct:free
     * - mistralai/mistral-7b-instruct:free
     */
    private String defaultModel = "google/gemini-2.0-flash-exp:free";
    
    /**
     * Alternative: Direct Google Gemini API key
     */
    private String geminiApiKey;
    
    /**
     * Alternative: Direct OpenAI API key
     */
    private String openaiApiKey;
    
    /**
     * Max tokens for AI responses
     */
    private int maxTokens = 500;
    
    /**
     * Temperature for AI responses (0.0 = deterministic, 1.0 = creative)
     */
    private double temperature = 0.7;
    
    /**
     * Timeout for AI API calls in seconds
     */
    private int timeoutSeconds = 30;
}
