# Module 07: Orders Management

**Status:** ⚠️ Needs Refinement (25% Complete)  
**Priority:** 🟡 High  
**Sprint:** Sprint 2 (Week 3)  
**Assigned To:** _[To be assigned]_  
**Estimated Effort:** 2 days

---

## 📋 Overview

Order management for buyers to view their order history, track shipments, and manage returns. This is the buyer-side order interface.

**Key Features:**
- View order history with filters
- Get detailed order information
- Track order status
- Cancel orders (before shipping)
- Download invoices
- View order timeline

---

## 🎯 Business Rules

1. **Authorization:** Users can only view their own orders
2. **Cancellation:** Orders can be cancelled only if status is PENDING or CONFIRMED
3. **Auto-Status:** Order status auto-updates based on all item statuses
4. **Tracking:** Users get notifications on status changes
5. **Invoices:** PDF invoices generated after payment confirmation

---

## 📡 API Endpoints

### Endpoint Summary Table

| Method | Endpoint | Description | Auth | Status |
|--------|----------|-------------|------|--------|
| GET | `/api/orders` | List user's orders | Required | ⚠️ Needs Filters |
| GET | `/api/orders/{id}` | Get order details | Required | ⚠️ Needs Enhancement |
| POST | `/api/orders/{id}/cancel` | Cancel order | Required | ❌ Missing |
| GET | `/api/orders/{id}/invoice` | Download invoice | Required | ❌ Missing |
| GET | `/api/orders/{id}/tracking` | Get tracking info | Required | ❌ Missing |

---

## 🔧 Implementation Details

### 1. List Orders ⚠️ NEEDS UPDATE
**GET** `/api/orders`

**Current Issues:**
- ❌ Returns all orders instead of auto-filtering by user
- ❌ Missing status filter
- ❌ Missing date range filter

**Required Query Parameters:**
```
?status=PENDING|CONFIRMED|PROCESSING|SHIPPED|DELIVERED|CANCELLED
&startDate=2026-01-01
&endDate=2026-01-31
&page=0
&size=20
&sortBy=createdAt|total
&sortOrder=desc|asc
```

**Required Response Format:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1001,
      "orderNumber": "ORD-2026-001001",
      "status": "SHIPPED",
      "paymentStatus": "COMPLETED",
      "items": [
        {
          "id": 5001,
          "book": {
            "title": "Clean Code",
            "coverImage": "...",
            "author": "Robert C. Martin"
          },
          "seller": {
            "username": "bookstore_pro",
            "businessName": "Pro Book Store"
          },
          "quantity": 2,
          "pricePerUnit": 45.99,
          "itemStatus": "SHIPPED",
          "trackingNumber": "TRK123456789"
        }
      ],
      "total": 97.48,
      "createdAt": "2026-01-01T10:00:00Z",
      "updatedAt": "2026-01-02T14:00:00Z"
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

**Required Backend Changes:**

```java
@GetMapping
public ResponseEntity<PagedResponse<OrderDto>> getOrders(
    @AuthenticationPrincipal User currentUser,
    @RequestParam(required = false) OrderStatus status,
    @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate startDate,
    @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate endDate,
    @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
    @RequestParam(required = false, defaultValue = "desc") String sortOrder,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size
) {
    Specification<Order> spec = Specification.where(null);
    
    // Auto-filter by current user
    spec = spec.and((root, query, cb) ->
        cb.equal(root.get("user").get("id"), currentUser.getId()));
    
    if (status != null) {
        spec = spec.and((root, query, cb) ->
            cb.equal(root.get("status"), status));
    }
    
    if (startDate != null && endDate != null) {
        spec = spec.and((root, query, cb) ->
            cb.between(root.get("createdAt"),
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59)));
    }
    
    Sort sort = sortOrder.equalsIgnoreCase("asc")
        ? Sort.by(sortBy).ascending()
        : Sort.by(sortBy).descending();
    
    Pageable pageable = PageRequest.of(page, size, sort);
    Page<Order> orders = orderRepository.findAll(spec, pageable);
    
    return ResponseEntity.ok(PagedResponse.of(orders, orderMapper::toDto));
}
```

---

### 2. Get Order Details ⚠️ NEEDS ENHANCEMENT
**GET** `/api/orders/{id}`

**Current Issues:**
- ❌ May not include full item details with tracking
- ❌ Missing timeline/history

**Required Response:**
```json
{
  "success": true,
  "data": {
    "id": 1001,
    "orderNumber": "ORD-2026-001001",
    "status": "SHIPPED",
    "paymentStatus": "COMPLETED",
    "items": [
      {
        "id": 5001,
        "listing": {
          "id": 301,
          "book": {
            "id": 123,
            "title": "Clean Code",
            "author": "Robert C. Martin",
            "coverImage": "...",
            "isbn": "978-0132350884"
          },
          "condition": "NEW"
        },
        "seller": {
          "id": 50,
          "username": "bookstore_pro",
          "businessName": "Pro Book Store",
          "avatar": "...",
          "rating": 4.8
        },
        "quantity": 2,
        "pricePerUnit": 45.99,
        "subtotal": 91.98,
        "itemStatus": "SHIPPED",
        "trackingNumber": "TRK123456789",
        "carrier": "Vietnam Post",
        "shippedAt": "2026-01-02T10:00:00Z",
        "estimatedDelivery": "2026-01-05"
      }
    ],
    "shippingAddress": {
      "recipientName": "John Doe",
      "street": "123 Main St",
      "ward": "Ward 1",
      "district": "District 1",
      "city": "Ho Chi Minh City",
      "postalCode": "70000",
      "country": "Vietnam",
      "phone": "+84123456789"
    },
    "payment": {
      "method": "STRIPE",
      "status": "COMPLETED",
      "amount": 97.48,
      "currency": "USD",
      "paidAt": "2026-01-01T10:05:00Z"
    },
    "summary": {
      "subtotal": 91.98,
      "discount": 9.20,
      "shipping": 5.00,
      "tax": 9.70,
      "total": 97.48
    },
    "voucher": {
      "code": "SAVE10",
      "discount": 9.20
    },
    "timeline": [
      {
        "status": "PENDING",
        "timestamp": "2026-01-01T10:00:00Z",
        "description": "Order placed"
      },
      {
        "status": "CONFIRMED",
        "timestamp": "2026-01-01T10:05:00Z",
        "description": "Payment confirmed"
      },
      {
        "status": "PROCESSING",
        "timestamp": "2026-01-02T09:00:00Z",
        "description": "Order is being prepared"
      },
      {
        "status": "SHIPPED",
        "timestamp": "2026-01-02T14:00:00Z",
        "description": "Order shipped"
      }
    ],
    "canCancel": false,
    "canReview": false,
    "createdAt": "2026-01-01T10:00:00Z",
    "updatedAt": "2026-01-02T14:00:00Z"
  }
}
```

**Required Enhancements:**
- Add full item details with tracking numbers
- Add order timeline showing status progression
- Add `canCancel` flag (true if status allows cancellation)
- Add `canReview` flag (true if delivered and not yet reviewed)
- Include voucher details if used

**Implementation:**
```java
@GetMapping("/{id}")
public ResponseEntity<OrderDetailDto> getOrderById(
    @PathVariable Long id,
    @AuthenticationPrincipal User currentUser
) {
    Order order = orderRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    
    // Verify ownership
    if (!order.getUser().getId().equals(currentUser.getId())) {
        throw new ForbiddenException("Access denied");
    }
    
    OrderDetailDto dto = orderMapper.toDetailDto(order);
    
    // Add timeline
    List<OrderTimelineDto> timeline = orderHistoryService.getTimeline(id);
    dto.setTimeline(timeline);
    
    // Set action flags
    dto.setCanCancel(orderService.canCancel(order));
    dto.setCanReview(orderService.canReview(order));
    
    return ResponseEntity.ok(ApiResponse.success(dto));
}
```

---

### 3. Cancel Order ❌ NEW ENDPOINT
**POST** `/api/orders/{id}/cancel`

**Request Body:**
```json
{
  "reason": "Changed my mind",
  "notes": "Additional details..."
}
```

**Validation:**
- Can only cancel if status is PENDING or CONFIRMED
- Cannot cancel if any item has been SHIPPED

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "orderId": 1001,
    "orderNumber": "ORD-2026-001001",
    "status": "CANCELLED",
    "cancelledAt": "2026-01-01T11:00:00Z",
    "refundStatus": "PROCESSING"
  },
  "message": "Order cancelled successfully. Refund will be processed within 5-7 business days."
}
```

**Business Logic:**
1. Validate order can be cancelled
2. Update order status to CANCELLED
3. Restore stock quantities for all items
4. Initiate refund process (if payment was made)
5. Send cancellation email to user
6. Notify sellers
7. Record cancellation reason

**Implementation:**
```java
@PostMapping("/{id}/cancel")
public ResponseEntity<ApiResponse<OrderCancellationDto>> cancelOrder(
    @PathVariable Long id,
    @RequestBody CancelOrderRequest request,
    @AuthenticationPrincipal User currentUser
) {
    OrderCancellationDto result = orderService.cancelOrder(
        id,
        currentUser.getId(),
        request
    );
    
    return ResponseEntity.ok(ApiResponse.success(
        result,
        "Order cancelled successfully"
    ));
}
```

**Service Method:**
```java
@Transactional
public OrderCancellationDto cancelOrder(Long orderId, Long userId, CancelOrderRequest request) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    
    // Verify ownership
    if (!order.getUser().getId().equals(userId)) {
        throw new ForbiddenException("Access denied");
    }
    
    // Validate cancellation
    if (!canCancel(order)) {
        throw new BadRequestException(
            "Order cannot be cancelled. Status: " + order.getStatus()
        );
    }
    
    // Update status
    order.setStatus(OrderStatus.CANCELLED);
    order.setCancelledAt(LocalDateTime.now());
    order.setCancellationReason(request.getReason());
    order.setCancellationNotes(request.getNotes());
    
    // Restore stock
    for (OrderItem item : order.getItems()) {
        Listing listing = item.getListing();
        listing.setStockQuantity(listing.getStockQuantity() + item.getQuantity());
        listingRepository.save(listing);
    }
    
    // Initiate refund if payment was completed
    if (order.getPaymentStatus() == PaymentStatus.COMPLETED) {
        refundService.initiateRefund(order);
    }
    
    orderRepository.save(order);
    
    // Send notifications
    emailService.sendOrderCancellationEmail(order);
    notificationService.notifySellersOfCancellation(order);
    
    return orderMapper.toCancellationDto(order);
}

public boolean canCancel(Order order) {
    return order.getStatus() == OrderStatus.PENDING
        || order.getStatus() == OrderStatus.CONFIRMED;
}
```

---

### 4. Download Invoice ❌ NEW ENDPOINT
**GET** `/api/orders/{id}/invoice`

**Query Parameters:**
```
?format=pdf|html
```

**Response:** PDF file or HTML

**Headers:**
```
Content-Type: application/pdf
Content-Disposition: attachment; filename="invoice-ORD-2026-001001.pdf"
```

**Business Logic:**
- Generate PDF invoice with order details
- Include company information
- Include itemized list
- Include payment details
- Include tax breakdown

**Implementation:**
```java
@GetMapping("/{id}/invoice")
public ResponseEntity<byte[]> downloadInvoice(
    @PathVariable Long id,
    @RequestParam(defaultValue = "pdf") String format,
    @AuthenticationPrincipal User currentUser
) {
    Order order = orderRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    
    // Verify ownership
    if (!order.getUser().getId().equals(currentUser.getId())) {
        throw new ForbiddenException("Access denied");
    }
    
    byte[] invoice = invoiceService.generateInvoice(order, format);
    
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.setContentDisposition(
        ContentDisposition.builder("attachment")
            .filename("invoice-" + order.getOrderNumber() + ".pdf")
            .build()
    );
    
    return ResponseEntity.ok()
        .headers(headers)
        .body(invoice);
}
```

---

### 5. Get Tracking Info ❌ NEW ENDPOINT
**GET** `/api/orders/{id}/tracking`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "orderId": 1001,
    "orderNumber": "ORD-2026-001001",
    "currentStatus": "SHIPPED",
    "items": [
      {
        "orderItemId": 5001,
        "book": {
          "title": "Clean Code",
          "coverImage": "..."
        },
        "seller": {
          "businessName": "Pro Book Store"
        },
        "trackingNumber": "TRK123456789",
        "carrier": "Vietnam Post",
        "status": "SHIPPED",
        "shippedAt": "2026-01-02T10:00:00Z",
        "estimatedDelivery": "2026-01-05",
        "trackingEvents": [
          {
            "timestamp": "2026-01-02T10:00:00Z",
            "location": "Ho Chi Minh City",
            "description": "Package picked up"
          },
          {
            "timestamp": "2026-01-02T15:00:00Z",
            "location": "Sorting Center",
            "description": "In transit"
          }
        ]
      }
    ]
  }
}
```

**Note:** For MVP, tracking events can be mock data. Future integration with carrier APIs.

---

## 🗄️ Database Schema Updates

### Order Entity (ADD FIELDS)
```java
// Add to Order entity
@Column(name = "cancelled_at")
private LocalDateTime cancelledAt;

@Column(name = "cancellation_reason")
private String cancellationReason;

@Column(name = "cancellation_notes", columnDefinition = "TEXT")
private String cancellationNotes;

@Column(name = "invoice_url")
private String invoiceUrl;
```

### OrderHistory Entity (NEW)
```java
@Entity
@Table(name = "order_history")
public class OrderHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
```

---

## 📦 DTOs

### OrderDto (UPDATE)
```java
public class OrderDto {
    private Long id;
    private String orderNumber;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private List<OrderItemSummaryDto> items;
    private BigDecimal total;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### OrderDetailDto (NEW)
```java
public class OrderDetailDto extends OrderDto {
    private ShippingAddressDto shippingAddress;
    private PaymentDetailDto payment;
    private OrderSummaryDto summary;
    private VoucherDto voucher;
    private List<OrderTimelineDto> timeline;
    private Boolean canCancel;
    private Boolean canReview;
    private String cancellationReason;
    private LocalDateTime cancelledAt;
}
```

### CancelOrderRequest
```java
public class CancelOrderRequest {
    @NotBlank
    @Size(max = 200)
    private String reason;
    
    @Size(max = 500)
    private String notes;
}
```

### OrderTimelineDto
```java
public class OrderTimelineDto {
    private OrderStatus status;
    private LocalDateTime timestamp;
    private String description;
}
```

---

## 🧪 Testing Requirements

### Unit Tests
```java
@Test
void getOrders_AutoFiltersCurrentUser() {
    // Verify only current user's orders returned
}

@Test
void cancelOrder_ValidStatus_Success() {
    // Test successful cancellation
}

@Test
void cancelOrder_AlreadyShipped_ThrowsException() {
    // Cannot cancel shipped orders
}

@Test
void cancelOrder_RestoresStock() {
    // Verify stock quantities restored
}
```

---

## 🔗 Dependencies

### Related Modules
- **Module 06:** Checkout (order creation)
- **Module 11:** Seller Dashboard (seller view of same orders)
- **Module 09:** Reviews (can review after delivery)

---

## ✅ Acceptance Criteria

- [ ] Orders list auto-filters by current user
- [ ] Orders can be filtered by status and date range
- [ ] Order details include full tracking information
- [ ] Order timeline shows status progression
- [ ] Users can cancel eligible orders
- [ ] Stock is restored on cancellation
- [ ] Refunds initiated on payment cancellation
- [ ] Invoices can be downloaded as PDF
- [ ] Tracking information is displayed
- [ ] All endpoints return standardized format
- [ ] Unit test coverage ≥ 80%

---

## 📅 Timeline

| Task | Estimated Time | Status |
|------|----------------|--------|
| Add filters to list endpoint | 2 hours | ❌ |
| Enhance order details response | 2 hours | ❌ |
| Implement cancel order | 4 hours | ❌ |
| Implement invoice generation | 3 hours | ❌ |
| Implement tracking endpoint | 2 hours | ❌ |
| Create OrderHistory entity | 1 hour | ❌ |
| Write tests | 3 hours | ❌ |
| **Total** | **17 hours (2 days)** | |

---

## ✔️ Sign-off

**Developer:** _________________ Date: _______  
**Reviewer:** _________________ Date: _______  
**QA:** _________________ Date: _______