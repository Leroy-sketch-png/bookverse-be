package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.Order.CreateCheckoutRequest;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.Order.CheckoutResponse;
import com.example.bookverseserver.service.CheckoutService;
import com.example.bookverseserver.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {

  private final CheckoutService checkoutService;
  private final SecurityUtils securityUtils;

  @PostMapping
  public ApiResponse<CheckoutResponse> createCheckoutSession(
      @RequestBody CreateCheckoutRequest request,
      Authentication authentication) {
    Long userId = securityUtils.getCurrentUserId(authentication);
    CheckoutResponse checkoutResponse = checkoutService.createCheckoutSession(userId, request);
    return ApiResponse.<CheckoutResponse>builder()
        .message("Checkout session created successfully")
        .result(checkoutResponse)
        .build();
  }
}
