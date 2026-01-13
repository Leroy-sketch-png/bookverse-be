package com.example.bookverseserver.service.ai;

import lombok.Data;

import java.time.Instant;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Track provider usage and health.
 * Thread-safe for concurrent access.
 */
@Data
public class ProviderStats {
    
    private int callsMade = 0;
    private int callsSucceeded = 0;
    private int callsFailed = 0;
    private Instant lastCallTime;
    private String lastError;
    private Instant lastErrorTime;
    private Instant cooldownUntil;
    
    // Sliding window for rate limit tracking (last 100 calls)
    private final Deque<Instant> recentCalls = new ConcurrentLinkedDeque<>();
    private static final int MAX_RECENT_CALLS = 100;
    
    /**
     * Record a call attempt
     */
    public synchronized void recordCall(boolean success, String error) {
        callsMade++;
        lastCallTime = Instant.now();
        recentCalls.addLast(lastCallTime);
        
        // Keep deque bounded
        while (recentCalls.size() > MAX_RECENT_CALLS) {
            recentCalls.removeFirst();
        }
        
        if (success) {
            callsSucceeded++;
        } else {
            callsFailed++;
            lastError = error;
            lastErrorTime = Instant.now();
        }
    }
    
    /**
     * Put provider on cooldown
     */
    public void setCooldown(int seconds) {
        this.cooldownUntil = Instant.now().plusSeconds(seconds);
    }
    
    /**
     * Check if provider is on cooldown
     */
    public boolean isOnCooldown() {
        if (cooldownUntil == null) {
            return false;
        }
        boolean onCooldown = Instant.now().isBefore(cooldownUntil);
        if (!onCooldown) {
            cooldownUntil = null; // Clear expired cooldown
        }
        return onCooldown;
    }
    
    /**
     * Count calls in the last minute for rate limiting
     */
    public int callsInLastMinute() {
        Instant cutoff = Instant.now().minusSeconds(60);
        return (int) recentCalls.stream()
                .filter(t -> t.isAfter(cutoff))
                .count();
    }
    
    /**
     * Get success rate as percentage
     */
    public double getSuccessRate() {
        if (callsMade == 0) return 100.0;
        return (double) callsSucceeded / callsMade * 100;
    }
}
