package com.example.bookverseserver.service.ai;

/**
 * Abstract base for AI providers.
 * Each provider implements this interface with their specific API integration.
 */
public interface AIProvider {
    
    /**
     * @return Provider name for logging
     */
    String getName();
    
    /**
     * @return Model identifier
     */
    String getModel();
    
    /**
     * @return Requests per minute limit
     */
    int getRpmLimit();
    
    /**
     * @return Whether this provider is enabled (has API key)
     */
    boolean isEnabled();
    
    /**
     * @return Current availability status
     */
    ProviderStatus getStatus();
    
    /**
     * @return true if available for a call right now
     */
    boolean isAvailable();
    
    /**
     * Generate a response from the AI provider.
     * 
     * @param prompt The prompt to send
     * @param timeoutSeconds Timeout for this call
     * @return Generated text
     * @throws AIProviderException if call fails
     */
    String generate(String prompt, int timeoutSeconds) throws AIProviderException;
    
    /**
     * Get usage statistics for this provider
     */
    ProviderStats getStats();
    
    /**
     * Put provider on cooldown (after rate limit hit)
     */
    void setCooldown(int seconds);
}
