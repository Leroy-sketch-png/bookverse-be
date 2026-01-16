package com.example.bookverseserver.service;

import com.example.bookverseserver.entity.User.Role;
import com.example.bookverseserver.entity.User.SignupRequest;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.entity.User.UserProfile;
import com.example.bookverseserver.enums.RoleName;
import com.example.bookverseserver.repository.RoleRepository;
import com.example.bookverseserver.repository.SignupRequestRepository;
import com.example.bookverseserver.repository.UserProfileRepository;
import com.example.bookverseserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional(readOnly = true)  // Default read-only, override for write methods
public class SignupRequestService {
    private static final Logger log = LoggerFactory.getLogger(SignupRequestService.class);
    private final SignupRequestRepository signupRequestRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final EmailService emailService;
    private final RoleRepository roleRepository;

    @Value("${app.otp.secret}")
    private String otpSecret;

    @Value("${app.otp.ttl-seconds:600}")
    private long otpTtlSeconds;

    // rate-limiting values
    @Value("${app.otp.max-attempts:5}")
    private int maxOtpAttempts;

    public SignupRequestService(SignupRequestRepository signupRequestRepository,
                                UserRepository userRepository,
                                UserProfileRepository userProfileRepository,
                                RoleRepository roleRepository,
                                EmailService emailService) {
        this.signupRequestRepository = signupRequestRepository;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.emailService = emailService;
        this.roleRepository = roleRepository;
    }

    public String generateOtpCode() {
        SecureRandom rnd = new SecureRandom();
        int code = rnd.nextInt(900_000) + 100_000; // 6-digit
        return String.valueOf(code);
    }

    public String hmacOtp(String otp) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(otpSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(key);
            byte[] raw = mac.doFinal(otp.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(raw);
        } catch (Exception e) {
            throw new RuntimeException("Failed to HMAC OTP", e);
        }
    }

    /**
     * Create or replace a SignupRequest for email.
     * Returns the plaintext OTP for immediate sending (not persisted).
     */
    public String createSignupRequest(String email, String username, String passwordHash) {
        String otp = generateOtpCode();
        String otpHash = hmacOtp(otp);
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(otpTtlSeconds);

        signupRequestRepository.findByEmail(email).ifPresent(existing -> {
            signupRequestRepository.delete(existing);
            log.info("Replaced existing signup request for {}", email);
        });

        SignupRequest req = new SignupRequest();
        req.setEmail(email);
        req.setUsername(username);
        req.setPasswordHash(passwordHash);
        req.setOtpHash(otpHash);
        req.setCreatedAt(now);
        req.setExpiresAt(expiresAt);
        signupRequestRepository.save(req);

        emailService.sendOtpEmail(email, otp);

        log.info("Created signup request for {} with expiry {}", email, expiresAt);
        return otp;
    }

    /**
     * Verify OTP and create real User atomically. Returns created User.
     * Throws AppException (or custom) on failure.
     */
    @Transactional
    public User verifyOtpAndCreateUser(String email, String otp) {
        Optional<SignupRequest> maybe = signupRequestRepository.findByEmail(email);
        if (maybe.isEmpty()) {
            throw new IllegalArgumentException("No pending signup for email");
        }
        SignupRequest req = maybe.get();

        if (Instant.now().isAfter(req.getExpiresAt())) {
            signupRequestRepository.delete(req);
            throw new IllegalArgumentException("OTP expired");
        }

        if (req.getAttempts() != null && req.getAttempts() >= maxOtpAttempts) {
            signupRequestRepository.delete(req);
            throw new IllegalArgumentException("Too many failed attempts");
        }

        String expected = hmacOtp(otp);
        if (!constantTimeEquals(expected, req.getOtpHash())) {
            req.setAttempts(req.getAttempts() + 1);
            signupRequestRepository.save(req);
            throw new IllegalArgumentException("Invalid OTP");
        }

        // P0 Security Fix #17: Case-insensitive email check prevents duplicate accounts
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            signupRequestRepository.delete(req);
            throw new IllegalStateException("User already exists with this email");
        }

        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new RuntimeException("USER role not found"));

        User user = new User();
        user.setEmail(req.getEmail());
        user.setUsername(req.getUsername());
        user.setPasswordHash(req.getPasswordHash());
        
        // Set roles as Set<Role>
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);
        
        user.setEnabled(true);

        userRepository.save(user);


        // Create user profile
        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setAccountType("BUYER");
        profile.setDisplayName(req.getUsername() != null ? req.getUsername() : user.getEmail());
        userProfileRepository.save(profile);

        signupRequestRepository.delete(req);

        log.info("Signup verified and created user id={} email={}", user.getId(), user.getEmail());
        return user;
    }


    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
