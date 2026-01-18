package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.Transaction.CreatePaymentIntentRequest;
import com.example.bookverseserver.dto.request.Transaction.VerifyPaymentRequest;
import com.example.bookverseserver.dto.response.Transaction.PaymentAuditResponse;
import com.example.bookverseserver.dto.response.Transaction.PaymentIntentResponse;
import com.example.bookverseserver.dto.response.Transaction.PaymentVerificationResponse;
import com.example.bookverseserver.entity.Order_Payment.Order;
import com.example.bookverseserver.entity.Order_Payment.Payment;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.OrderStatus;
import com.example.bookverseserver.enums.PaymentStatus;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.repository.OrderRepository;
import com.example.bookverseserver.repository.TransactionRepository;
import com.example.bookverseserver.repository.UserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)  // Default read-only, override for write methods
public class TransactionService {

    private final TransactionRepository transactionRepository; // Lưu ý: Đây chính là PaymentRepository
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Value("${stripe.publishable.key}")
    private String stripePublishableKey;

    @Transactional
    public PaymentIntentResponse createPaymentIntent(CreatePaymentIntentRequest request, Long userId, String idempotencyKey) {
        // 1. Lấy User từ DB
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 2. Lấy Order từ DB (Dùng Long ID)
        // Giả sử request.getOrderId() trả về Long. Nếu là String thì parse: Long.parseLong(request.getOrderId())
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        // 3. Validate: Đảm bảo Order thuộc về User này
        if (!order.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        boolean isAlreadyPaid = order.getPayments().stream()
                .anyMatch(p -> p.getStatus() == PaymentStatus.PAID);

        if (isAlreadyPaid) {
            throw new AppException(ErrorCode.ORDER_ALREADY_PAID);
        }

        // 4. Lấy số tiền thực tế từ Order
        BigDecimal amount = order.getTotalAmount();
        long amountInCents = amount.multiply(new BigDecimal(100)).longValue();

        try {
            // 5. Tạo Stripe Payment Intent
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency("VND") // Lưu ý: Stripe Account phải support VND, nếu không thì dùng USD
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
                    )
                    .putMetadata("order_id", String.valueOf(order.getId()))
                    .putMetadata("user_id", String.valueOf(user.getId()))
                    .build();

            // --- LOGIC IDEMPOTENCY ---
            // Nếu Client không gửi key lên, ta tự tạo UUID để ít nhất bảo vệ được việc gọi Stripe bị lặp
            if (idempotencyKey == null || idempotencyKey.isEmpty()) {
                idempotencyKey = "req_" + UUID.randomUUID().toString();
            }

            RequestOptions options = RequestOptions.builder()
                    .setIdempotencyKey(idempotencyKey) // <--- Đã có biến để truyền vào
                    .build();

            // QUAN TRỌNG: Phải truyền 'options' vào đây thì Idempotency mới có tác dụng!
            PaymentIntent intent = PaymentIntent.create(params, options);

            // 6. Lưu Payment vào DB
            Payment payment = Payment.builder()
                    .order(order)
                    .user(user)
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
                    .currency("vnd") // Sửa lại cho khớp với params ở trên
                    .status(intent.getStatus())
                    .publishableKey(stripePublishableKey)
                    .build();

        } catch (StripeException e) {
            log.error("Stripe Create Intent Error: {}", e.getMessage());
            throw new AppException(ErrorCode.PAYMENT_INTENT_CREATION_FAILED);
        }
    }

    @Transactional
    public PaymentVerificationResponse verifyPayment(VerifyPaymentRequest request) {
        try {
            // 1. Tìm Payment trong DB (Nếu không thấy thì lỗi luôn)
            Payment payment = transactionRepository.findByPaymentIntentId(request.getPaymentIntentId())
                    .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));

            // 2. Validate Order ID khớp nhau (Tránh trường hợp Payment của đơn A mà verify cho đơn B)
            if (!payment.getOrder().getId().equals(request.getOrderId())) {
                throw new AppException(ErrorCode.PAYMENT_ORDER_MISMATCH);
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
                log.warn("Payment verification failed. Stripe status: {}", intent.getStatus());
                throw new AppException(ErrorCode.PAYMENT_VERIFICATION_FAILED);
            }

        } catch (StripeException e) {
            log.error("Stripe Verify Error: {}", e.getMessage());
            throw new AppException(ErrorCode.PAYMENT_PROCESSING_ERROR);
        }
    }

    public Page<PaymentAuditResponse> getUserPaymentAudit(Long userId, int page, int limit) {
        // 1. Tạo Pageable: Sắp xếp giảm dần theo ngày tạo (Mới nhất lên đầu)
        Pageable pageable = PageRequest.of(page, limit, Sort.by("createdAt").descending());

        // 2. Gọi Repo lấy dữ liệu
        Page<Payment> paymentPage = transactionRepository.findByUserId(userId, pageable);

        // 3. Map Entity -> DTO Response
        return paymentPage.map(payment -> PaymentAuditResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId()) // Lấy ID từ object Order
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .build());
    }
    
    /**
     * Process refund for an order's payment via Stripe.
     * 
     * @param order The order to refund
     * @return RefundResult with status and Stripe refund ID
     */
    @Transactional
    public RefundResult processRefund(Order order) {
        // Find the paid payment for this order
        Payment payment = order.getPayments().stream()
                .filter(p -> p.getStatus() == PaymentStatus.PAID && p.getPaymentIntentId() != null)
                .findFirst()
                .orElse(null);
        
        if (payment == null) {
            log.warn("No paid payment found for order {}, skipping refund", order.getId());
            return RefundResult.builder()
                    .success(false)
                    .message("No paid payment found to refund")
                    .build();
        }
        
        try {
            // Create Stripe refund
            com.stripe.param.RefundCreateParams params = com.stripe.param.RefundCreateParams.builder()
                    .setPaymentIntent(payment.getPaymentIntentId())
                    .setReason(com.stripe.param.RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                    .build();
            
            com.stripe.model.Refund refund = com.stripe.model.Refund.create(params);
            
            // Update payment status
            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setTransactionId(refund.getId());
            transactionRepository.save(payment);
            
            log.info("Refund processed successfully for order {}: refund_id={}", 
                    order.getId(), refund.getId());
            
            return RefundResult.builder()
                    .success(true)
                    .refundId(refund.getId())
                    .amount(payment.getAmount())
                    .message("Refund processed successfully")
                    .build();
            
        } catch (StripeException e) {
            log.error("Stripe refund failed for order {}: {}", order.getId(), e.getMessage());
            return RefundResult.builder()
                    .success(false)
                    .message("Refund failed: " + e.getMessage())
                    .build();
        }
    }
    
    @lombok.Data
    @lombok.Builder
    public static class RefundResult {
        private boolean success;
        private String refundId;
        private BigDecimal amount;
        private String message;
    }
}