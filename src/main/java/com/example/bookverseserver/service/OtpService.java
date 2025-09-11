package com.example.bookverseserver.service;

import com.example.bookverseserver.entity.User.EmailVerificationToken;
import com.example.bookverseserver.repository.EmailVerificationTokenRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OtpService {

    EmailVerificationTokenRepository tokenRepo;
    EmailService emailService;

    @Transactional
    public void generateAndSendOtp(String email) {
        // Delete existing token for email to avoid duplicates
        tokenRepo.deleteByEmail(email);

        String otp = emailService.generateOtp(6); // instance method now
        EmailVerificationToken token = EmailVerificationToken.builder()
                .email(email)
                .otp(otp)
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .build();

        tokenRepo.save(token);
        log.info("Generated OTP for email: {}", email);

        emailService.sendOtpEmail(email, otp);
    }

    public boolean verifyOtp(String email, String otp) {
        return tokenRepo.findByEmailAndOtp(email, otp)
                .filter(token -> token.getExpiryDate().isAfter(LocalDateTime.now()))
                .map(token -> {
                    tokenRepo.delete(token);
                    log.info("OTP verified successfully for email: {}", email);
                    return true;
                })
                .orElseGet(() -> {
                    log.warn("OTP verification failed for email: {}", email);
                    return false;
                });
    }
}
