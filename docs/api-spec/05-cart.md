# Module 05: Shopping Cart

**Status:** ✅ Complete (100%)  
**Priority:** 🔴 CRITICAL  
**Sprint:** Pre-Sprint (Foundation)  
**Completed Date:** December 2025

---

## 📋 Overview

Shopping cart management allowing users to add items, update quantities, and proceed to checkout. Cart persists across sessions for authenticated users.

**Key Features:**
- Add listings to cart
- Update item quantities
- Remove items from cart
- Get cart summary with totals
- Cart validation (stock check)
- Cart persistence

---

## 🎯 Business Rules

1. **Authorization:** Only authenticated users can manage cart
2. **Unique Items:** Each listing can only appear once (update quantity instead)
3. **Stock Validation:** Cannot add more than available stock
4. **Price Snapshot:** Cart stores price at time of adding
5. **Auto-Cleanup:** Remove items if listing becomes unavailable
6. **Expiration:** Cart items older than 30 days are auto-removed

---

## 📡 API Endpoints

### Endpoint Summary Table

| Method | Endpoint | Description | Auth | Status |
|--------|----------|-------------|------|--------|
| GET | `/api/cart` | Get user's cart | Required | ✅ Complete |
| POST | `/api/cart/items` | Add item to cart | Required | ✅ Complete |
| PUT | `/api/cart/items/{id}` | Update item quantity | Required | ✅ Complete |
| DELETE | `/api/cart/items/{id}` | Remove item from cart | Required | ✅ Complete |
| DELETE | `/api/cart/clear` | Clear entire cart | Required | ✅ Complete |

---

## 🔧 Implementation Details

### 1. Get Cart
**GET** `/api/cart`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "id": 1,
    "items": [
      {
        "id": 501,
        "listing": {
          "id": 301,
          "book": {
            "id": 123,
            "title": "Clean Code",
            "author": "Robert C. Martin",
            "coverImage": "https://cdn.example.com/books/clean-code.jpg",
            "isbn": "978-0132350884"
          },
          "seller": {
            "id": 50,
            "username": "bookstore_pro",
            "businessName": "Pro Book Store"
          },
          "condition": "NEW",
          "currentPrice": 45.99,
          "stockQuantity": 15,
          "status": "ACTIVE"
        },
        "quantity": 2,
        "priceAtAdd": 45.99,
        "subtotal": 91.98,
        "addedAt": "2026-01-01T10:00:00Z"
      }
    ],
    "summary": {
      "totalItems": 2,
      "subtotal": 91.98,
      "shipping": 5.00,
      "tax": 9.70,
      "total": 106.68
    },
    "updatedAt": "2026-01-01T10:30:00Z"
  }
}
```

**Backend Files:**
- `CartController.java` ✅
- `CartService.java` ✅
- `Cart.java` entity ✅

---

### 2. Add Item to Cart
**POST** `/api/cart/items`

**Request Body:**
```json
{
  "listingId": 301,
  "quantity": 2
}
```

**Validation:**
- `listingId`: Required, must exist and be ACTIVE
- `quantity`: Required, must be > 0 and <= available stock

**Response:** `201 Created`
```json
{
  "success": true,
  "data": {
    "id": 501,
    "listing": { ... },
    "quantity": 2,
    "priceAtAdd": 45.99,
    "subtotal": 91.98,
    "addedAt": "2026-01-01T10:00:00Z"
  },
  "message": "Item added to cart"
}
```

**Business Logic:**
- If item already in cart, update quantity instead of creating duplicate
- Store current listing price as `priceAtAdd`
- Validate stock availability
- If listing is SOLD_OUT or INACTIVE, return error

**Error Cases:**
- `400 Bad Request`: Quantity exceeds stock
- `404 Not Found`: Listing not found
- `409 Conflict`: Listing not available for purchase

**Status:** ✅ Implemented in `CartItemController.java`

---

### 3. Update Item Quantity
**PUT** `/api/cart/items/{id}`

**Request Body:**
```json
{
  "quantity": 5
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "id": 501,
    "quantity": 5,
    "subtotal": 229.95
  }
}
```

**Validation:**
- New quantity must be > 0 (use DELETE to remove)
- New quantity must be <= available stock

**Status:** ✅ Implemented

---

### 4. Remove Item from Cart
**DELETE** `/api/cart/items/{id}`

**Response:** `204 No Content`

**Status:** ✅ Implemented

---

### 5. Clear Cart
**DELETE** `/api/cart/clear`

**Response:** `204 No Content`

**Use Case:** After successful order placement

**Status:** ✅ Implemented

---

## 🗄️ Database Schema

### Cart Entity
```java
@Entity
@Table(name = "carts")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;
    
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

### CartItem Entity
```java
@Entity
@Table(name = "cart_items")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;
    
    @ManyToOne
    @JoinColumn(name = "listing_id")
    private Listing listing;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(name = "price_at_add", precision = 10, scale = 2)
    private BigDecimal priceAtAdd;
    
    @Column(name = "added_at")
    private LocalDateTime addedAt;
    
    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }
}
```

---

## 📦 DTOs

### CartDto
```java
public class CartDto {
    private Long id;
    private List<CartItemDto> items;
    private CartSummaryDto summary;
    private LocalDateTime updatedAt;
}
```

### CartItemDto
```java
public class CartItemDto {
    private Long id;
    private ListingDto listing;
    private Integer quantity;
    private BigDecimal priceAtAdd;
    private BigDecimal subtotal;
    private LocalDateTime addedAt;
}
```

### CartSummaryDto
```java
public class CartSummaryDto {
    private Integer totalItems;
    private BigDecimal subtotal;
    private BigDecimal shipping;
    private BigDecimal tax;
    private BigDecimal total;
}
```

### AddToCartRequest
```java
public class AddToCartRequest {
    @NotNull
    private Long listingId;
    
    @NotNull
    @Min(1)
    private Integer quantity;
}
```

---

## 🧪 Testing Status

### Unit Tests
- ✅ CartService tests complete
- ✅ Add to cart logic tested
- ✅ Update quantity tested
- ✅ Stock validation tested
- ✅ Cart summary calculation tested

### Integration Tests
- ✅ All controller endpoints tested
- ✅ Authorization tested
- ✅ Validation tested

**Test Coverage:** 85%

---

## 🔗 Dependencies

### Related Modules
- **Module 04:** Listings (listing entity)
- **Module 06:** Checkout (cart used during checkout)
- **Module 01:** Authentication (user cart association)

---

## ✅ Verification Checklist

- ✅ Users can add items to cart
- ✅ Duplicate listings update quantity instead of adding twice
- ✅ Stock validation prevents over-ordering
- ✅ Users can update item quantities
- ✅ Users can remove items from cart
- ✅ Cart summary calculates totals correctly
- ✅ Cart persists across sessions
- ✅ Price at time of adding is stored
- ✅ Cart items validate against listing availability
- ✅ All endpoints return standardized response format
- ✅ Unit test coverage ≥ 80%
- ✅ Integration tests pass

---

## 📝 Notes

**Current Implementation:**
- Cart management is fully functional
- Stock validation works correctly
- Cart persists for authenticated users
- Price snapshots protect against price changes

**Future Enhancements:**
- Add cart expiration job (remove items > 30 days old)
- Add "save for later" feature
- Add cart sharing functionality
- Add recently removed items recovery
- Add price change notifications

**Known Limitations:**
- Cart doesn't handle seller-level organization (items from same seller)
- No quantity limits per user
- No cart merge on login (if guest cart exists)

---

## ✔️ Sign-off

**Developer:** Backend Team - Date: Dec 2025  
**Reviewer:** Tech Lead - Date: Dec 2025  
**QA:** QA Team - Date: Dec 2025  
**Status:** ✅ Production Ready