package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.Order.CreateCheckoutRequest;
import com.example.bookverseserver.dto.response.Order.CheckoutResponse;
import com.example.bookverseserver.dto.response.Order.UnavailableItemDTO;
import com.example.bookverseserver.entity.Order_Payment.*;
import com.example.bookverseserver.entity.Product.Listing;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.OrderStatus;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.exception.OutOfStockException;
import com.example.bookverseserver.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CheckoutService {

  CartRepository cartRepository;
  OrderRepository orderRepository;
  OrderItemRepository orderItemRepository;
  CheckoutSessionRepository checkoutSessionRepository;
  OrderTimelineRepository orderTimelineRepository;
  ListingRepository listingRepository;
  UserRepository userRepository;
  VoucherService voucherService;

  @NonFinal
  @Value("${checkout.tax-rate:0.08}")
  BigDecimal taxRate;

  @NonFinal
  @Value("${checkout.shipping-flat-fee:5.00}")
  BigDecimal shippingFlatFee;

  @Transactional
  public CheckoutResponse createCheckoutSession(Long userId, CreateCheckoutRequest request) {
    User currentUser = userRepository.findById(userId)
        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

    Cart cart = cartRepository.findById(request.getCartId())
        .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

    if (!cart.getUser().getId().equals(currentUser.getId())) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    if (cart.getCartItems().isEmpty()) {
      throw new AppException(ErrorCode.INVALID_REQUEST);
    }

    // Validate stock
    validateStock(cart);

    // Calculate totals
    BigDecimal subtotal = cart.getTotalPrice();
    BigDecimal tax = subtotal.multiply(taxRate);
    BigDecimal shipping = shippingFlatFee;
    BigDecimal discount = BigDecimal.ZERO;

    if (request.getPromoCode() != null && !request.getPromoCode().isEmpty()) {
      try {
        discount = voucherService.calculateDiscount(request.getPromoCode(), subtotal);
      } catch (AppException e) {
        throw new AppException(ErrorCode.INVALID_PROMO_CODE);
      }
    }

    BigDecimal total = subtotal.add(tax).add(shipping).subtract(discount);

    // Create Order (PENDING)
    Order order = Order.builder()
        .user(currentUser)
        .orderNumber(generateOrderNumber())
        .status(OrderStatus.PENDING)
        .subtotal(subtotal)
        .tax(tax)
        .shipping(shipping)
        .discount(discount)
        .total(total)
        .promoCode(request.getPromoCode())
        .notes(request.getNotes())
        .build();

    Order savedOrder = orderRepository.save(order);

    // Create Order Items and deduct stock
    List<OrderItem> orderItems = new ArrayList<>();
    cart.getCartItems().forEach(cartItem -> {
      OrderItem orderItem = OrderItem.fromCartItem(cartItem, savedOrder);
      orderItems.add(orderItem);

      // Deduct stock
      Listing listing = cartItem.getListing();
      listing.setQuantity(listing.getQuantity() - cartItem.getQuantity());
      listing.setSoldCount(listing.getSoldCount() + cartItem.getQuantity());
      listingRepository.save(listing);
    });
    orderItemRepository.saveAll(orderItems);

    // Add to timeline
    OrderTimeline timeline = OrderTimeline.builder()
        .order(savedOrder)
        .status("PENDING")
        .note("Order created from checkout session")
        .build();
    orderTimelineRepository.save(timeline);

    // Create Checkout Session
    CheckoutSession session = CheckoutSession.builder()
        .user(currentUser)
        .cart(cart)
        .order(savedOrder)
        .amount(total)
        .currency("USD")
        .status("PENDING")
        .expiresAt(LocalDateTime.now().plusHours(24))
        .paymentIntentId("pi_" + UUID.randomUUID().toString()) // Stripe placeholder
        .clientSecret("secret_" + UUID.randomUUID().toString()) // Stripe placeholder
        .build();

    CheckoutSession savedSession = checkoutSessionRepository.save(session);

    // Clear cart
    cart.getCartItems().clear();
    cart.setTotalPrice(BigDecimal.ZERO);
    cartRepository.save(cart);

    return CheckoutResponse.builder()
        .sessionId(savedSession.getId())
        .orderId(savedOrder.getId())
        .paymentIntentId(savedSession.getPaymentIntentId())
        .clientSecret(savedSession.getClientSecret())
        .amount(savedSession.getAmount())
        .currency(savedSession.getCurrency())
        .status("PENDING_PAYMENT")
        .expiresAt(savedSession.getExpiresAt())
        .build();
  }

  private void validateStock(Cart cart) {
    List<UnavailableItemDTO> unavailableItems = new ArrayList<>();

    for (CartItem item : cart.getCartItems()) {
      if (item.getListing().getQuantity() < item.getQuantity()) {
        unavailableItems.add(UnavailableItemDTO.builder()
            .bookId(item.getListing().getBookMeta().getId())
            .title(item.getListing().getBookMeta().getTitle())
            .requestedQuantity(item.getQuantity())
            .availableStock(item.getListing().getQuantity())
            .build());
      }
    }

    if (!unavailableItems.isEmpty()) {
      throw new OutOfStockException(unavailableItems);
    }
  }

  private String generateOrderNumber() {
    return "BV-" + LocalDateTime.now().getYear() + "-" + String.format("%06d", new Random().nextInt(1000000));
  }
}
