package com.example.bookverseserver.configuration;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting filter for auth endpoints to prevent brute force attacks.
 * 
 * Limits:
 * - Login: 5 attempts per minute per IP
 * - Registration: 3 attempts per minute per IP
 * - Password reset: 3 attempts per minute per IP
 * - OTP verification: 5 attempts per minute per IP
 */
@Component
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    // Per-IP buckets for different endpoints
    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> registrationBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> passwordResetBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> otpBuckets = new ConcurrentHashMap<>();

    // Rate limit configurations
    private static final int LOGIN_REQUESTS_PER_MINUTE = 5;
    private static final int REGISTRATION_REQUESTS_PER_MINUTE = 3;
    private static final int PASSWORD_RESET_REQUESTS_PER_MINUTE = 3;
    private static final int OTP_REQUESTS_PER_MINUTE = 5;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        // Only rate limit POST requests to auth endpoints
        if (!"POST".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        Bucket bucket = null;
        String endpointType = null;

        // Check which endpoint is being accessed
        if (path.contains("/auth/login") || path.contains("/auth/token")) {
            bucket = loginBuckets.computeIfAbsent(clientIp, k -> createBucket(LOGIN_REQUESTS_PER_MINUTE));
            endpointType = "login";
        } else if (path.contains("/auth/register") || path.contains("/signup")) {
            bucket = registrationBuckets.computeIfAbsent(clientIp, k -> createBucket(REGISTRATION_REQUESTS_PER_MINUTE));
            endpointType = "registration";
        } else if (path.contains("/auth/forgot-password") || path.contains("/auth/reset-password")) {
            bucket = passwordResetBuckets.computeIfAbsent(clientIp, k -> createBucket(PASSWORD_RESET_REQUESTS_PER_MINUTE));
            endpointType = "password-reset";
        } else if (path.contains("/otp/verify") || path.contains("/otp/resend")) {
            bucket = otpBuckets.computeIfAbsent(clientIp, k -> createBucket(OTP_REQUESTS_PER_MINUTE));
            endpointType = "otp";
        }

        // If no rate limit applies to this endpoint, continue
        if (bucket == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Try to consume a token
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for {} from IP: {} on endpoint: {}", endpointType, clientIp, path);
            
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("""
                {
                    "code": 429,
                    "message": "Too many requests. Please try again later.",
                    "result": null
                }
            """);
        }
    }

    private Bucket createBucket(int requestsPerMinute) {
        Bandwidth limit = Bandwidth.classic(requestsPerMinute, Refill.greedy(requestsPerMinute, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Only apply to auth-related paths
        return !path.contains("/auth/") && !path.contains("/otp/") && !path.contains("/signup");
    }
}
