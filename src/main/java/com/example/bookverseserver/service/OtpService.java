package com.example.bookverseserver.service;

import com.example.bookverseserver.entity.User.EmailVerificationToken;
import com.example.bookverseserver.repository.EmailVerificationTokenRepository;
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
    private final EmailVerificationTokenRepository tokenRepo;
    private final EmailService emailService;

    public void generateAndSendOtp(String email) {
        String otp = EmailService.generateOtp(6);

        EmailVerificationToken token = EmailVerificationToken.builder()
                .email(email)
                .otp(otp)
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .build();

        tokenRepo.save(token);

        emailService.sendOtpEmail(email, otp);
    }

    public boolean verifyOtp(String email, String otp) {
        return tokenRepo.findByEmailAndOtp(email, otp)
                .filter(token -> token.getExpiryDate().isAfter(LocalDateTime.now()))
                .map(token -> {
                    tokenRepo.delete(token);
                    return true;
                })
                .orElse(false);
    }
}

