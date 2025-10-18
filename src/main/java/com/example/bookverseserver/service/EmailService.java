package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.Authentication.EmailRequest;
import com.example.bookverseserver.dto.request.Authentication.Recipient;
import com.example.bookverseserver.dto.request.Authentication.SendEmailRequest;
import com.example.bookverseserver.dto.request.Authentication.Sender;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.repository.httpclient.EmailClient;
import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailService {
    EmailClient emailClient;

    @Value("${notification.email.brevo-apikey}")
    @NonFinal
    String apiKey;

    public void sendEmail(SendEmailRequest request) {
        EmailRequest emailRequest = EmailRequest.builder()
                .sender(Sender.builder()
                        .name("BookVerse")
                        .email("tinvo2005@gmail.com")
                        .build())
                .to(request.getTo())
                .subject(request.getSubject())
                .htmlContent(request.getHtmlContent())
                .build();
        try {

            emailClient.sendEmail(apiKey, emailRequest);
            log.info("✅ Email successfully dispatched via Brevo.");

        } catch (FeignException e) {
            log.error("❌ Error sending email via Brevo. Status: {}, Body: {}, Headers: {}",
                    e.status(),
                    e.contentUTF8(),
                    e.responseHeaders());
            throw new AppException(ErrorCode.CANNOT_SEND_EMAIL);
        }
    }

    public void sendOtpEmail(String toEmail, String otp) {
        String subject = "Your BookVerse Verification Code";
        String htmlContent = String.format("""
                <html>
                  <body style="font-family: Arial, sans-serif; color: #333;">
                    <h2>Your One-Time Password (OTP)</h2>
                    <p>Hello,</p>
                    <p>Your verification code is:</p>
                    <p style="font-size: 24px; font-weight: bold; color: #0056b3; letter-spacing: 2px; border: 1px solid #ddd; padding: 10px; display: inline-block;">%s</p>
                    <p>This code will expire in 5 minutes.</p>
                    <p>If you did not request this code, please ignore this email.</p>
                    <br>
                    <p>Thank you,</p>
                    <p>The BookVerse Team</p>
                  </body>
                </html>
                """, otp);

        List<Recipient> recipients = List.of(
                Recipient.builder()
                        .email(toEmail)
                        .name("User")
                        .build()
        );

        SendEmailRequest request = SendEmailRequest.builder()
                .to(recipients)
                .subject(subject)
                .htmlContent(htmlContent)
                .build();

        this.sendEmail(request);
    }

    public static String generateOtp(int length) {
        StringBuilder otp = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < length; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
}

