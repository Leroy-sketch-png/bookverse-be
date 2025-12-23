package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.Transaction.CreatePaymentIntentRequest;
import com.example.bookverseserver.dto.request.Transaction.VerifyPaymentRequest;
import com.example.bookverseserver.dto.response.Transaction.PaymentIntentResponse;
import com.example.bookverseserver.dto.response.Transaction.PaymentVerificationResponse;
import com.example.bookverseserver.entity.Order_Payment.Order;
import com.example.bookverseserver.entity.Order_Payment.Payment;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.OrderStatus;
import com.example.bookverseserver.enums.PaymentStatus;
import com.example.bookverseserver.repository.OrderRepository;
import com.example.bookverseserver.repository.TransactionRepository; // PaymentRepository đổi tên thành TransactionRepository?
import com.example.bookverseserver.repository.UserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository; // Lưu ý: Đây chính là PaymentRepository
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Value("${stripe.publishable.key}")
    private String stripePublishableKey;

    @Transactional
    public PaymentIntentResponse createPaymentIntent(CreatePaymentIntentRequest request, Long userId) {
        // 1. Lấy User từ DB
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Lấy Order từ DB (Dùng Long ID)
        // Giả sử request.getOrderId() trả về Long. Nếu là String thì parse: Long.parseLong(request.getOrderId())
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // 3. Validate: Đảm bảo Order thuộc về User này
        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Order does not belong to this user");
        }

        // 4. Lấy số tiền thực tế từ Order
        BigDecimal amount = order.getTotalAmount();
        long amountInCents = amount.multiply(new BigDecimal(100)).longValue();

        try {
            // 5. Tạo Stripe Payment Intent
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency("VND")
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
                    )
                    // Metadata giúp tracking trên Dashboard Stripe dễ hơn
                    .putMetadata("order_id", String.valueOf(order.getId()))
                    .putMetadata("user_id", String.valueOf(user.getId()))
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);

            // 6. Lưu Payment vào DB (Map Entity quan hệ)
            Payment payment = Payment.builder()
                    .order(order)   // Set quan hệ ManyToOne
                    .user(user)     // Set quan hệ ManyToOne
                    .paymentIntentId(intent.getId())
                    .amount(amount)
                    .status(PaymentStatus.PENDING)
                    .paymentMethod("STRIPE")
                    .build();

            transactionRepository.save(payment);

            return PaymentIntentResponse.builder()
                    .paymentIntentId(intent.getId())
                    .clientSecret(intent.getClientSecret())
                    .amount(amountInCents)
                    .currency("usd")
                    .status(intent.getStatus())
                    .publishableKey(stripePublishableKey)
                    .build();

        } catch (StripeException e) {
            log.error("Stripe Create Intent Error", e);
            throw new RuntimeException("Failed to create payment intent: " + e.getMessage());
        }
    }

    @Transactional
    public PaymentVerificationResponse verifyPayment(VerifyPaymentRequest request) {
        try {
            // 1. Tìm Payment trong DB (Nếu không thấy thì lỗi luôn)
            Payment payment = transactionRepository.findByPaymentIntentId(request.getPaymentIntentId())
                    .orElseThrow(() -> new RuntimeException("Transaction not found"));

            // 2. Validate Order ID khớp nhau (Tránh trường hợp Payment của đơn A mà verify cho đơn B)
            if (!payment.getOrder().getId().equals(request.getOrderId())) {
                throw new RuntimeException("Payment does not match the provided Order ID");
            }

            // 3. Gọi Stripe kiểm tra trạng thái thực tế
            PaymentIntent intent = PaymentIntent.retrieve(request.getPaymentIntentId());

            // 4. Kiểm tra kết quả từ Stripe
            if ("succeeded".equals(intent.getStatus())) {

                // Chỉ update nếu trạng thái DB chưa là PAID (tránh update thừa)
                if (payment.getStatus() != PaymentStatus.PAID) {

                    // A. Update trạng thái thanh toán
                    payment.setStatus(PaymentStatus.PAID);
                    payment.setPaidAt(LocalDateTime.now());
                    payment.setTransactionId(intent.getLatestCharge());
                    transactionRepository.save(payment);

                    // B. Xử lý trạng thái Đơn Hàng (Order)
                    // Vì enum OrderStatus của bạn chỉ có: PENDING, SHIPPED, DELIVERED, CANCELLED
                    // Nên ta giữ nguyên là PENDING (Hiểu là: Đã thanh toán, chờ ship).
                    // Nếu sau này bạn thêm OrderStatus.PROCESSING, hãy uncomment dòng dưới:

                    // Order order = payment.getOrder();
                    // order.setOrderStatus(OrderStatus.PROCESSING);
                    // orderRepository.save(order);
                }

                return PaymentVerificationResponse.builder()
                        .orderId(String.valueOf(payment.getOrder().getId()))
                        .paymentStatus("PAID") // Trạng thái của Payment
                        .transactionId(payment.getTransactionId())
                        .paidAt(payment.getPaidAt())
                        .build();

            } else {
                throw new RuntimeException("Payment not successful. Status: " + intent.getStatus());
            }

        } catch (StripeException e) {
            log.error("Stripe Verify Error", e);
            throw new RuntimeException("Stripe verification failed");
        }
    }
}