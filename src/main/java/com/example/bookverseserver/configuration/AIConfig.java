package com.example.bookverseserver.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for AI/LLM integration.
 * 
 * Multi-Provider Strategy (The Cunning Budget-Free AI):
 * We support 7 providers with intelligent rotation and fallback.
 * Priority order by free tier generosity:
 * 
 * 1. Groq - 100+ RPM (fastest, most generous)
 * 2. Mistral - 1B tokens/month
 * 3. OpenRouter - Multiple free models
 * 4. HuggingFace - 300/hour
 * 5. Fireworks - Limited free tier
 * 6. Cohere - 1000 reqs/month
 * 7. Gemini - Backup (limited free tier)
 */
@Configuration
@ConfigurationProperties(prefix = "app.ai")
@Data
public class AIConfig {
    
    /**
     * Whether AI features are enabled
     */
    private boolean enabled = false;
    
    // ═══════════════════════════════════════════════════════════════════════
    // PRIMARY PROVIDERS (High Free Tier Limits)
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Groq API key - 100+ RPM, fastest inference
     * Get free key at: https://console.groq.com
     */
    private String groqApiKey;
    
    /**
     * Mistral AI API key - 1B tokens/month free
     * Get free key at: https://console.mistral.ai
     */
    private String mistralApiKey;
    
    /**
     * OpenRouter API key - Access to multiple free models
     * Get free key at: https://openrouter.ai
     */
    private String openrouterApiKey;
    
    // ═══════════════════════════════════════════════════════════════════════
    // SECONDARY PROVIDERS (Lower Limits, Good Fallbacks)
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * HuggingFace API key - 300/hour free
     * Get free key at: https://huggingface.co/settings/tokens
     */
    private String huggingfaceApiKey;
    
    /**
     * Fireworks AI API key - Fast inference
     * Get free key at: https://fireworks.ai
     */
    private String fireworksApiKey;
    
    /**
     * Cohere API key - 1000 reqs/month free
     * Get free key at: https://dashboard.cohere.com
     */
    private String cohereApiKey;
    
    // ═══════════════════════════════════════════════════════════════════════
    // BACKUP PROVIDER (Very Limited Free Tier - Last Resort)
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Google Gemini API key - 15 RPM / 1500 RPD free
     * Get free key at: https://aistudio.google.com
     */
    private String geminiApiKey;
    
    // ═══════════════════════════════════════════════════════════════════════
    // GENERATION SETTINGS
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Max tokens for AI responses
     */
    private int maxTokens = 2048;
    
    /**
     * Temperature for AI responses (0.0 = deterministic, 1.0 = creative)
     */
    private double temperature = 0.7;
    
    /**
     * Timeout for AI API calls in seconds
     */
    private int timeoutSeconds = 30;
    
    /**
     * Max retry attempts across all providers
     */
    private int maxRetries = 3;
    
    // ═══════════════════════════════════════════════════════════════════════
    // LEGACY (kept for backward compatibility)
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Default model (used by OpenRouter)
     * @deprecated Use provider-specific models instead
     */
    private String defaultModel = "google/gemini-2.0-flash-exp:free";
    
    /**
     * OpenAI API key (not free, but supported)
     */
    private String openaiApiKey;
    
    /**
     * Check if any AI provider is configured
     */
    public boolean hasAnyProvider() {
        return isNotBlank(groqApiKey) ||
               isNotBlank(mistralApiKey) ||
               isNotBlank(openrouterApiKey) ||
               isNotBlank(huggingfaceApiKey) ||
               isNotBlank(fireworksApiKey) ||
               isNotBlank(cohereApiKey) ||
               isNotBlank(geminiApiKey);
    }
    
    private boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }
}
