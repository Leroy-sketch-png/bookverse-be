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

    @Value("${notification.email.sender-email:noreply@bookverse.com}")
    @NonFinal
    String senderEmail;

    @Value("${notification.email.sender-name:BookVerse}")
    @NonFinal
    String senderName;

    public void sendEmail(SendEmailRequest request) {
        EmailRequest emailRequest = EmailRequest.builder()
                .sender(Sender.builder()
                        .name(senderName)
                        .email(senderEmail)
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
    
    // ============================================================================
    // ORDER NOTIFICATION EMAILS
    // ============================================================================
    
    /**
     * Send order confirmation email to buyer after successful payment.
     */
    public void sendOrderConfirmation(String toEmail, String customerName, String orderNumber, 
                                       String totalAmount, int itemCount) {
        String subject = "Order Confirmed — " + orderNumber;
        String htmlContent = String.format("""
                <html>
                  <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto;">
                    <div style="background-color: #f8f5f0; padding: 20px; border-radius: 8px;">
                      <h2 style="color: #8B4513;">✓ Order Confirmed!</h2>
                      <p>Hi %s,</p>
                      <p>Thank you for your order! We've received your payment and your order is now being processed.</p>
                      
                      <div style="background: white; padding: 15px; border-radius: 8px; margin: 20px 0;">
                        <p style="margin: 5px 0;"><strong>Order Number:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>Items:</strong> %d book(s)</p>
                        <p style="margin: 5px 0;"><strong>Total:</strong> %s</p>
                      </div>
                      
                      <p>You'll receive another email when your order ships.</p>
                      
                      <p style="margin-top: 30px;">Happy reading! 📚</p>
                      <p>The BookVerse Team</p>
                    </div>
                  </body>
                </html>
                """, customerName, orderNumber, itemCount, totalAmount);

        sendEmailToRecipient(toEmail, customerName, subject, htmlContent);
    }
    
    /**
     * Send shipping notification email when order is shipped.
     */
    public void sendShippingNotification(String toEmail, String customerName, String orderNumber,
                                          String trackingNumber, String carrier) {
        String subject = "Your Order Has Shipped — " + orderNumber;
        String trackingInfo = trackingNumber != null && !trackingNumber.isEmpty() 
                ? String.format("<p style=\"margin: 5px 0;\"><strong>Tracking:</strong> %s (%s)</p>", trackingNumber, carrier != null ? carrier : "Standard")
                : "<p style=\"margin: 5px 0;\"><em>Tracking information will be available soon.</em></p>";
        
        String htmlContent = String.format("""
                <html>
                  <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto;">
                    <div style="background-color: #f8f5f0; padding: 20px; border-radius: 8px;">
                      <h2 style="color: #8B4513;">📦 Your Order is On Its Way!</h2>
                      <p>Hi %s,</p>
                      <p>Great news! Your order has been shipped and is on its way to you.</p>
                      
                      <div style="background: white; padding: 15px; border-radius: 8px; margin: 20px 0;">
                        <p style="margin: 5px 0;"><strong>Order Number:</strong> %s</p>
                        %s
                      </div>
                      
                      <p>You can track your order in your BookVerse account.</p>
                      
                      <p style="margin-top: 30px;">Happy reading! 📚</p>
                      <p>The BookVerse Team</p>
                    </div>
                  </body>
                </html>
                """, customerName, orderNumber, trackingInfo);

        sendEmailToRecipient(toEmail, customerName, subject, htmlContent);
    }
    
    /**
     * Send delivery confirmation email when order is delivered.
     */
    public void sendDeliveryConfirmation(String toEmail, String customerName, String orderNumber) {
        String subject = "Your Order Has Been Delivered — " + orderNumber;
        String htmlContent = String.format("""
                <html>
                  <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto;">
                    <div style="background-color: #f8f5f0; padding: 20px; border-radius: 8px;">
                      <h2 style="color: #8B4513;">🎉 Your Books Have Arrived!</h2>
                      <p>Hi %s,</p>
                      <p>Your order <strong>%s</strong> has been delivered!</p>
                      
                      <div style="background: white; padding: 15px; border-radius: 8px; margin: 20px 0;">
                        <p>We hope you love your new books. If you have a moment, we'd really appreciate a review!</p>
                        <p>Reviews help other readers discover great books and support the sellers you bought from.</p>
                      </div>
                      
                      <p style="margin-top: 30px;">Happy reading! 📚</p>
                      <p>The BookVerse Team</p>
                    </div>
                  </body>
                </html>
                """, customerName, orderNumber);

        sendEmailToRecipient(toEmail, customerName, subject, htmlContent);
    }
    
    /**
     * Send new order notification to seller.
     */
    public void sendNewOrderNotification(String toEmail, String sellerName, String orderNumber,
                                          String buyerName, String totalAmount, int itemCount) {
        String subject = "New Order Received — " + orderNumber;
        String htmlContent = String.format("""
                <html>
                  <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto;">
                    <div style="background-color: #f8f5f0; padding: 20px; border-radius: 8px;">
                      <h2 style="color: #8B4513;">🛒 New Order!</h2>
                      <p>Hi %s,</p>
                      <p>You have a new order waiting to be fulfilled!</p>
                      
                      <div style="background: white; padding: 15px; border-radius: 8px; margin: 20px 0;">
                        <p style="margin: 5px 0;"><strong>Order Number:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>Buyer:</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>Items:</strong> %d book(s)</p>
                        <p style="margin: 5px 0;"><strong>Total:</strong> %s</p>
                      </div>
                      
                      <p>Log in to your seller dashboard to view the order details and ship the items.</p>
                      
                      <p style="margin-top: 30px;">Thank you for selling on BookVerse!</p>
                      <p>The BookVerse Team</p>
                    </div>
                  </body>
                </html>
                """, sellerName, orderNumber, buyerName, itemCount, totalAmount);

        sendEmailToRecipient(toEmail, sellerName, subject, htmlContent);
    }
    
    /**
     * Helper method to send email to a single recipient.
     */
    private void sendEmailToRecipient(String toEmail, String name, String subject, String htmlContent) {
        List<Recipient> recipients = List.of(
                Recipient.builder()
                        .email(toEmail)
                        .name(name)
                        .build()
        );

        SendEmailRequest request = SendEmailRequest.builder()
                .to(recipients)
                .subject(subject)
                .htmlContent(htmlContent)
                .build();

        try {
            this.sendEmail(request);
            log.info("📧 Order email sent: {} to {}", subject, toEmail);
        } catch (Exception e) {
            log.error("Failed to send order email to {}: {}", toEmail, e.getMessage());
            // Don't throw - order emails should not block order flow
        }
    }
}

