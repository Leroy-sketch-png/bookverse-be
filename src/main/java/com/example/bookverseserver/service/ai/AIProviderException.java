package com.example.bookverseserver.service.ai;

/**
 * Exception thrown when an AI provider call fails.
 */
public class AIProviderException extends RuntimeException {
    
    private final String providerName;
    private final boolean rateLimited;
    private final boolean timeout;
    
    public AIProviderException(String providerName, String message) {
        super(message);
        this.providerName = providerName;
        this.rateLimited = false;
        this.timeout = false;
    }
    
    public AIProviderException(String providerName, String message, boolean rateLimited, boolean timeout) {
        super(message);
        this.providerName = providerName;
        this.rateLimited = rateLimited;
        this.timeout = timeout;
    }
    
    public AIProviderException(String providerName, String message, Throwable cause) {
        super(message, cause);
        this.providerName = providerName;
        this.rateLimited = false;
        this.timeout = false;
    }
    
    public String getProviderName() {
        return providerName;
    }
    
    public boolean isRateLimited() {
        return rateLimited;
    }
    
    public boolean isTimeout() {
        return timeout;
    }
}
