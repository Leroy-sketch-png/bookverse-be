package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.Transaction.CreatePaymentIntentRequest;
import com.example.bookverseserver.dto.request.Transaction.VerifyPaymentRequest;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.Transaction.PaymentIntentResponse;
import com.example.bookverseserver.dto.response.Transaction.PaymentVerificationResponse;
import com.example.bookverseserver.service.TransactionService;
import com.example.bookverseserver.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/transactions")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Transaction", description = "Transaction management APIs")
public class TransactionController {

    SecurityUtils securityUtils;
    TransactionService transactionService;

    @Operation(summary = "Create Payment Intent", description = "Initialize a Stripe payment intent")
    @PostMapping("/intent")
    public ApiResponse<PaymentIntentResponse> createPaymentIntent(
            @RequestBody CreatePaymentIntentRequest request,
            Authentication authentication
    ) {
        try {
            // 1. Lấy User ID từ SecurityUtils (Utility class của bạn)
            //Long userId = securityUtils.getCurrentUserId(authentication);
            Long userId = 4L;

            // 2. Gọi Service xử lý logic
            PaymentIntentResponse response = transactionService.createPaymentIntent(request, userId);

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

    @Operation(summary = "Verify Payment", description = "Verify payment status with Stripe")
    @PostMapping("/verify")
    public ApiResponse<PaymentVerificationResponse> verifyPayment(
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
}
