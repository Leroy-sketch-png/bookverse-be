package com.example.bookverseserver.scheduled;

import com.example.bookverseserver.repository.CheckoutSessionRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Scheduled task to clean up expired/abandoned checkout sessions.
 * Prevents database bloat from incomplete checkouts.
 * Runs daily, deletes sessions older than 7 days that were never completed.
 */
@Component
public class CheckoutSessionCleanupTask {
    private static final Logger log = LoggerFactory.getLogger(CheckoutSessionCleanupTask.class);
    
    private final CheckoutSessionRepository checkoutSessionRepository;
    
    // Default: 7 days retention before cleanup
    private static final int RETENTION_DAYS = 7;

    public CheckoutSessionCleanupTask(CheckoutSessionRepository checkoutSessionRepository) {
        this.checkoutSessionRepository = checkoutSessionRepository;
    }

    /**
     * Clean up expired checkout sessions daily.
     * Default: runs every 24 hours (86400000 ms).
     * Configurable via app.checkout.cleanup-millis property.
     */
    @Scheduled(fixedDelayString = "${app.checkout.cleanup-millis:86400000}")
    @Transactional
    public void cleanupExpiredSessions() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(RETENTION_DAYS);
        int deleted = checkoutSessionRepository.deleteExpiredSessions(cutoffDate);
        if (deleted > 0) {
            log.info("Cleaned up {} expired checkout sessions (older than {} days)", deleted, RETENTION_DAYS);
        }
    }
}
