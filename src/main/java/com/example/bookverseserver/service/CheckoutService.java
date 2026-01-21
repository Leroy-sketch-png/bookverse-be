package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.Order.CreateCheckoutRequest;
import com.example.bookverseserver.dto.request.Order.UpdateCheckoutSessionRequest;
import com.example.bookverseserver.dto.response.Order.*;
import com.example.bookverseserver.dto.response.Order.CheckoutSessionResponse.*;
import com.example.bookverseserver.dto.response.ShippingAddress.ShippingAddressResponse;
import com.example.bookverseserver.entity.Order_Payment.*;
import com.example.bookverseserver.entity.Product.Listing;
import com.example.bookverseserver.entity.User.ShippingAddress;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.entity.User.UserProfile;
import com.example.bookverseserver.enums.OrderStatus;
import com.example.bookverseserver.enums.PaymentStatus;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.exception.OutOfStockException;
import com.example.bookverseserver.mapper.ShippingAddressMapper;
import com.example.bookverseserver.repository.*;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.example.bookverseserver.configuration.DemoModeConfig;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Transactional(readOnly = true)  // P0: Prevent LazyInitializationException & connection leaks
public class CheckoutService {

    CartRepository cartRepository;
    CartItemRepository cartItemRepository;
    OrderRepository orderRepository;
    OrderItemRepository orderItemRepository;
    CheckoutSessionRepository checkoutSessionRepository;
    OrderTimelineRepository orderTimelineRepository;
    ListingRepository listingRepository;
    UserRepository userRepository;
    ShippingAddressRepository shippingAddressRepository;
    TransactionRepository transactionRepository;
    VoucherRepository voucherRepository;
    VoucherService voucherService;
    ShippingAddressMapper shippingAddressMapper;
    SmsService smsService;
    EmailService emailService;
    DemoModeConfig demoModeConfig;

    // Cryptographically secure random for order numbers
    @NonFinal
    static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @NonFinal
    @Value("${checkout.tax-rate:0.10}")
    BigDecimal taxRate;

    @NonFinal
    @Value("${checkout.shipping-flat-fee:30000}")
    BigDecimal shippingFlatFee;

    @NonFinal
    @Value("${stripe.publishable.key:pk_test_placeholder}")
    String stripePublishableKey;

    // ============================================================================
    // NEW API: Step 1 - Create Session from Cart
    // ============================================================================
    
    @Transactional
    public CheckoutSessionResponse createSession(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Get user's cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        if (cart.getCartItems().isEmpty()) {
            throw new AppException(ErrorCode.CART_EMPTY);
        }

        // Validate stock
        validateStock(cart);

        // Calculate initial totals (no voucher yet)
        BigDecimal subtotal = cart.getTotalPrice();
        BigDecimal tax = subtotal.multiply(taxRate);
        BigDecimal shipping = shippingFlatFee;
        BigDecimal total = subtotal.add(tax).add(shipping);

        // Check for existing pending session for this cart (prevents duplicate constraint violation)
        Optional<CheckoutSession> existingSession = checkoutSessionRepository.findPendingByCartId(cart.getId());
        if (existingSession.isPresent()) {
            CheckoutSession session = existingSession.get();
            // Update the session with fresh totals and extend expiry
            session.setAmount(total);
            session.setExpiresAt(LocalDateTime.now().plusHours(24));
            CheckoutSession updatedSession = checkoutSessionRepository.save(session);
            log.info("Reusing existing checkout session {} for user {}", updatedSession.getId(), userId);
            return buildSessionResponse(updatedSession, cart, subtotal, tax, shipping, BigDecimal.ZERO, null);
        }

        // Create checkout session (no order yet - order is created on complete)
        CheckoutSession session = CheckoutSession.builder()
                .user(user)
                .cart(cart)
                .amount(total)
                .currency("VND")
                .status("PENDING")
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        CheckoutSession savedSession = checkoutSessionRepository.save(session);
        log.info("Created checkout session {} for user {}", savedSession.getId(), userId);

        return buildSessionResponse(savedSession, cart, subtotal, tax, shipping, BigDecimal.ZERO, null);
    }

    // ============================================================================
    // Get Session
    // ============================================================================
    
    @Transactional(readOnly = true)
    public CheckoutSessionResponse getSession(Long userId, Long sessionId) {
        CheckoutSession session = getValidSession(userId, sessionId);
        Cart cart = session.getCart();
        
        BigDecimal subtotal = cart.getTotalPrice();
        BigDecimal tax = subtotal.multiply(taxRate);
        BigDecimal shipping = shippingFlatFee;
        BigDecimal discount = BigDecimal.ZERO;
        VoucherInfoDTO voucherInfo = null;
        
        // Check for applied voucher
        if (session.getClientSecret() != null && session.getClientSecret().startsWith("voucher:")) {
            String voucherCode = session.getClientSecret().substring(8);
            try {
                Voucher voucher = voucherRepository.findByCode(voucherCode).orElse(null);
                if (voucher != null) {
                    discount = voucherService.calculateDiscount(voucherCode, subtotal);
                    voucherInfo = VoucherInfoDTO.builder()
                            .code(voucher.getCode())
                            .discountType(voucher.getDiscountType().name())
                            .discountValue(voucher.getDiscountValue())
                            .discountAmount(discount)
                            .build();
                }
            } catch (Exception e) {
                // Voucher no longer valid, ignore
            }
        }
        
        return buildSessionResponse(session, cart, subtotal, tax, shipping, discount, voucherInfo);
    }

    // ============================================================================
    // Step 2: Update Session (Set Shipping Address)
    // ============================================================================
    
    @Transactional
    public CheckoutSessionResponse updateSession(Long userId, Long sessionId, UpdateCheckoutSessionRequest request) {
        CheckoutSession session = getValidSession(userId, sessionId);
        User user = session.getUser();
        
        Long addressId = null;
        
        // Option 1: Inline address creation
        if (request.getShippingAddress() != null) {
            UpdateCheckoutSessionRequest.InlineShippingAddressRequest inlineAddr = request.getShippingAddress();
            
            // Create new shipping address
            ShippingAddress newAddress = ShippingAddress.builder()
                    .user(user)
                    .fullName(inlineAddr.getFullName())
                    .phoneNumber(inlineAddr.getPhoneNumber())
                    .addressLine1(inlineAddr.getAddressLine1())
                    .addressLine2(inlineAddr.getAddressLine2())
                    .city(inlineAddr.getCity())
                    .postalCode(inlineAddr.getPostalCode())
                    .country(inlineAddr.getCountry() != null ? inlineAddr.getCountry() : "Vietnam")
                    .provinceId(inlineAddr.getProvinceId())
                    .district(inlineAddr.getDistrict())
                    .districtId(inlineAddr.getDistrictId())
                    .ward(inlineAddr.getWard())
                    .wardCode(inlineAddr.getWardCode())
                    .note(inlineAddr.getNote())
                    .isDefault(Boolean.TRUE.equals(inlineAddr.getIsDefault()))
                    .build();
            
            // Handle default address logic
            if (Boolean.TRUE.equals(inlineAddr.getIsDefault())) {
                shippingAddressRepository.resetDefaultAddress(userId);
            }
            
            ShippingAddress savedAddress = shippingAddressRepository.save(newAddress);
            addressId = savedAddress.getId();
        }
        // Option 2: Select existing address by ID
        else if (request.getShippingAddressId() != null) {
            ShippingAddress address = shippingAddressRepository.findById(request.getShippingAddressId())
                    .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));
            
            if (!address.getUser().getId().equals(userId)) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
            
            addressId = address.getId();
        }
        
        // Store selected address ID in session
        if (addressId != null) {
            session.setPaymentIntentId("addr:" + addressId);
            session.setStatus("SHIPPING_SELECTED");
        }
        
        checkoutSessionRepository.save(session);
        
        return getSession(userId, sessionId);
    }

    // ============================================================================
    // Step 3: Apply Voucher
    // ============================================================================
    
    @Transactional
    public ApplyVoucherResponse applyVoucher(Long userId, Long sessionId, String code) {
        CheckoutSession session = getValidSession(userId, sessionId);
        Cart cart = session.getCart();
        
        BigDecimal subtotal = cart.getTotalPrice();
        
        // Validate and calculate discount (with per-user limit check)
        BigDecimal discountAmount = voucherService.calculateDiscountForUser(code, subtotal, userId);
        
        // Get voucher details
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));
        
        // Store voucher in session (using clientSecret field temporarily)
        session.setClientSecret("voucher:" + code);
        checkoutSessionRepository.save(session);
        
        // Calculate new total
        BigDecimal tax = subtotal.multiply(taxRate);
        BigDecimal shipping = shippingFlatFee;
        BigDecimal newTotal = subtotal.add(tax).add(shipping).subtract(discountAmount);
        
        return ApplyVoucherResponse.builder()
                .code(voucher.getCode())
                .discountType(voucher.getDiscountType().name())
                .discountValue(voucher.getDiscountValue())
                .discountAmount(discountAmount)
                .newTotal(newTotal)
                .build();
    }

    // ============================================================================
    // Remove Voucher
    // ============================================================================
    
    @Transactional
    public CheckoutSessionResponse removeVoucher(Long userId, Long sessionId) {
        CheckoutSession session = getValidSession(userId, sessionId);
        
        // Clear voucher
        if (session.getClientSecret() != null && session.getClientSecret().startsWith("voucher:")) {
            session.setClientSecret(null);
            checkoutSessionRepository.save(session);
        }
        
        return getSession(userId, sessionId);
    }

    // ============================================================================
    // Step 4: Complete Checkout - Create Order + REAL Stripe Payment Intent
    // ============================================================================
    
    @Transactional
    public CompleteCheckoutResponse completeCheckout(Long userId, Long sessionId, String paymentMethod) {
        CheckoutSession session = getValidSession(userId, sessionId);
        User user = session.getUser();
        Cart cart = session.getCart();
        
        // Re-validate stock
        validateStock(cart);
        
        // Get shipping address
        Long shippingAddressId = null;
        ShippingAddress shippingAddress = null;
        if (session.getPaymentIntentId() != null && session.getPaymentIntentId().startsWith("addr:")) {
            shippingAddressId = Long.parseLong(session.getPaymentIntentId().substring(5));
            shippingAddress = shippingAddressRepository.findById(shippingAddressId)
                    .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));
        }
        
        // Calculate totals
        BigDecimal subtotal = cart.getTotalPrice();
        BigDecimal tax = subtotal.multiply(taxRate);
        BigDecimal shipping = shippingFlatFee;
        BigDecimal discount = BigDecimal.ZERO;
        String promoCode = null;
        
        // Check for voucher
        if (session.getClientSecret() != null && session.getClientSecret().startsWith("voucher:")) {
            promoCode = session.getClientSecret().substring(8);
            try {
                discount = voucherService.calculateDiscount(promoCode, subtotal);
            } catch (Exception e) {
                log.warn("Voucher {} no longer valid, ignoring", promoCode);
                promoCode = null;
            }
        }
        
        BigDecimal total = subtotal.add(tax).add(shipping).subtract(discount);
        
        // Create Order
        Order order = Order.builder()
                .user(user)
                .orderNumber(generateOrderNumber())
                .status(OrderStatus.PENDING)
                .subtotal(subtotal)
                .totalAmount(total) // Required NOT NULL column
                .tax(tax)
                .shipping(shipping)
                .discount(discount)
                .total(total)
                .promoCode(promoCode)
                .shippingAddress(shippingAddress)
                .build();
        
        Order savedOrder = orderRepository.save(order);
        
        // Create Order Items and reserve stock ATOMICALLY
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = OrderItem.fromCartItem(cartItem, savedOrder);
            orderItems.add(orderItem);
            
            // Reserve stock atomically - prevents overselling via SQL WHERE clause
            int reserved = listingRepository.reserveStock(
                    cartItem.getListing().getId(), 
                    cartItem.getQuantity()
            );
            if (reserved == 0) {
                // Insufficient stock - rollback transaction
                throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
            }
        }
        orderItemRepository.saveAll(orderItems);
        
        // Add to timeline
        OrderTimeline timeline = OrderTimeline.builder()
                .order(savedOrder)
                .status("PENDING")
                .note("Order created, awaiting payment")
                .build();
        orderTimelineRepository.save(timeline);
        
        // Record voucher usage (after successful order creation)
        if (promoCode != null) {
            voucherService.recordVoucherUsage(promoCode, user);
        }
        
        // Payment Intent - Demo Mode or Real Stripe
        String paymentIntentId;
        String clientSecret;
        String paymentStatus;
        
        if (demoModeConfig.isEnabled()) {
            // DEMO MODE: Simulate payment success without Stripe API
            paymentIntentId = demoModeConfig.generateFakePaymentIntentId();
            clientSecret = demoModeConfig.generateFakeClientSecret();
            paymentStatus = "requires_payment_method"; // Frontend will auto-confirm in demo mode
            log.info("🎓 DEMO MODE: Simulated payment intent {} for order {}", paymentIntentId, savedOrder.getId());
        } else {
            // REAL MODE: Create Stripe Payment Intent
            try {
                long amountInCents = total.multiply(BigDecimal.valueOf(100)).longValue();
                
                PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                        .setAmount(amountInCents)
                        .setCurrency("vnd")
                        .setAutomaticPaymentMethods(
                                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                        .setEnabled(true)
                                        .build()
                        )
                        .putMetadata("order_id", String.valueOf(savedOrder.getId()))
                        .putMetadata("user_id", String.valueOf(user.getId()))
                        .putMetadata("order_number", savedOrder.getOrderNumber())
                        .build();
                
                PaymentIntent stripeIntent = PaymentIntent.create(params);
                paymentIntentId = stripeIntent.getId();
                clientSecret = stripeIntent.getClientSecret();
                paymentStatus = stripeIntent.getStatus();
                log.info("Created Stripe payment intent {} for order {}", paymentIntentId, savedOrder.getId());
                
            } catch (StripeException e) {
                log.error("Stripe error creating payment intent", e);
                throw new AppException(ErrorCode.PAYMENT_PROCESSING_ERROR);
            }
        }
        
        // Save payment record
        Payment payment = Payment.builder()
                .order(savedOrder)
                .user(user)
                .paymentIntentId(paymentIntentId)
                .amount(total)
                .status(PaymentStatus.PENDING)
                .paymentMethod(demoModeConfig.isEnabled() ? "DEMO" : "STRIPE")
                .build();
        transactionRepository.save(payment);
        
        // Update checkout session
        session.setOrder(savedOrder);
        session.setPaymentIntentId(paymentIntentId);
        session.setClientSecret(clientSecret);
        session.setStatus("READY_FOR_PAYMENT");
        checkoutSessionRepository.save(session);
        
        // Clear cart using direct SQL to avoid optimistic locking issues
        cartItemRepository.deleteAllByCartIdDirect(cart.getId());
        cart.setTotalPrice(BigDecimal.ZERO);
        cartRepository.save(cart);
        
        // Send order confirmation SMS to buyer (async, fails silently)
        sendOrderConfirmationSms(savedOrder, shippingAddress);
        
        // Send order confirmation EMAIL to buyer (async, fails silently)
        sendOrderConfirmationEmail(savedOrder);
        
        // Notify sellers about new order (async, fails silently)
        notifySellersNewOrder(savedOrder);
        
        return CompleteCheckoutResponse.builder()
                .orderId(savedOrder.getId())
                .orderNumber(savedOrder.getOrderNumber())
                .demoMode(demoModeConfig.isEnabled())
                .paymentIntent(CompleteCheckoutResponse.PaymentIntentDTO.builder()
                        .id(paymentIntentId)
                        .clientSecret(clientSecret)
                        .amount(total)
                        .currency("VND")
                        .status(paymentStatus)
                        .publishableKey(stripePublishableKey)
                        .build())
                .build();
    }

    // ============================================================================
    // DEMO MODE: Simulate Payment Success
    // ============================================================================
    
    @Transactional
    public void demoConfirmPayment(Long userId, Long orderId) {
        if (!demoModeConfig.isEnabled()) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        
        if (!order.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        // Update order status to PROCESSING (simulating payment success)
        order.setStatus(OrderStatus.PROCESSING);
        orderRepository.save(order);
        
        // Update payment record
        Payment payment = transactionRepository.findByOrderId(orderId).stream().findFirst().orElse(null);
        if (payment != null) {
            payment.setStatus(PaymentStatus.COMPLETED);
            transactionRepository.save(payment);
        }
        
        // Add timeline entry
        OrderTimeline timeline = OrderTimeline.builder()
                .order(order)
                .status("PROCESSING")
                .note("Payment confirmed (Demo Mode)")
                .build();
        orderTimelineRepository.save(timeline);
        
        log.info("🎓 DEMO MODE: Payment confirmed for order {}", orderId);
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================
    
    private CheckoutSession getValidSession(Long userId, Long sessionId) {
        // Use eager-loading query to prevent LazyInitializationException
        CheckoutSession session = checkoutSessionRepository.findByIdWithFullCart(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.CHECKOUT_SESSION_NOT_FOUND));
        
        if (!session.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.CHECKOUT_SESSION_EXPIRED);
        }
        
        return session;
    }
    
    private CheckoutSessionResponse buildSessionResponse(
            CheckoutSession session, 
            Cart cart, 
            BigDecimal subtotal, 
            BigDecimal tax, 
            BigDecimal shipping, 
            BigDecimal discount,
            VoucherInfoDTO voucherInfo) {
        
        User user = session.getUser();
        
        // Build cart items list (Vision structure)
        List<CartItemDTO> cartItems = cart.getCartItems().stream()
                .map(item -> {
                    Listing listing = item.getListing();
                    User seller = listing.getSeller();
                    UserProfile sellerProfile = seller.getUserProfile();
                    
                    return CartItemDTO.builder()
                            .id(item.getId())
                            .listing(ListingDTO.builder()
                                    .id(listing.getId())
                                    .book(BookDTO.builder()
                                            .title(listing.getBookMeta().getTitle())
                                            .coverImage(listing.getBookMeta().getCoverImageUrl())
                                            .build())
                                    .price(listing.getPrice())
                                    .finalPrice(listing.getPrice()) // Could include promotions
                                    .quantity(listing.getQuantity())
                                    .condition(listing.getCondition().name())
                                    .seller(SellerDTO.builder()
                                            .id(seller.getId())
                                            .name(sellerProfile != null ? sellerProfile.getFullName() : seller.getUsername())
                                            .build())
                                    .build())
                            .quantity(item.getQuantity())
                            .build();
                })
                .collect(Collectors.toList());
        
        // Build cart DTO with summary
        BigDecimal total = subtotal.add(tax).add(shipping).subtract(discount);
        CartDTO cartDTO = CartDTO.builder()
                .id(cart.getId())
                .cartItems(cartItems)
                .summary(SummaryDTO.builder()
                        .subtotal(subtotal)
                        .discount(discount)
                        .total(total)
                        .build())
                .itemCount(cart.getCartItems().size())
                .build();
        
        // Get user's shipping addresses (per Vision)
        List<ShippingAddress> userAddresses = shippingAddressRepository.findByUserId(user.getId());
        List<ShippingAddressResponse> addressResponses = userAddresses.stream()
                .map(shippingAddressMapper::toShippingAddressResponse)
                .collect(Collectors.toList());
        
        // Extract selected address ID if set
        Long selectedAddressId = null;
        if (session.getPaymentIntentId() != null && session.getPaymentIntentId().startsWith("addr:")) {
            selectedAddressId = Long.parseLong(session.getPaymentIntentId().substring(5));
        }
        
        return CheckoutSessionResponse.builder()
                .sessionId(session.getId())
                .status(session.getStatus())
                .cart(cartDTO)
                .shippingAddresses(addressResponses)
                .selectedAddressId(selectedAddressId)
                .voucher(voucherInfo)
                .expiresAt(session.getExpiresAt())
                .createdAt(session.getCreatedAt())
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

    /**
     * Generate cryptographically secure order number.
     * Format: BV-YYYY-XXXXXXXXXXXX (12 alphanumeric chars)
     * Uses SecureRandom to prevent order number enumeration attacks.
     */
    private String generateOrderNumber() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // No O/0/I/1 to avoid confusion
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(SECURE_RANDOM.nextInt(chars.length())));
        }
        return "BV-" + LocalDateTime.now().getYear() + "-" + sb.toString();
    }

    // ============================================================================
    // LEGACY: Keep for backward compatibility
    // ============================================================================
    
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
            throw new AppException(ErrorCode.CART_EMPTY);
        }

        validateStock(cart);

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

        // Reserve stock atomically for each item
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = OrderItem.fromCartItem(cartItem, savedOrder);
            orderItems.add(orderItem);

            // Reserve stock atomically - prevents overselling via SQL WHERE clause
            int reserved = listingRepository.reserveStock(
                    cartItem.getListing().getId(),
                    cartItem.getQuantity()
            );
            if (reserved == 0) {
                // Insufficient stock - rollback transaction
                throw new AppException(ErrorCode.INSUFFICIENT_STOCK);
            }
        }
        orderItemRepository.saveAll(orderItems);

        OrderTimeline timeline = OrderTimeline.builder()
                .order(savedOrder)
                .status("PENDING")
                .note("Order created from checkout session")
                .build();
        orderTimelineRepository.save(timeline);

        CheckoutSession session = CheckoutSession.builder()
                .user(currentUser)
                .cart(cart)
                .order(savedOrder)
                .amount(total)
                .currency("VND")
                .status("PENDING")
                .expiresAt(LocalDateTime.now().plusHours(24))
                .paymentIntentId("pi_" + UUID.randomUUID().toString())
                .clientSecret("secret_" + UUID.randomUUID().toString())
                .build();

        CheckoutSession savedSession = checkoutSessionRepository.save(session);

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
    
    /**
     * Send order confirmation SMS to buyer.
     * Fails silently - SMS is not critical to checkout flow.
     */
    private void sendOrderConfirmationSms(Order order, ShippingAddress shippingAddress) {
        try {
            String buyerPhone = null;
            if (shippingAddress != null && shippingAddress.getPhoneNumber() != null) {
                buyerPhone = shippingAddress.getPhoneNumber();
            } else if (order.getUser().getUserProfile() != null && order.getUser().getUserProfile().getPhoneNumber() != null) {
                buyerPhone = order.getUser().getUserProfile().getPhoneNumber();
            }
            
            if (buyerPhone == null || buyerPhone.isBlank()) {
                log.debug("No phone number for order {} buyer, skipping SMS", order.getId());
                return;
            }
            
            String totalAmount = order.getTotalAmount().toPlainString();
            boolean sent = smsService.sendOrderConfirmation(buyerPhone, order.getOrderNumber(), totalAmount);
            
            if (sent) {
                log.info("Order confirmation SMS sent to {} for order {}", buyerPhone, order.getOrderNumber());
            }
        } catch (Exception e) {
            log.warn("Failed to send order confirmation SMS for order {}: {}", order.getId(), e.getMessage());
            // Don't rethrow - SMS failure shouldn't break checkout flow
        }
    }
    
    /**
     * Notify all sellers in the order about the new order.
     * Each order item may belong to a different seller.
     * Fails silently - notifications shouldn't break checkout.
     */
    private void notifySellersNewOrder(Order order) {
        try {
            // Group items by seller to send one notification per seller
            java.util.Map<Long, java.util.List<OrderItem>> itemsBySeller = order.getItems().stream()
                    .filter(item -> item.getListing() != null && item.getListing().getSeller() != null)
                    .collect(java.util.stream.Collectors.groupingBy(item -> item.getListing().getSeller().getId()));
            
            for (java.util.Map.Entry<Long, java.util.List<OrderItem>> entry : itemsBySeller.entrySet()) {
                try {
                    User seller = entry.getValue().get(0).getListing().getSeller();
                    UserProfile profile = seller.getUserProfile();
                    
                    // Get seller phone
                    String sellerPhone = null;
                    if (profile != null && profile.getPhoneNumber() != null && !profile.getPhoneNumber().isBlank()) {
                        sellerPhone = profile.getPhoneNumber();
                    }
                    
                    // Calculate seller's portion of the order
                    BigDecimal sellerTotal = entry.getValue().stream()
                            .map(OrderItem::getSubtotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    int itemCount = entry.getValue().size();
                    String sellerName = profile != null && profile.getDisplayName() != null 
                            ? profile.getDisplayName() : seller.getUsername();
                    
                    // Send SMS if phone available
                    if (sellerPhone != null) {
                        String message = String.format(
                            "New order! %s, you have %d new item(s) to fulfill. Order #%s, total: $%s. Check your dashboard!",
                            sellerName, itemCount, order.getOrderNumber(), sellerTotal.setScale(2).toPlainString()
                        );
                        smsService.sendSms(sellerPhone, message);
                        log.info("Sent new order SMS to seller {} for order {}", seller.getId(), order.getOrderNumber());
                    }
                    
                    // Send email notification to seller
                    String buyerName = order.getUser().getUserProfile() != null && order.getUser().getUserProfile().getDisplayName() != null
                            ? order.getUser().getUserProfile().getDisplayName()
                            : order.getUser().getUsername();
                    emailService.sendNewOrderNotification(
                            seller.getEmail(),
                            sellerName,
                            order.getOrderNumber(),
                            buyerName,
                            sellerTotal.setScale(2).toPlainString(),
                            itemCount
                    );
                    
                } catch (Exception e) {
                    log.warn("Failed to notify seller {}: {}", entry.getKey(), e.getMessage());
                    // Continue notifying other sellers
                }
            }
        } catch (Exception e) {
            log.warn("Failed to notify sellers for order {}: {}", order.getId(), e.getMessage());
            // Don't rethrow - notification failure shouldn't break checkout
        }
    }
    
    /**
     * Send order confirmation email to buyer.
     * Fails silently - email is not critical to checkout flow.
     */
    private void sendOrderConfirmationEmail(Order order) {
        try {
            User buyer = order.getUser();
            String buyerEmail = buyer.getEmail();
            String buyerName = buyer.getUserProfile() != null && buyer.getUserProfile().getDisplayName() != null
                    ? buyer.getUserProfile().getDisplayName()
                    : buyer.getUsername();
            
            int itemCount = order.getItems() != null ? order.getItems().size() : 0;
            String totalAmount = order.getTotalAmount().setScale(2).toPlainString();
            
            emailService.sendOrderConfirmation(
                    buyerEmail,
                    buyerName,
                    order.getOrderNumber(),
                    totalAmount,
                    itemCount
            );
            
            log.info("Order confirmation email sent to {} for order {}", buyerEmail, order.getOrderNumber());
        } catch (Exception e) {
            log.warn("Failed to send order confirmation email for order {}: {}", order.getId(), e.getMessage());
            // Don't rethrow - email failure shouldn't break checkout flow
        }
    }
}
