package com.example.bookverseserver.service;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.bookverseserver.dto.request.Authentication.RoleRequest;
import com.example.bookverseserver.dto.response.Authentication.RoleResponse;
import com.example.bookverseserver.mapper.RoleMapper;
import com.example.bookverseserver.repository.PermissionRepository;
import com.example.bookverseserver.repository.RoleRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendOtpEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your OTP Code");
        message.setText("Your OTP is: " + otp + "\nThis code will expire in 5 minutes.");
        mailSender.send(message);
    }

    public static String generateOtp(int length) {
        StringBuilder otp = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < length; i++) {
            otp.append(random.nextInt(10)); // 0-9
        }
        return otp.toString();
    }
}
