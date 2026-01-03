# Module 06: Checkout & Payment

**Status:** ✅ Complete (100%)  
**Priority:** 🔴 CRITICAL  
**Sprint:** Pre-Sprint (Foundation)  
**Completed Date:** December 2025

---

## 📋 Overview

Complete checkout and payment processing system with Stripe integration. Handles order creation, payment intent creation, payment confirmation, and order finalization.

**Key Features:**
- Create orders from cart
- Stripe payment integration
- Payment intent creation
- Payment confirmation
- Order status tracking
- Transaction history
- Voucher/discount application

---

## 🎯 Business Rules

1. **Authorization:** Must be authenticated to checkout
2. **Cart Validation:** Cart must not be empty and all items must be available
3. **Address Required:** Shipping address must be provided
4. **Payment Methods:** Currently supports Stripe (cards)
5. **Order Creation:** Order is created before payment
6. **Payment Confirmation:** Order status updates after successful payment
7. **Stock Reduction:** Stock decreases only after successful payment
8. **Email Notifications:** Confirmation emails sent on successful orders

---

## 📡 API Endpoints

### Endpoint Summary Table

| Method | Endpoint | Description | Auth | Status |
|--------|----------|-------------|------|--------|
| POST | `/api/checkout/create-order` | Initialize order | Required | ✅ Complete |
| POST | `/api/payment/stripe/intent` | Create payment intent | Required | ✅ Complete |
| POST | `/api/payment/stripe/confirm` | Confirm payment | Required | ✅ Complete |
| GET | `/api/transactions/history` | Payment history | Required | ✅ Complete |
| GET | `/api/checkout/summary` | Checkout summary | Required | ✅ Complete |

---

## 🔧 Implementation Details

### 1. Create Order
**POST** `/api/checkout/create-order`

**Request Body:**
```json
{
  "shippingAddressId": 10,
  "voucherCode": "SAVE10",
  "notes": "Please handle with care"
}
```

**Validation:**
- Cart must not be empty
- Shipping address must exist and belong to user
- All cart items must be available and in stock
- Voucher code validated if provided

**Response:** `201 Created`
```json
{
  "success": true,
  "data": {
    "orderId": 1001,
    "orderNumber": "ORD-2026-001001",
    "status": "PENDING",
    "items": [
      {
        "id": 5001,
        "listing": {
          "id": 301,
          "book": {
            "title": "Clean Code",
            "coverImage": "..."
          },
          "seller": {
            "id": 50,
            "username": "bookstore_pro"
          }
        },
        "quantity": 2,
        "pricePerUnit": 45.99,
        "subtotal": 91.98
      }
    ],
    "shippingAddress": {
      "recipientName": "John Doe",
      "street": "123 Main St",
      "city": "Ho Chi Minh City",
      "country": "Vietnam",
      "phone": "+84123456789"
    },
    "summary": {
      "subtotal": 91.98,
      "discount": 9.20,
      "shipping": 5.00,
      "tax": 9.70,
      "total": 97.48
    },
    "createdAt": "2026-01-01T10:00:00Z"
  }
}
```

**Business Logic:**
1. Validate cart items availability and stock
2. Create Order entity with PENDING status
3. Create OrderItems from cart items
4. Calculate totals (subtotal, shipping, tax, discount)
5. Apply voucher if provided
6. Store order but don't reduce stock yet
7. Return order details for payment

**Backend Files:**
- `CheckoutController.java` ✅
- `CheckoutService.java` ✅
- `Order.java` entity ✅

---

### 2. Create Payment Intent
**POST** `/api/payment/stripe/intent`

**Request Body:**
```json
{
  "orderId": 1001,
  "currency": "USD"
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "clientSecret": "pi_3abc123_secret_def456",
    "paymentIntentId": "pi_3abc123",
    "amount": 9748,
    "currency": "usd"
  }
}
```

**Business Logic:**
1. Fetch order and validate it's PENDING
2. Create Stripe PaymentIntent with order amount
3. Store PaymentIntent ID in transaction record
4. Return client secret for frontend Stripe.js

**Integration:**
- Uses Stripe SDK
- Amount in cents (multiply by 100)
- Currency code lowercase

**Backend Files:**
- `TransactionController.java` ✅
- `StripeService.java` ✅

---

### 3. Confirm Payment
**POST** `/api/payment/stripe/confirm`

**Request Body:**
```json
{
  "orderId": 1001,
  "paymentIntentId": "pi_3abc123"
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "orderId": 1001,
    "orderNumber": "ORD-2026-001001",
    "orderStatus": "CONFIRMED",
    "paymentStatus": "COMPLETED",
    "transactionId": "txn_789",
    "paidAt": "2026-01-01T10:05:00Z"
  },
  "message": "Payment successful! Your order has been confirmed."
}
```

**Business Logic:**
1. Verify PaymentIntent status with Stripe
2. If payment successful:
    - Update Order status to CONFIRMED
    - Update Transaction status to COMPLETED
    - Reduce stock quantities for all order items
    - Clear user's cart
    - Send order confirmation email
    - Send notifications to sellers
3. If payment failed:
    - Update Order status to PAYMENT_FAILED
    - Return error message

**Error Cases:**
- `400 Bad Request`: Payment not successful
- `404 Not Found`: Order or PaymentIntent not found
- `409 Conflict`: Order already confirmed

---

### 4. Get Transaction History
**GET** `/api/transactions/history`

**Query Parameters:**
```
?page=0
&size=20
&status=COMPLETED|PENDING|FAILED
&startDate=2026-01-01
&endDate=2026-01-31
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": 789,
      "orderId": 1001,
      "orderNumber": "ORD-2026-001001",
      "amount": 97.48,
      "currency": "USD",
      "paymentMethod": "STRIPE",
      "status": "COMPLETED",
      "paymentIntentId": "pi_3abc123",
      "paidAt": "2026-01-01T10:05:00Z",
      "createdAt": "2026-01-01T10:00:00Z"
    }
  ],
  "meta": {
    "page": 0,
    "totalPages": 5,
    "totalItems": 92,
    "itemsPerPage": 20
  }
}
```

---

### 5. Get Checkout Summary
**GET** `/api/checkout/summary`

**Description:** Get pricing breakdown before order creation

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "listing": { ... },
        "quantity": 2,
        "pricePerUnit": 45.99,
        "subtotal": 91.98
      }
    ],
    "subtotal": 91.98,
    "shipping": 5.00,
    "tax": 9.70,
    "discount": 0.00,
    "total": 106.68,
    "availableVouchers": [
      {
        "code": "SAVE10",
        "description": "10% off your order",
        "discountPercent": 10
      }
    ]
  }
}
```

---

## 🗄️ Database Schema

### Order Entity
```java
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String orderNumber;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items = new ArrayList<>();
    
    @ManyToOne
    @JoinColumn(name = "shipping_address_id")
    private ShippingAddress shippingAddress;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal subtotal;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal discount;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal shipping;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal tax;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal total;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @ManyToOne
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
```

### Transaction Entity
```java
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal amount;
    
    private String currency;
    
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
    
    @Column(name = "payment_intent_id")
    private String paymentIntentId;
    
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
```

### Enums
```java
public enum OrderStatus {
    PENDING,          // Order created, awaiting payment
    CONFIRMED,        // Payment successful, order confirmed
    PROCESSING,       // Being prepared by sellers
    SHIPPED,          // All items shipped
    DELIVERED,        // All items delivered
    CANCELLED,        // Order cancelled
    PAYMENT_FAILED    // Payment was unsuccessful
}

public enum PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REFUNDED
}

public enum PaymentMethod {
    STRIPE,
    PAYPAL,  // Future
    COD      // Future
}

public enum TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REFUNDED
}
```

---

## 📦 DTOs

### CreateOrderRequest
```java
public class CreateOrderRequest {
    @NotNull
    private Long shippingAddressId;
    
    private String voucherCode;
    
    @Size(max = 500)
    private String notes;
}
```

### CreatePaymentIntentRequest
```java
public class CreatePaymentIntentRequest {
    @NotNull
    private Long orderId;
    
    @NotBlank
    private String currency = "USD";
}
```

### ConfirmPaymentRequest
```java
public class ConfirmPaymentRequest {
    @NotNull
    private Long orderId;
    
    @NotBlank
    private String paymentIntentId;
}
```

---

## 🧪 Testing Status

### Unit Tests
- ✅ CheckoutService tests complete
- ✅ Order creation logic tested
- ✅ Payment intent creation tested
- ✅ Payment confirmation tested
- ✅ Voucher application tested
- ✅ Stock reduction tested
- ✅ Email notification tested

### Integration Tests
- ✅ All controller endpoints tested
- ✅ Stripe integration tested (with test keys)
- ✅ Order workflow tested end-to-end

**Test Coverage:** 88%

---

## 🔗 Dependencies

### Related Modules
- **Module 05:** Shopping Cart (cart items → order items)
- **Module 08:** Shipping Addresses (shipping address validation)
- **Module 17:** Vouchers (discount application)
- **Module 04:** Listings (stock reduction)

### External Services
- Stripe API
- Email Service
- SMS Service (optional for notifications)

---

## ✅ Verification Checklist

- ✅ Users can create orders from cart
- ✅ Shipping address is validated and stored
- ✅ Voucher codes are validated and applied
- ✅ Payment intents are created via Stripe
- ✅ Payment confirmation updates order status
- ✅ Stock quantities reduce after payment
- ✅ Cart is cleared after successful order
- ✅ Order confirmation emails are sent
- ✅ Seller notifications are sent
- ✅ Transaction history is recorded
- ✅ Failed payments are handled gracefully
- ✅ All endpoints return standardized format
- ✅ Unit test coverage ≥ 80%
- ✅ Integration tests pass
- ✅ Stripe webhooks handled (payment succeeded/failed)

---

## 📝 Notes

**Current Implementation:**
- Stripe integration is fully functional
- Order creation and payment flow works end-to-end
- Stock management integrated
- Email notifications implemented

**Stripe Configuration:**
- Test keys used in development
- Production keys in environment variables
- Webhook endpoint configured for payment events

**Future Enhancements:**
- Add PayPal integration
- Add Cash on Delivery (COD) option
- Add order editing (before payment)
- Add partial refunds
- Add subscription/recurring payments
- Add multi-currency support
- Add payment installment plans

**Security Considerations:**
- Stripe keys stored in environment variables
- Payment intent IDs validated
- Order ownership verified before payment confirmation
- Double-payment prevention implemented

---

## ✔️ Sign-off

**Developer:** Backend Team - Date: Dec 2025  
**Reviewer:** Tech Lead - Date: Dec 2025  
**QA:** QA Team - Date: Dec 2025  
**Status:** ✅ Production Ready