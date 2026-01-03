# Module 11: Seller Dashboard & Order Management

**Status:** ❌ Missing (0% Complete)  
**Priority:** 🔴 CRITICAL  
**Sprint:** Sprint 2 (Weeks 3-4)  
**Assigned To:** _[To be assigned]_  
**Estimated Effort:** 3 days

---

## 📋 Overview

Seller dashboard for managing orders that contain their products. Sellers need to view, filter, and update the status of orders containing items from their listings.

**Key Features:**
- View all orders containing seller's products
- Filter orders by status
- Update order shipping status
- View detailed order information
- Track fulfillment metrics

---

## 🎯 Business Rules

1. **Authorization:** Only users with ROLE_SELLER can access these endpoints
2. **Order Visibility:** Sellers only see their own items within orders
3. **Multi-Seller Orders:** An order may contain items from multiple sellers
4. **Status Updates:** Sellers can only update status for their own items
5. **Status Flow:** PENDING → PROCESSING → SHIPPED → DELIVERED
6. **Notifications:** Email sent to buyer when seller updates shipping status

---

## 📡 API Endpoints

### Endpoint Summary Table

| Method | Endpoint | Description | Auth | Status |
|--------|----------|-------------|------|--------|
| GET | `/api/seller/orders` | Get orders with seller's items | Seller | ❌ Not Implemented |
| GET | `/api/seller/orders/{orderId}` | Get order details | Seller | ❌ Not Implemented |
| PATCH | `/api/seller/orders/{orderId}/items/{itemId}/status` | Update item status | Seller | ❌ Not Implemented |
| GET | `/api/seller/orders/stats` | Order statistics | Seller | ❌ Not Implemented |

---

## 🔧 Implementation Details

### 1. Get Seller's Orders
**GET** `/api/seller/orders`

**Authorization:** `@PreAuthorize("hasRole('SELLER')")`

**Query Parameters:**
```
?status=PENDING|PROCESSING|SHIPPED|DELIVERED|CANCELLED
&page=0
&size=20
&startDate=2026-01-01
&endDate=2026-01-31
```

**Business Logic:**
- Find all OrderItems where `listing.seller = currentUser`
- Group by Order
- Return orders containing seller's items
- Only include seller's items in response (not other sellers' items)

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "orderId": 1001,
      "orderNumber": "ORD-2026-001001",
      "orderDate": "2026-01-15T10:30:00Z",
      "orderStatus": "PROCESSING",
      "buyer": {
        "id": 50,
        "username": "john_doe",
        "email": "john@example.com"
      },
      "shippingAddress": {
        "recipientName": "John Doe",
        "street": "123 Main St",
        "city": "Ho Chi Minh City",
        "postalCode": "70000",
        "country": "Vietnam",
        "phone": "+84123456789"
      },
      "sellerItems": [
        {
          "orderItemId": 5001,
          "listing": {
            "id": 301,
            "book": {
              "id": 123,
              "title": "Clean Code",
              "coverImage": "...",
              "isbn": "978-0132350884"
            },
            "condition": "NEW"
          },
          "quantity": 2,
          "pricePerUnit": 45.99,
          "subtotal": 91.98,
          "itemStatus": "PENDING",
          "trackingNumber": null,
          "shippedAt": null
        }
      ],
      "sellerTotal": 91.98,
      "totalItems": 2
    }
  ],
  "meta": {
    "page": 0,
    "totalPages": 10,
    "totalItems": 195,
    "itemsPerPage": 20
  }
}
```

**Important Notes:**
- `sellerItems` only contains items sold by the current seller
- `sellerTotal` is the sum of only the seller's items (not entire order total)
- Order may have items from other sellers (not shown in response)

---

### 2. Get Order Details
**GET** `/api/seller/orders/{orderId}`

**Authorization:** `@PreAuthorize("hasRole('SELLER')")`

**Business Logic:**
- Verify order contains at least one item from seller's listings
- Return detailed information including all seller's items
- Include shipping address and buyer contact info

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "orderId": 1001,
    "orderNumber": "ORD-2026-001001",
    "orderDate": "2026-01-15T10:30:00Z",
    "orderStatus": "PROCESSING",
    "buyer": {
      "id": 50,
      "username": "john_doe",
      "fullName": "John Doe",
      "email": "john@example.com",
      "phone": "+84123456789"
    },
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
    "paymentMethod": "STRIPE",
    "paymentStatus": "COMPLETED",
    "sellerItems": [
      {
        "orderItemId": 5001,
        "listing": {
          "id": 301,
          "book": {
            "id": 123,
            "title": "Clean Code",
            "author": "Robert C. Martin",
            "coverImage": "...",
            "isbn": "978-0132350884"
          },
          "condition": "NEW",
          "price": 45.99
        },
        "quantity": 2,
        "pricePerUnit": 45.99,
        "subtotal": 91.98,
        "itemStatus": "PENDING",
        "trackingNumber": null,
        "carrier": null,
        "shippedAt": null,
        "deliveredAt": null,
        "notes": null
      }
    ],
    "sellerTotal": 91.98,
    "createdAt": "2026-01-15T10:30:00Z",
    "updatedAt": "2026-01-15T10:30:00Z"
  }
}
```

**Error Cases:**
- `403 Forbidden`: Order doesn't contain seller's items
- `404 Not Found`: Order doesn't exist

---

### 3. Update Item Status
**PATCH** `/api/seller/orders/{orderId}/items/{itemId}/status`

**Authorization:** `@PreAuthorize("hasRole('SELLER')")`

**Request Body:**
```json
{
  "status": "SHIPPED",
  "trackingNumber": "TRK123456789",
  "carrier": "Vietnam Post",
  "notes": "Package shipped via express service"
}
```

**Validation:**
- `status`: Required, must be one of: PROCESSING, SHIPPED, DELIVERED, CANCELLED
- `trackingNumber`: Required if status is SHIPPED or DELIVERED
- `carrier`: Optional
- `notes`: Optional, max 500 chars

**Business Logic:**
1. Verify order item belongs to seller's listing
2. Validate status transition (can't go backwards)
3. Update item status and metadata
4. If all items in order are DELIVERED, update order status to DELIVERED
5. Send email notification to buyer
6. Record status change in order history

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "orderItemId": 5001,
    "orderId": 1001,
    "itemStatus": "SHIPPED",
    "trackingNumber": "TRK123456789",
    "carrier": "Vietnam Post",
    "shippedAt": "2026-01-16T09:00:00Z",
    "updatedAt": "2026-01-16T09:00:00Z"
  },
  "message": "Shipping status updated. Buyer has been notified."
}
```

**Error Cases:**
- `403 Forbidden`: Item doesn't belong to seller
- `400 Bad Request`: Invalid status transition
- `404 Not Found`: Order item not found

---

### 4. Order Statistics
**GET** `/api/seller/orders/stats`

**Authorization:** `@PreAuthorize("hasRole('SELLER')")`

**Query Parameters:**
```
?period=7d|30d|90d|1y
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "totalOrders": 195,
    "pendingOrders": 23,
    "processingOrders": 45,
    "shippedOrders": 67,
    "deliveredOrders": 50,
    "cancelledOrders": 10,
    "totalRevenue": 12450.50,
    "averageOrderValue": 63.85,
    "period": "30d"
  }
}
```

---

## 🗄️ Database Schema

### OrderItem Entity (UPDATE REQUIRED)

```java
@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
    
    @ManyToOne
    @JoinColumn(name = "listing_id")
    private Listing listing;
    
    private Integer quantity;
    private BigDecimal pricePerUnit;
    private BigDecimal subtotal;
    
    // ADD THESE FIELDS
    @Enumerated(EnumType.STRING)
    @Column(name = "item_status")
    private OrderItemStatus itemStatus = OrderItemStatus.PENDING;
    
    @Column(name = "tracking_number")
    private String trackingNumber;
    
    private String carrier;
    
    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;
    
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    @Column(length = 500)
    private String notes;
}
```

### OrderItemStatus Enum (NEW)

```java
public enum OrderItemStatus {
    PENDING,      // Order received, awaiting seller action
    PROCESSING,   // Seller is preparing item
    SHIPPED,      // Item shipped by seller
    DELIVERED,    // Item received by buyer
    CANCELLED     // Item cancelled
}
```

### OrderStatusHistory Entity (NEW - for audit trail)

```java
@Entity
@Table(name = "order_status_history")
public class OrderStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;
    
    @Enumerated(EnumType.STRING)
    private OrderItemStatus oldStatus;
    
    @Enumerated(EnumType.STRING)
    private OrderItemStatus newStatus;
    
    @ManyToOne
    @JoinColumn(name = "changed_by_user_id")
    private User changedBy;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "changed_at")
    private LocalDateTime changedAt;
}
```

### Database Migration

```sql
-- Add new columns to order_items
ALTER TABLE order_items 
ADD COLUMN item_status VARCHAR(20) DEFAULT 'PENDING',
ADD COLUMN tracking_number VARCHAR(100),
ADD COLUMN carrier VARCHAR(100),
ADD COLUMN shipped_at TIMESTAMP,
ADD COLUMN delivered_at TIMESTAMP,
ADD COLUMN notes VARCHAR(500);

-- Create order status history table
CREATE TABLE order_status_history (
    id BIGSERIAL PRIMARY KEY,
    order_item_id BIGINT NOT NULL REFERENCES order_items(id) ON DELETE CASCADE,
    old_status VARCHAR(20),
    new_status VARCHAR(20) NOT NULL,
    changed_by_user_id BIGINT NOT NULL REFERENCES users(id),
    notes TEXT,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_order_items_status ON order_items(item_status);
CREATE INDEX idx_order_items_listing_seller ON order_items(listing_id);
CREATE INDEX idx_order_status_history_item ON order_status_history(order_item_id);
```

---

## 📦 DTOs

### SellerOrderDto
```java
public class SellerOrderDto {
    private Long orderId;
    private String orderNumber;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private BuyerSummaryDto buyer;
    private ShippingAddressDto shippingAddress;
    private List<SellerOrderItemDto> sellerItems;
    private BigDecimal sellerTotal;
    private Integer totalItems;
}
```

### SellerOrderItemDto
```java
public class SellerOrderItemDto {
    private Long orderItemId;
    private ListingSummaryDto listing;
    private Integer quantity;
    private BigDecimal pricePerUnit;
    private BigDecimal subtotal;
    private OrderItemStatus itemStatus;
    private String trackingNumber;
    private String carrier;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private String notes;
}
```

### UpdateItemStatusRequest
```java
public class UpdateItemStatusRequest {
    @NotNull
    private OrderItemStatus status;
    
    @Size(max = 100)
    private String trackingNumber;
    
    @Size(max = 100)
    private String carrier;
    
    @Size(max = 500)
    private String notes;
}
```

---

## 🔧 Service Layer

### SellerOrderService

```java
@Service
@RequiredArgsConstructor
public class SellerOrderService {
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final EmailService emailService;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    
    public Page<SellerOrderDto> getSellerOrders(
        Long sellerId, 
        OrderItemStatus status,
        LocalDate startDate,
        LocalDate endDate,
        Pageable pageable
    ) {
        // Find all order items where listing.seller = sellerId
        Specification<OrderItem> spec = Specification.where(null);
        
        spec = spec.and((root, query, cb) -> {
            Join<OrderItem, Listing> listingJoin = root.join("listing");
            Join<Listing, User> sellerJoin = listingJoin.join("seller");
            return cb.equal(sellerJoin.get("id"), sellerId);
        });
        
        if (status != null) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("itemStatus"), status));
        }
        
        if (startDate != null || endDate != null) {
            spec = spec.and((root, query, cb) -> {
                Join<OrderItem, Order> orderJoin = root.join("order");
                // Add date range filter
                return cb.between(orderJoin.get("createdAt"), 
                    startDate.atStartOfDay(), 
                    endDate.atTime(23, 59, 59));
            });
        }
        
        Page<OrderItem> items = orderItemRepository.findAll(spec, pageable);
        
        // Group by Order and map to DTO
        return items.map(this::groupByOrderAndMapToDto);
    }
    
    public OrderItemDto updateItemStatus(
        Long sellerId,
        Long orderId,
        Long itemId,
        UpdateItemStatusRequest request
    ) {
        // Verify item belongs to seller
        OrderItem item = orderItemRepository.findById(itemId)
            .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));
        
        if (!item.getListing().getSeller().getId().equals(sellerId)) {
            throw new ForbiddenException("You can only update your own items");
        }
        
        // Validate status transition
        validateStatusTransition(item.getItemStatus(), request.getStatus());
        
        // Record old status for history
        OrderItemStatus oldStatus = item.getItemStatus();
        
        // Update item
        item.setItemStatus(request.getStatus());
        item.setTrackingNumber(request.getTrackingNumber());
        item.setCarrier(request.getCarrier());
        item.setNotes(request.getNotes());
        
        if (request.getStatus() == OrderItemStatus.SHIPPED) {
            item.setShippedAt(LocalDateTime.now());
        } else if (request.getStatus() == OrderItemStatus.DELIVERED) {
            item.setDeliveredAt(LocalDateTime.now());
        }
        
        OrderItem updated = orderItemRepository.save(item);
        
        // Save status history
        saveStatusHistory(updated, oldStatus, sellerId);
        
        // Send email notification
        emailService.sendOrderStatusUpdate(updated.getOrder(), updated);
        
        // Check if all items delivered, update order status
        checkAndUpdateOrderStatus(updated.getOrder());
        
        return orderItemMapper.toDto(updated);
    }
    
    private void validateStatusTransition(
        OrderItemStatus current, 
        OrderItemStatus next
    ) {
        // Define valid transitions
        Map<OrderItemStatus, List<OrderItemStatus>> validTransitions = Map.of(
            OrderItemStatus.PENDING, List.of(
                OrderItemStatus.PROCESSING, 
                OrderItemStatus.CANCELLED
            ),
            OrderItemStatus.PROCESSING, List.of(
                OrderItemStatus.SHIPPED, 
                OrderItemStatus.CANCELLED
            ),
            OrderItemStatus.SHIPPED, List.of(
                OrderItemStatus.DELIVERED
            )
        );
        
        if (!validTransitions.getOrDefault(current, List.of()).contains(next)) {
            throw new BadRequestException(
                String.format("Cannot transition from %s to %s", current, next)
            );
        }
    }
}
```

---

## 🧪 Testing Requirements

### Unit Tests

```java
@Test
void getSellerOrders_OnlyReturnsSellerItems() {
    // Given: Orders with items from multiple sellers
    // When: Seller 1 queries their orders
    // Then: Only items from Seller 1 are returned
}

@Test
void updateItemStatus_ValidTransition_Success() {
    // Given: Item in PENDING status
    // When: Update to PROCESSING
    // Then: Status updated, history recorded
}

@Test
void updateItemStatus_InvalidTransition_ThrowsException() {
    // Given: Item in SHIPPED status
    // When: Try to update to PENDING
    // Then: Throw BadRequestException
}

@Test
void updateItemStatus_NotOwner_ThrowsForbidden() {
    // Given: Item belongs to Seller A
    // When: Seller B tries to update
    // Then: Throw ForbiddenException
}

@Test
void updateItemStatus_ToShipped_SendsEmail() {
    // Given: Item being marked as SHIPPED
    // When: updateItemStatus() is called
    // Then: Email sent to buyer
}
```

---

## 🔗 Dependencies

### Related Modules
- **Module 07:** Orders (order entity)
- **Module 04:** Listings (listing entity)
- **Module 02:** User Management (seller verification)

### External Services
- Email service (order status notifications)

---

## ✅ Acceptance Criteria

- [ ] Sellers can view all orders containing their items
- [ ] Order list shows only seller's items (not other sellers')
- [ ] Sellers can filter orders by status
- [ ] Sellers can update shipping status for their items
- [ ] Status transitions are validated
- [ ] Email sent to buyer when status updated
- [ ] Status history is recorded for audit trail
- [ ] Order status auto-updates when all items delivered
- [ ] Statistics endpoint provides seller metrics
- [ ] All endpoints return standardized format
- [ ] Unit test coverage ≥ 80%
- [ ] Integration tests pass

---

## 📅 Timeline

| Task | Estimated Time | Status |
|------|----------------|--------|
| Add status fields to OrderItem | 1 hour | ❌ |
| Create OrderStatusHistory entity | 1 hour | ❌ |
| Implement SellerOrderService | 6 hours | ❌ |
| Create SellerController | 3 hours | ❌ |
| Email notification integration | 2 hours | ❌ |
| Status validation logic | 2 hours | ❌ |
| Write unit tests | 4 hours | ❌ |
| Write integration tests | 3 hours | ❌ |
| Documentation | 1 hour | ❌ |
| **Total** | **23 hours (3 days)** | |

---

## 📝 Notes

- Consider adding bulk status update (update multiple items at once)
- Consider adding automatic status progression after X days
- Consider integrating with shipping carrier APIs for real-time tracking
- Monitor email delivery success rates
- Consider adding SMS notifications (future)

---

## ✔️ Sign-off

**Developer:** _________________ Date: _______  
**Reviewer:** _________________ Date: _______  
**QA:** _________________ Date: _______