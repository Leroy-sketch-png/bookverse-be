package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.Transaction.CreatePaymentIntentRequest;
import com.example.bookverseserver.dto.request.Transaction.VerifyPaymentRequest;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.Transaction.PaymentAuditResponse;
import com.example.bookverseserver.dto.response.Transaction.PaymentIntentResponse;
import com.example.bookverseserver.dto.response.Transaction.PaymentVerificationResponse;
import com.example.bookverseserver.service.TransactionService;
import com.example.bookverseserver.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Transactions", description = "💳 Payment transaction management APIs - Stripe payment intents, verification, history")
public class TransactionController {

    SecurityUtils securityUtils;
    TransactionService transactionService;

    @Operation(
        summary = "Create Stripe Payment Intent",
        description = "Initialize a Stripe payment intent for checkout. " +
                     "**Idempotency**: Use Idempotency-Key header to prevent duplicate charges. " +
                     "Returns client_secret for frontend Stripe.js integration. " +
                     "**Payment flow**: " +
                     "1. Create intent (this endpoint) " +
                     "2. Frontend confirms payment with Stripe " +
                     "3. Verify payment with /verify endpoint"
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Payment intent created successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid amount or currency"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Unauthorized"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500", 
            description = "Stripe API error"
        )
    })
    @PostMapping("/intent")
    public ApiResponse<PaymentIntentResponse> createPaymentIntent(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Payment details (amount, currency, order ID)",
                required = true,
                content = @Content(schema = @Schema(implementation = CreatePaymentIntentRequest.class))
            )
            @RequestBody CreatePaymentIntentRequest request,
            
            @Parameter(description = "Idempotency key to prevent duplicate charges")
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            
            Authentication authentication
    ) {
        try {
            // 1. Lấy User ID từ SecurityUtils (Utility class của bạn)
            //Long userId = securityUtils.getCurrentUserId(authentication);
            Long userId = 4L;

            // 2. Gọi Service xử lý logic
            PaymentIntentResponse response = transactionService.createPaymentIntent(request, userId, idempotencyKey);

            // 3. Trả về kết quả thành công
            return ApiResponse.<PaymentIntentResponse>builder()
                    .code(200)
                    .message("Payment intent created successfully")
                    .result(response) // Truyền thẳng object DTO vào, không cần bọc trong Map
                    .build();

        } catch (Exception e) {
            // 4. Xử lý lỗi (Exception từ Service hoặc Stripe)
            return ApiResponse.<PaymentIntentResponse>builder()
                    .code(400) // Hoặc code lỗi tương ứng
                    .message(e.getMessage())
                    .build();
        }
    }

    @Operation(
        summary = "Verify payment status",
        description = "Verify and confirm payment success with Stripe. " +
                     "**Call this after** user completes payment on frontend. " +
                     "Checks payment_intent status and updates order accordingly. " +
                     "**Statuses**: succeeded, processing, requires_payment_method, failed"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Payment verified successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Payment failed or invalid payment_intent_id"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Payment intent not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500", 
            description = "Stripe verification error"
        )
    })
    @PostMapping("/verify")
    public ApiResponse<PaymentVerificationResponse> verifyPayment(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Payment intent ID from Stripe",
                required = true,
                content = @Content(schema = @Schema(implementation = VerifyPaymentRequest.class))
            )
            @RequestBody VerifyPaymentRequest request
    ) {
        try {
            PaymentVerificationResponse response = transactionService.verifyPayment(request);

            return ApiResponse.<PaymentVerificationResponse>builder()
                    .code(200)
                    .message("Payment verified successfully")
                    .result(response)
                    .build();

        } catch (Exception e) {
            e.printStackTrace(); // Log lỗi server
            return ApiResponse.<PaymentVerificationResponse>builder()
                    .code(400)
                    .message(e.getMessage())
                    .build();
        }
    }

    @Operation(
        summary = "Get payment history",
        description = "Retrieve paginated payment transaction history for current user. " +
                     "Includes all payment attempts, successful and failed transactions. " +
                     "Shows payment method, amount, status, and timestamps."
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Payment history retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Unauthorized"
        )
    })
    @GetMapping("/history")
    public ApiResponse<Page<PaymentAuditResponse>> getPaymentHistory(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Items per page", example = "10")
            @RequestParam(defaultValue = "10") int limit,
            
            Authentication authentication
    ) {
        try {
            // Lấy User thật từ Token
            Long userId = securityUtils.getCurrentUserId(authentication);

            Page<PaymentAuditResponse> history = transactionService.getUserPaymentAudit(userId, page, limit);

            return ApiResponse.<Page<PaymentAuditResponse>>builder()
                    .code(200)
                    .message("History retrieved successfully")
                    .result(history)
                    .build();
        } catch (Exception e) {
            return ApiResponse.<Page<PaymentAuditResponse>>builder()
                    .code(400).message(e.getMessage()).build();
        }
    }
}
