package com.example.bookverseserver.controller;

import com.example.bookverseserver.entity.Order_Payment.Order;
import com.example.bookverseserver.entity.Order_Payment.Payment;
import com.example.bookverseserver.enums.OrderStatus;
import com.example.bookverseserver.enums.PaymentStatus;
import com.example.bookverseserver.repository.OrderRepository;
import com.example.bookverseserver.repository.TransactionRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Stripe Webhook Controller
 * 
 * Handles Stripe webhook events for payment lifecycle.
 * This is the PRIMARY mechanism for payment status updates.
 * The /verify endpoint should be a FALLBACK only.
 * 
 * Events handled:
 * - payment_intent.succeeded: Mark payment as PAID, update order to PROCESSING
 * - payment_intent.payment_failed: Mark payment as FAILED
 * - charge.refunded: Mark payment as REFUNDED, update order to REFUNDED
 * 
 * @see <a href="https://stripe.com/docs/webhooks">Stripe Webhooks Documentation</a>
 */
@RestController
@RequestMapping("/api/stripe")
@RequiredArgsConstructor
@Slf4j
@Hidden // Hide from Swagger - webhooks are not user-facing
public class StripeWebhookController {

    private final TransactionRepository transactionRepository;
    private final OrderRepository orderRepository;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    /**
     * Main webhook endpoint for Stripe events.
     * 
     * SECURITY: This endpoint MUST verify the Stripe signature before processing.
     * The raw request body is required for signature verification.
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        log.info("Received Stripe webhook event");
        
        Event event;
        
        // Skip signature verification in development if placeholder secret
        if (webhookSecret.startsWith("whsec_placeholder")) {
            log.warn("⚠️ Webhook signature verification SKIPPED (development mode)");
            try {
                event = Event.GSON.fromJson(payload, Event.class);
            } catch (Exception e) {
                log.error("Failed to parse webhook payload", e);
                return ResponseEntity.badRequest().body("Invalid payload");
            }
        } else {
            // PRODUCTION: Verify Stripe signature
            try {
                event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            } catch (SignatureVerificationException e) {
                log.error("Webhook signature verification failed", e);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
            } catch (Exception e) {
                log.error("Error parsing webhook event", e);
                return ResponseEntity.badRequest().body("Invalid payload");
            }
        }

        // Handle the event
        String eventType = event.getType();
        log.info("Processing Stripe event: {} (id: {})", eventType, event.getId());

        EventDataObjectDeserializer dataDeserializer = event.getDataObjectDeserializer();
        Optional<StripeObject> stripeObjectOpt = dataDeserializer.getObject();
        
        if (stripeObjectOpt.isEmpty()) {
            log.error("Failed to deserialize event data object");
            return ResponseEntity.badRequest().body("Failed to deserialize event");
        }

        StripeObject stripeObject = stripeObjectOpt.get();

        try {
            switch (eventType) {
                case "payment_intent.succeeded" -> handlePaymentSucceeded((PaymentIntent) stripeObject);
                case "payment_intent.payment_failed" -> handlePaymentFailed((PaymentIntent) stripeObject);
                case "charge.refunded" -> handleRefund((Refund) stripeObject);
                default -> log.info("Unhandled event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Error processing webhook event: {}", eventType, e);
            // Return 200 to prevent Stripe from retrying (we've logged the error)
            // In production, you might want to queue this for manual review
        }

        return ResponseEntity.ok("Webhook received");
    }

    /**
     * Handle successful payment
     * - Update Payment status to PAID
     * - Update Order status to PROCESSING
     * - Record paid timestamp
     */
    private void handlePaymentSucceeded(PaymentIntent paymentIntent) {
        String paymentIntentId = paymentIntent.getId();
        log.info("Payment succeeded: {}", paymentIntentId);

        Optional<Payment> paymentOpt = transactionRepository.findByPaymentIntentId(paymentIntentId);
        
        if (paymentOpt.isEmpty()) {
            log.warn("Payment not found for PaymentIntent: {}. May have been created via direct API.", paymentIntentId);
            return;
        }

        Payment payment = paymentOpt.get();
        
        // Idempotency: Don't update if already paid
        if (payment.getStatus() == PaymentStatus.PAID) {
            log.info("Payment {} already marked as PAID, skipping", paymentIntentId);
            return;
        }

        // Update payment
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        payment.setTransactionId(paymentIntent.getLatestCharge());
        transactionRepository.save(payment);

        // Update order status
        Order order = payment.getOrder();
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.PROCESSING);
            orderRepository.save(order);
            log.info("Order {} updated to PROCESSING", order.getOrderNumber());
        }

        log.info("Payment {} successfully processed via webhook", paymentIntentId);
    }

    /**
     * Handle failed payment
     * - Update Payment status to FAILED
     * - Order remains PENDING (user can retry)
     */
    private void handlePaymentFailed(PaymentIntent paymentIntent) {
        String paymentIntentId = paymentIntent.getId();
        log.info("Payment failed: {}", paymentIntentId);

        Optional<Payment> paymentOpt = transactionRepository.findByPaymentIntentId(paymentIntentId);
        
        if (paymentOpt.isEmpty()) {
            log.warn("Payment not found for failed PaymentIntent: {}", paymentIntentId);
            return;
        }

        Payment payment = paymentOpt.get();
        payment.setStatus(PaymentStatus.FAILED);
        transactionRepository.save(payment);

        log.info("Payment {} marked as FAILED", paymentIntentId);
    }

    /**
     * Handle refund
     * - Update Payment status to REFUNDED
     * - Update Order status to REFUNDED (if full refund)
     */
    private void handleRefund(Refund refund) {
        String paymentIntentId = refund.getPaymentIntent();
        log.info("Refund processed for PaymentIntent: {}", paymentIntentId);

        Optional<Payment> paymentOpt = transactionRepository.findByPaymentIntentId(paymentIntentId);
        
        if (paymentOpt.isEmpty()) {
            log.warn("Payment not found for refunded PaymentIntent: {}", paymentIntentId);
            return;
        }

        Payment payment = paymentOpt.get();
        payment.setStatus(PaymentStatus.REFUNDED);
        transactionRepository.save(payment);

        // Update order status if full refund
        Order order = payment.getOrder();
        if ("succeeded".equals(refund.getStatus())) {
            order.setStatus(OrderStatus.REFUNDED);
            orderRepository.save(order);
            log.info("Order {} marked as REFUNDED", order.getOrderNumber());
        }

        log.info("Refund for payment {} processed", paymentIntentId);
    }
}
