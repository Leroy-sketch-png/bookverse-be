package com.example.bookverseserver.configuration;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting filter to prevent abuse of public endpoints.
 * Uses Bucket4j token bucket algorithm for smooth rate limiting.
 * 
 * Limits by IP address:
 * - General API: 100 requests/minute
 * - Auth endpoints: 20 requests/minute (stricter for login/signup)
 * - Search endpoints: 60 requests/minute
 */
@Component
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    // Configurable via application.properties
    @Value("${app.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${app.rate-limit.general.requests-per-minute:100}")
    private int generalRequestsPerMinute;

    @Value("${app.rate-limit.auth.requests-per-minute:20}")
    private int authRequestsPerMinute;

    @Value("${app.rate-limit.search.requests-per-minute:60}")
    private int searchRequestsPerMinute;

    // IP-based buckets for different endpoint types
    private final Map<String, Bucket> generalBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> authBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> searchBuckets = new ConcurrentHashMap<>();

    // Cleanup old buckets periodically (every 1000 requests)
    private int requestCounter = 0;
    private static final int CLEANUP_THRESHOLD = 1000;
    private static final int MAX_BUCKET_AGE_MINUTES = 10;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        
        // Skip rate limiting if disabled
        if (!rateLimitEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        // Skip for internal/health endpoints
        String path = request.getRequestURI();
        if (isExcludedPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        Bucket bucket = resolveBucket(clientIp, path);

        // Try to consume a token
        if (bucket.tryConsume(1)) {
            // Add rate limit headers for transparency
            response.addHeader("X-RateLimit-Remaining", 
                String.valueOf(bucket.getAvailableTokens()));
            
            filterChain.doFilter(request, response);
            
            // Periodic cleanup
            if (++requestCounter >= CLEANUP_THRESHOLD) {
                requestCounter = 0;
                cleanupOldBuckets();
            }
        } else {
            // Rate limit exceeded
            log.warn("Rate limit exceeded for IP: {} on path: {}", clientIp, path);
            
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("""
                {
                    "code": 429,
                    "message": "Too many requests. Please slow down.",
                    "result": null
                }
                """);
        }
    }

    private boolean isExcludedPath(String path) {
        return path.startsWith("/v3/api-docs") ||
               path.startsWith("/swagger-ui") ||
               path.equals("/error") ||
               path.startsWith("/actuator");
    }

    private Bucket resolveBucket(String clientIp, String path) {
        if (isAuthPath(path)) {
            return authBuckets.computeIfAbsent(clientIp, this::createAuthBucket);
        } else if (isSearchPath(path)) {
            return searchBuckets.computeIfAbsent(clientIp, this::createSearchBucket);
        } else {
            return generalBuckets.computeIfAbsent(clientIp, this::createGeneralBucket);
        }
    }

    private boolean isAuthPath(String path) {
        return path.startsWith("/api/auth/") ||
               path.contains("/login") ||
               path.contains("/signup") ||
               path.contains("/otp");
    }

    private boolean isSearchPath(String path) {
        return path.contains("search") ||
               (path.startsWith("/api/listings") && path.contains("?")) ||
               (path.startsWith("/api/books") && path.contains("?"));
    }

    private Bucket createGeneralBucket(String key) {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(generalRequestsPerMinute, 
                    Refill.greedy(generalRequestsPerMinute, Duration.ofMinutes(1))))
                .build();
    }

    private Bucket createAuthBucket(String key) {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(authRequestsPerMinute, 
                    Refill.greedy(authRequestsPerMinute, Duration.ofMinutes(1))))
                .build();
    }

    private Bucket createSearchBucket(String key) {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(searchRequestsPerMinute, 
                    Refill.greedy(searchRequestsPerMinute, Duration.ofMinutes(1))))
                .build();
    }

    private String getClientIp(HttpServletRequest request) {
        // Check common proxy headers
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Take the first IP in the chain (original client)
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    private void cleanupOldBuckets() {
        // Simple cleanup: if bucket count exceeds threshold, clear oldest half
        // This is a simple approach; production might want TTL-based expiry
        int maxBuckets = 10000;
        
        if (generalBuckets.size() > maxBuckets) {
            log.info("Cleaning up {} general rate limit buckets", generalBuckets.size() / 2);
            generalBuckets.clear();
        }
        if (authBuckets.size() > maxBuckets) {
            log.info("Cleaning up {} auth rate limit buckets", authBuckets.size() / 2);
            authBuckets.clear();
        }
        if (searchBuckets.size() > maxBuckets) {
            log.info("Cleaning up {} search rate limit buckets", searchBuckets.size() / 2);
            searchBuckets.clear();
        }
    }
}
