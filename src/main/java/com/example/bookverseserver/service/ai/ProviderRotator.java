package com.example.bookverseserver.service.ai;

import com.example.bookverseserver.service.ai.providers.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Intelligent Provider Rotation with Fallback
 * 
 * The cunning heart of our budget-free AI strategy.
 * Rotates through providers, tracking quotas and falling back gracefully.
 * 
 * Priority order (by free tier generosity):
 * 1. Groq - 100+ RPM (fastest, most generous)
 * 2. Mistral - 1B tokens/month
 * 3. OpenRouter - Variable by model
 * 4. HuggingFace - 300/hour
 * 5. Fireworks - Limited
 * 6. Cohere - 1000/month
 * 7. Gemini - Backup (limited free tier)
 */
@Component
@Slf4j
public class ProviderRotator {
    
    private final List<AIProvider> providers = new ArrayList<>();
    private final AtomicInteger currentIndex = new AtomicInteger(0);
    private boolean initialized = false;
    
    /**
     * Initialize with API keys from configuration
     */
    public void initialize(
            String groqKey,
            String mistralKey,
            String openrouterKey,
            String huggingfaceKey,
            String fireworksKey,
            String cohereKey,
            String geminiKey
    ) {
        providers.clear();
        
        // Priority order: Most generous free tiers first
        if (groqKey != null && !groqKey.isBlank()) {
            providers.add(new GroqProvider(groqKey));
        }
        if (mistralKey != null && !mistralKey.isBlank()) {
            providers.add(new MistralProvider(mistralKey));
        }
        if (openrouterKey != null && !openrouterKey.isBlank()) {
            providers.add(new OpenRouterProvider(openrouterKey));
        }
        if (huggingfaceKey != null && !huggingfaceKey.isBlank()) {
            providers.add(new HuggingFaceProvider(huggingfaceKey));
        }
        if (fireworksKey != null && !fireworksKey.isBlank()) {
            providers.add(new FireworksProvider(fireworksKey));
        }
        if (cohereKey != null && !cohereKey.isBlank()) {
            providers.add(new CohereProvider(cohereKey));
        }
        if (geminiKey != null && !geminiKey.isBlank()) {
            providers.add(new GeminiProvider(geminiKey));
        }
        
        initialized = true;
        
        log.info("🤖 AI Provider Rotator initialized with {} providers: {}", 
                providers.size(),
                providers.stream().map(AIProvider::getName).toList());
    }
    
    /**
     * Check if rotator is ready to use
     */
    public boolean isReady() {
        return initialized && !providers.isEmpty();
    }
    
    /**
     * Get the next available provider using round-robin with fallback
     */
    private AIProvider getNextAvailableProvider() {
        if (providers.isEmpty()) {
            return null;
        }
        
        int startIdx = currentIndex.get();
        
        // Try all providers starting from current index
        for (int i = 0; i < providers.size(); i++) {
            int idx = (startIdx + i) % providers.size();
            AIProvider provider = providers.get(idx);
            
            if (provider.isAvailable()) {
                currentIndex.set((idx + 1) % providers.size());
                return provider;
            }
        }
        
        // All providers exhausted - try to find one anyway
        log.warn("⚠️ All AI providers exhausted, attempting cooldown recovery");
        
        // Return first enabled provider (may hit rate limit but better than nothing)
        for (AIProvider provider : providers) {
            if (provider.isEnabled()) {
                return provider;
            }
        }
        
        return null;
    }
    
    /**
     * Generate a response, rotating through providers with intelligent fallback
     * 
     * @param prompt The prompt to send
     * @param timeoutSeconds Timeout per provider attempt
     * @param maxRetries Maximum total retry attempts across all providers
     * @return Generated text response
     * @throws AIProviderException If all providers fail
     */
    public String generate(String prompt, int timeoutSeconds, int maxRetries) throws AIProviderException {
        if (!isReady()) {
            throw new AIProviderException("rotator", "Provider rotator not initialized or no providers available");
        }
        
        List<String> errors = new ArrayList<>();
        
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            AIProvider provider = getNextAvailableProvider();
            
            if (provider == null) {
                throw new AIProviderException("rotator", 
                        String.format("No AI providers available. Errors: %s", errors));
            }
            
            try {
                log.info("🤖 AI call attempt {}/{} via {} ({})", 
                        attempt + 1, maxRetries, provider.getName(), provider.getModel());
                
                String result = provider.generate(prompt, timeoutSeconds);
                
                log.info("✅ AI call succeeded via {} ({} chars)", 
                        provider.getName(), result.length());
                
                return result;
                
            } catch (AIProviderException e) {
                String errorMsg = String.format("%s: %s", provider.getName(), e.getMessage());
                errors.add(errorMsg);
                
                log.warn("⚠️ AI call failed via {}: {}", provider.getName(), e.getMessage());
                
                if (e.isRateLimited()) {
                    provider.setCooldown(60);
                } else if (e.isTimeout()) {
                    // Don't set cooldown for timeout, just try next
                }
                // Continue to next provider
            }
        }
        
        throw new AIProviderException("rotator", 
                String.format("All AI providers failed after %d attempts. Errors: %s", maxRetries, errors));
    }
    
    /**
     * Generate with default settings
     */
    public String generate(String prompt) throws AIProviderException {
        return generate(prompt, 30, 3);
    }
    
    /**
     * Get status of all providers
     */
    public Map<String, ProviderStatusInfo> getProvidersStatus() {
        Map<String, ProviderStatusInfo> status = new HashMap<>();
        
        for (AIProvider provider : providers) {
            ProviderStats stats = provider.getStats();
            status.put(provider.getName(), new ProviderStatusInfo(
                    provider.getName(),
                    provider.getModel(),
                    provider.getStatus().name(),
                    provider.isAvailable(),
                    stats.getCallsMade(),
                    stats.getCallsSucceeded(),
                    stats.getCallsFailed(),
                    stats.getSuccessRate(),
                    stats.callsInLastMinute(),
                    provider.getRpmLimit()
            ));
        }
        
        return status;
    }
    
    /**
     * Get count of available providers
     */
    public int getAvailableProviderCount() {
        return (int) providers.stream().filter(AIProvider::isAvailable).count();
    }
    
    /**
     * Get total provider count
     */
    public int getTotalProviderCount() {
        return providers.size();
    }
    
    /**
     * Status info record for API responses
     */
    public record ProviderStatusInfo(
            String name,
            String model,
            String status,
            boolean available,
            int callsMade,
            int callsSucceeded,
            int callsFailed,
            double successRate,
            int callsLastMinute,
            int rpmLimit
    ) {}
}
