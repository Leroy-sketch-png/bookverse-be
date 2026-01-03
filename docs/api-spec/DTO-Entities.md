# BookVerse Backend DTOs & Entities Analysis

**Generated Date:** January 3, 2026  
**Analysis Scope:** Complete comparison between Backend (Java Entities/DTOs) and Frontend (TypeScript Types)

---

## Table of Contents
1. [Executive Summary](#executive-summary)
2. [Authentication & Authorization](#1-authentication--authorization)
3. [User Management & Profile](#2-user-management--profile)
4. [Books & Listings](#3-books--listings)
5. [Orders & Checkout](#4-orders--checkout)
6. [Cart & Cart Items](#5-cart--cart-items)
7. [Reviews & Ratings](#6-reviews--ratings)
8. [Seller Features](#7-seller-features)
9. [Wishlist](#8-wishlist)
10. [Promotions & Discounts](#9-promotions--discounts)
11. [Analytics & Statistics](#10-analytics--statistics)
12. [Missing Entities](#11-missing-entities)
13. [Refinements Needed](#12-refinements-needed)
14. [Implementation Priority Matrix](#13-implementation-priority-matrix)

---

## Executive Summary

### Overall Alignment Status
- **✅ Well Aligned:** 60% (Core entities like User, Listing, Order, Cart)
- **⚠️ Needs Refinement:** 25% (Response formats, missing fields, entity relationships)
- **❌ Missing Implementation:** 15% (Reviews, Promotions, Analytics entities)

### Critical Findings

#### ✅ Strengths
1. **Solid Core Architecture:** User, Authentication, Cart, Order, Payment entities are well-structured
2. **Good Database Design:** Proper relationships, constraints, and indexes
3. **Enum Usage:** Consistent use of enums for status fields
4. **Audit Fields:** CreatedAt/UpdatedAt timestamps properly implemented

#### ❌ Major Gaps
1. **No Review Entity Implementation** - Entity exists but no DTOs/Responses
2. **Missing Promotion System** - Frontend expects promotions but backend has none
3. **No Analytics DTOs** - Seller dashboard analytics not supported
4. **Incomplete Response Structures** - Many responses missing nested data

#### ⚠️ Alignment Issues
1. **Response Format Inconsistency** - FE expects nested objects, BE returns IDs
2. **Pagination Format Mismatch** - Different structures
3. **Field Name Differences** - camelCase vs snake_case mismatches
4. **Missing Aggregated Data** - Counts, averages, statistics not calculated

---

## 1. Authentication & Authorization

### ✅ Well Aligned

#### Backend Entities
```java
// User.java
- id: Long
- username: String
- email: String
- passwordHash: String
- enabled: Boolean
- lastLogin: LocalDateTime
- role: Role (ManyToOne)
- googleId: String
- authProvider: String
```

#### Backend DTOs
```java
// AuthenticationResponse.java
- token: String
- authenticated: boolean
- lastLogin: LocalDateTime
- user: UserResponse
```

#### Frontend Types
```typescript
// auth.type.ts - SignInResponse
{
  code: number
  message: string
  result: {
    token: string
    authenticated: boolean
    lastLogin: Date
    user: {
      id: number
      username: string
      email: string
      enabled: boolean
      roles: { id: number, name: RoleEnum }[]
    }
  }
}
```

### ⚠️ Issues Found

1. **Role Structure Mismatch**
    - **Backend:** `UserResponse` has `RoleResponse role` (singular)
    - **Frontend:** Expects `roles: []` (array)
    - **Impact:** 🔴 Critical - Auth token parsing will fail
    - **Fix Required:**
   ```java
   // UserResponse.java - Change from:
   RoleResponse role;
   // To:
   Set<RoleResponse> roles;
   ```

2. **Missing Password Reset DTOs**
    - **Frontend:** Has `ChangePasswordDto` with email, otp, password
    - **Backend:** Missing corresponding request DTO
    - **Impact:** 🟡 Medium - Password reset feature incomplete
    - **Fix Required:** Create `PasswordResetRequest.java`

3. **Seller Registration Response Incomplete**
    - **Frontend:** Expects `{ id, shopName, status }`
    - **Backend:** Missing `SellerRegistrationResponse` DTO
    - **Impact:** 🟡 Medium - Seller onboarding may fail
    - **Fix Required:** Create response DTO matching FE expectations

---

## 2. User Management & Profile

### ✅ Well Aligned

#### Backend Entities
```java
// UserProfile.java
- id: Long
- user: User (OneToOne)
- displayName: String
- fullName: String
- phoneNumber: String
- avatarUrl: String
- bio: String
- accountType: String
- location: String
- ratingAvg: Double
- ratingCount: Integer
- sellerSince: LocalDate
- preferences: String (JSON)
```

#### Backend DTOs
```java
// ProfileCreationRequest.java
- fullName: String
- avatarUrl: String
- displayName: String
- phoneNumber: String
- location: String
- accountType: String
- preferences: String
- bio: String
```

#### Frontend Types
```typescript
// user.type.ts - SetupProfileDto
{
  fullName: string
  location: string
  accountType: AccountTypeEnum
  preferences: string
  phoneNumber: string
  bio?: string
}
```

### ⚠️ Issues Found

1. **Missing ProfileResponse DTO**
    - **Backend:** Has entity but likely returns raw entity or generic response
    - **Frontend:** Expects structured `GetMeResponse`
    - **Impact:** 🟡 Medium - Profile display may show wrong fields
    - **Fix Required:**
   ```java
   @Data
   @AllArgsConstructor
   @NoArgsConstructor
   public class ProfileResponse {
       private Long id;
       private Long userId;
       private String fullName;
       private String displayName;
       private String phoneNumber;
       private String avatarUrl;
       private String bio;
       private String accountType;
       private String location;
       private String preferences;
       private Double ratingAvg;
       private Integer ratingCount;
       private LocalDate sellerSince;
   }
   ```

2. **User Response Missing Profile Data**
    - **Backend:** `UserResponse` only has basic user fields
    - **Frontend:** Expects `User` type with profile fields merged
    - **Impact:** 🟡 Medium - Multiple API calls needed
    - **Fix Required:** Add `ProfileResponse profile` to `UserResponse`

3. **AccountType Enum Missing**
    - **Frontend:** Uses `AccountTypeEnum` (BUYER, CASUAL_SELLER, PRO_SELLER)
    - **Backend:** Stores as String
    - **Impact:** 🟢 Low - No validation, but works
    - **Fix Recommended:** Create `AccountType` enum for type safety

---

## 3. Books & Listings

### ✅ Well Aligned

#### Backend Entities
```java
// BookMeta.java (Core Book Data)
- id: Long
- title: String
- isbn: String
- description: String
- publisher: String
- publishedDate: LocalDate
- pages: Integer
- listPrice: BigDecimal
- images: List<BookImage> (OneToMany)
- authors: Set<Author> (ManyToMany)
- categories: Set<Category> (ManyToMany)

// Listing.java (Seller's Book Instance)
- id: Long
- bookMeta: BookMeta (ManyToOne)
- seller: User (ManyToOne)
- titleOverride: String
- price: BigDecimal
- currency: String
- condition: BookCondition (ENUM)
- quantity: Integer
- location: String
- status: ListingStatus (ENUM)
- visibility: Boolean
- views: Integer
- likes: Integer
- soldCount: Integer
- photos: List<ListingPhoto> (OneToMany)
```

#### Frontend Types
```typescript
// book.type.ts - Book (Aggregated View)
{
  id: number
  title: string
  isbn: string
  publisher: string
  publicationDate: string
  pageCount: number
  language: string
  description: string
  coverImageUrl: string
  authors: Author[]
  categories: Category[]
  price: number
  finalPrice: number
  discount?: { type: 'PERCENT' | 'FIXED', value: number }
  currency: string
  seller: SellerProfile
}

// BookListing (Seller's View)
{
  id: string
  sellerId: number
  title: string
  author: string
  isbn?: string
  publisher: string
  publishYear: number
  category: string
  condition: string
  price: number
  stock: number
  sold: number
  views?: number
  images: string[]
  status: 'active' | 'out_of_stock' | 'draft'
}
```

### ❌ Missing Implementation

1. **No Unified Book Response DTO**
    - **Backend:** Has `ListingResponse` but missing aggregated book view
    - **Frontend:** Expects combined Book + Listing + Seller data
    - **Impact:** 🔴 Critical - Book catalog pages won't work properly
    - **Fix Required:**
   ```java
   @Data
   @AllArgsConstructor
   @NoArgsConstructor
   public class BookResponse {
       private Long id;
       private String title;
       private String isbn;
       private String publisher;
       private LocalDate publicationDate;
       private Integer pageCount;
       private String language;
       private String description;
       private String coverImageUrl;
       private List<AuthorResponse> authors;
       private List<CategoryResponse> categories;
       private BigDecimal price;
       private BigDecimal finalPrice;
       private DiscountResponse discount;
       private String currency;
       private SellerProfileResponse seller;
   }
   ```

2. **Missing Discount/Promotion Join**
    - **Backend:** Listing has no relationship to promotions
    - **Frontend:** Expects `discount` field on books
    - **Impact:** 🔴 Critical - Discounted prices won't show
    - **Fix Required:** Add promotion relationship to Listing entity

3. **ListingResponse Missing Computed Fields**
    - **Backend:** `ListingResponse` returns raw fields
    - **Frontend:** Expects computed `sold`, aggregated `author` string, `category` string
    - **Impact:** 🟡 Medium - FE needs to compute these
    - **Fix Required:** Add computed fields to DTO

### ⚠️ Refinements Needed

1. **ID Type Mismatch**
    - **Backend:** Uses `Long` for IDs
    - **Frontend:** Some types expect `string` (e.g., `BookListing.id: string`)
    - **Impact:** 🟢 Low - JS handles conversion
    - **Note:** Ensure consistent serialization

2. **Status Enum Values**
    - **Backend:** `ListingStatus { DRAFT, ACTIVE, PAUSED, SOLD, REMOVED }`
    - **Frontend:** Expects `'active' | 'out_of_stock' | 'draft'`
    - **Impact:** 🟡 Medium - Status filtering will fail
    - **Fix Required:** Align enum values or add mapping

3. **Missing Language Field**
    - **Backend:** `BookMeta` has no language field
    - **Frontend:** Expects `language: string`
    - **Impact:** 🟢 Low - Can default to 'en'
    - **Fix Required:** Add `language` column to BookMeta

---

## 4. Orders & Checkout

### ✅ Well Aligned

#### Backend Entities
```java
// Order.java
- id: UUID
- user: User (ManyToOne)
- orderNumber: String (unique)
- status: OrderStatus (ENUM)
- subtotal: BigDecimal
- totalAmount: BigDecimal
- tax: BigDecimal
- shipping: BigDecimal
- discount: BigDecimal
- total: BigDecimal
- promoCode: String
- shippingAddress: ShippingAddress (ManyToOne)
- billingAddress: ShippingAddress (ManyToOne)
- trackingNumber: String
- trackingUrl: String
- carrier: String
- shippedAt: LocalDateTime
- deliveredAt: LocalDateTime
- cancelledAt: LocalDateTime
- items: List<OrderItem> (OneToMany)

// OrderItem.java
- id: UUID
- order: Order (ManyToOne)
- listing: Listing (ManyToOne)
- bookMeta: BookMeta (ManyToOne)
- seller: User (ManyToOne)
- title: String
- author: String
- coverImage: String
- quantity: Integer
- price: BigDecimal
- subtotal: BigDecimal
```

#### Frontend Types
```typescript
// order.type.ts - SellerOrder
{
  id: string
  orderNumber: string
  status: 'pending' | 'processing' | 'shipped' | 'delivered' | 'cancelled'
  buyerName: string
  buyerEmail: string
  shippingAddress: {...}
  books: Array<{
    listingId: string
    title: string
    image: string
    quantity: number
    price: number
  }>
  totalPrice: number
  trackingNumber?: string
  shippedAt?: Date
}

// Order (Buyer View)
{
  id: string
  orderNumber: string
  createdAt: Date
  status: OrderStatus
  items: OrderBookItem[]
  totalAmount: number
  finalAmount: number
  discount?: Discount
  shippingAddress: ShippingAddress
}
```

### ❌ Missing Implementation

1. **No OrderResponse DTOs**
    - **Backend:** No `OrderResponse`, `SellerOrderResponse` DTOs found
    - **Frontend:** Expects structured responses for buyer and seller views
    - **Impact:** 🔴 Critical - Order pages will fail
    - **Fix Required:**
   ```java
   // Buyer view
   @Data
   public class OrderResponse {
       private UUID id;
       private String orderNumber;
       private LocalDateTime createdAt;
       private OrderStatus status;
       private List<OrderItemResponse> items;
       private BigDecimal totalAmount;
       private BigDecimal finalAmount;
       private DiscountResponse discount;
       private ShippingAddressResponse shippingAddress;
   }
   
   // Seller view
   @Data
   public class SellerOrderResponse {
       private UUID id;
       private String orderNumber;
       private OrderStatus status;
       private String buyerName;
       private String buyerEmail;
       private ShippingAddressResponse shippingAddress;
       private List<SellerOrderItemResponse> books;
       private BigDecimal totalPrice;
       private String trackingNumber;
       private LocalDateTime shippedAt;
       private LocalDateTime createdAt;
   }
   ```

2. **Missing Update Order Status DTO**
    - **Frontend:** Has `UpdateOrderStatusDto { status, trackingNumber }`
    - **Backend:** Missing request DTO
    - **Impact:** 🔴 Critical - Sellers can't update order status
    - **Fix Required:** Create `UpdateOrderStatusRequest.java`

### ⚠️ Refinements Needed

1. **OrderStatus Enum Mismatch**
    - **Backend:** `{ PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED }`
    - **Frontend:** Expects `'pending' | 'processing' | 'shipped' | 'delivered' | 'cancelled'`
    - **Impact:** 🟡 Medium - Status filters won't work
    - **Fix Required:** Remove `CONFIRMED` and `REFUNDED`, or add mapping

2. **Shipping Address Structure**
    - **Backend:** Has `ShippingAddress` entity with `addressLine1`, `addressLine2`, `city`, `postalCode`, `country`
    - **Frontend:** Expects `{ fullName, phone, street, ward?, district, city, note? }`
    - **Impact:** 🟡 Medium - Different address formats
    - **Fix Required:** Add `ward`, `district`, `note` fields; map `street` to `addressLine1`

---

## 5. Cart & Cart Items

### ✅ Well Aligned

#### Backend Entities
```java
// Cart.java
- id: Long
- user: User (OneToOne)
- totalPrice: BigDecimal
- cartItems: Set<CartItem> (OneToMany)
- voucher: Voucher (ManyToOne)

// CartItem.java
- id: Long
- cart: Cart (ManyToOne)
- listing: Listing (ManyToOne)
- quantity: Integer
- addedAt: LocalDateTime
+ getSubtotalPrice(): BigDecimal (computed)
```

#### Backend DTOs
```java
// CartItemRequest.java (record)
record CartItemRequest(
    Long listingId,
    Integer quantity
)
```

#### Frontend Types
```typescript
// Cart structure is handled implicitly in checkout
// Cart items managed via API calls
```

### ⚠️ Issues Found

1. **Missing Cart Response DTO**
    - **Backend:** Likely returns raw entity or generic structure
    - **Frontend:** Expects cart with computed totals, item details
    - **Impact:** 🟡 Medium - FE needs to compute totals
    - **Fix Required:**
   ```java
   @Data
   public class CartResponse {
       private Long id;
       private List<CartItemResponse> items;
       private BigDecimal subtotal;
       private BigDecimal discount;
       private BigDecimal total;
       private VoucherResponse appliedVoucher;
       private Integer itemCount;
   }
   
   @Data
   public class CartItemResponse {
       private Long id;
       private ListingResponse listing;
       private Integer quantity;
       private BigDecimal subtotal;
       private LocalDateTime addedAt;
   }
   ```

2. **Voucher Application**
    - **Backend:** Cart has `voucher` field
    - **Frontend:** Cart flow applies voucher at checkout
    - **Impact:** 🟢 Low - Flow works but could be clearer
    - **Recommendation:** Add `POST /api/cart/apply-voucher` endpoint

---

## 6. Reviews & Ratings

### ❌ Critical Gap - Missing Implementation

#### Backend Entity Exists ✅
```java
// Review.java
- id: Long
- bookMeta: BookMeta (ManyToOne)
- user: User (ManyToOne)
- rating: Integer
- comment: String
- isVisible: Boolean
- createdAt: LocalDateTime
```

#### Backend DTOs Missing ❌
- No `ReviewRequest.java`
- No `ReviewResponse.java`
- No `ReviewUpdateRequest.java`
- No Review controller found

#### Frontend Expects ⚠️
```typescript
// review.type.ts
{
  id: string
  bookId: string
  userId: number
  userName: string
  userAvatar?: string
  rating: number // 1-5
  comment: string
  createdAt: string
  updatedAt: string
  helpful: number
  verified: boolean
}

// CreateReviewDto
{
  bookId: string
  rating: number
  comment: string
}

// ReviewsListResponse
{
  reviews: Review[]
  stats: {
    averageRating: number
    totalReviews: number
    ratingDistribution: { 5: n, 4: n, 3: n, 2: n, 1: n }
  }
}
```

### 🔴 Critical - Required Implementation

**Priority: HIGHEST**

1. **Create Review DTOs:**
   ```java
   @Data
   public class CreateReviewRequest {
       private Long bookId; // or listingId?
       private Integer rating; // 1-5, add @Min(1) @Max(5)
       private String comment;
   }
   
   @Data
   public class UpdateReviewRequest {
       private Integer rating;
       private String comment;
   }
   
   @Data
   public class ReviewResponse {
       private Long id;
       private Long bookId;
       private Long userId;
       private String userName;
       private String userAvatar;
       private Integer rating;
       private String comment;
       private LocalDateTime createdAt;
       private LocalDateTime updatedAt;
       private Integer helpfulCount;
       private Boolean verified; // verified purchase
   }
   
   @Data
   public class ReviewsListResponse {
       private List<ReviewResponse> reviews;
       private ReviewStatsResponse stats;
   }
   
   @Data
   public class ReviewStatsResponse {
       private Double averageRating;
       private Integer totalReviews;
       private Map<Integer, Integer> ratingDistribution; // 1->count, 2->count, etc.
   }
   ```

2. **Add Missing Fields to Review Entity:**
   ```java
   // Add to Review.java:
   @Column(name = "helpful_count")
   private Integer helpfulCount = 0;
   
   @Column(name = "verified_purchase")
   private Boolean verifiedPurchase = false;
   
   @Column(name = "updated_at")
   private LocalDateTime updatedAt;
   ```

3. **Create ReviewController:**
   ```java
   @RestController
   @RequestMapping("/api/reviews")
   public class ReviewController {
       // POST /api/reviews - Create review
       // PUT /api/reviews/{id} - Update review
       // DELETE /api/reviews/{id} - Delete review
       // GET /api/books/{bookId}/reviews - List reviews for book
       // POST /api/reviews/{id}/helpful - Mark review as helpful
   }
   ```

4. **Business Logic Needed:**
    - Verify user purchased the book before allowing review
    - Prevent duplicate reviews (one per user per book)
    - Calculate average rating and update BookMeta or Listing
    - Support pagination for reviews list

---

## 7. Seller Features

### ⚠️ Partial Implementation

#### Backend Entities Related to Sellers
```java
// UserProfile.java
- ratingAvg: Double
- ratingCount: Integer
- sellerSince: LocalDate

// Listing.java (seller's products)
- seller: User
- views: Integer
- likes: Integer
- soldCount: Integer
```

#### Frontend Expects
```typescript
// seller.type.ts - SellerProfile (Public View)
{
  id: string
  username: string
  displayName: string
  avatarUrl?: string
  coverImageUrl?: string
  bio?: string
  location?: string
  memberSince: string
  isVerified: boolean
  isProSeller: boolean
  badge?: string
  stats: {
    totalSales: number
    averageRating: number
    totalReviews: number
    fulfillmentRate: number
    responseTime: string
    repeatBuyerRate?: number
  }
  tags?: string[]
}

// ProSellerApplicationDto
{
  businessName: string
  businessAddress: string
  businessPhone: string
  taxId: string
  businessLicenseNumber?: string
  businessDescription: string
  yearsInBusiness?: number
  monthlyInventory?: number
  documents?: File[]
}

// SellerStats (Dashboard Analytics)
{
  revenue: RevenueData
  sales: SalesData
  orders: OrdersBreakdown
  listings: ListingsStats
  views: ViewsData
  rating: RatingData
}
```

### ❌ Missing Implementation

1. **Pro Seller System**
    - **Backend:** Missing `ProSellerApplication` entity
    - **Backend:** Missing `UpgradeToSellerRequest` DTO (has basic version but incomplete)
    - **Impact:** 🔴 Critical - Pro seller upgrade flow doesn't work
    - **Fix Required:**
   ```java
   @Entity
   @Table(name = "pro_seller_application")
   public class ProSellerApplication {
       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long id;
       
       @ManyToOne
       @JoinColumn(name = "user_id")
       private User user;
       
       private String businessName;
       private String businessAddress;
       private String businessPhone;
       private String taxId;
       private String businessLicenseNumber;
       private String businessDescription;
       private Integer yearsInBusiness;
       private Integer monthlyInventory;
       
       @ElementCollection
       private List<String> documentUrls;
       
       @Enumerated(EnumType.STRING)
       private ApplicationStatus status; // PENDING, APPROVED, REJECTED
       
       private String reviewNotes;
       private LocalDateTime submittedAt;
       private LocalDateTime reviewedAt;
       private Long reviewedBy;
   }
   ```

2. **Seller Analytics DTOs**
    - **Backend:** No analytics DTOs exist
    - **Frontend:** Expects detailed analytics for seller dashboard
    - **Impact:** 🔴 Critical - Seller dashboard won't load
    - **Fix Required:**
   ```java
   @Data
   public class SellerStatsResponse {
       private RevenueData revenue;
       private SalesData sales;
       private OrdersBreakdown orders;
       private ListingsStats listings;
       private ViewsData views;
       private RatingData rating;
       private BigDecimal avgOrderValue;
       private Double conversionRate;
   }
   
   @Data
   public class RevenueData {
       private BigDecimal total;
       private Double trend; // percentage
       private List<DailyData> byDay;
   }
   
   @Data
   public class OrdersBreakdown {
       private Integer pending;
       private Integer processing;
       private Integer shipped;
       private Integer delivered;
       private Integer cancelled;
   }
   
   @Data
   public class ListingsStats {
       private Integer active;
       private Integer outOfStock;
       private Integer pending;
       private Integer total;
   }
   ```

3. **Seller Profile Response**
    - **Backend:** No public seller profile DTO
    - **Frontend:** Expects rich profile with stats
    - **Impact:** 🔴 Critical - Public seller pages won't work
    - **Fix Required:** Create `SellerProfileResponse` with aggregated stats

4. **Missing isProSeller Flag**
    - **Backend:** No clear field to indicate pro seller status
    - **Frontend:** Checks `isProSeller` boolean
    - **Impact:** 🟡 Medium - Can't differentiate seller tiers
    - **Fix Required:** Add to UserProfile or use Role enum

---

## 8. Wishlist

### ✅ Entity Exists

#### Backend Entity
```java
// Wishlist.java
- id: Long
- user: User (ManyToOne)
- bookMeta: BookMeta (ManyToOne)
- addedAt: LocalDateTime
+ uniqueConstraint on (user_id, book_id)
```

### ❌ Missing Implementation

1. **No Wishlist Controller**
    - **Backend:** Entity exists but no REST API
    - **Frontend:** Likely expects wishlist endpoints
    - **Impact:** 🔴 Critical - Wishlist feature completely non-functional
    - **Fix Required:**
   ```java
   @RestController
   @RequestMapping("/api/wishlist")
   public class WishlistController {
       // GET /api/wishlist - Get user's wishlist
       // POST /api/wishlist - Add to wishlist (body: { bookId })
       // DELETE /api/wishlist/{bookId} - Remove from wishlist
       // GET /api/wishlist/check/{bookId} - Check if book is in wishlist
   }
   ```

2. **No Wishlist DTOs**
    - **Fix Required:**
   ```java
   @Data
   public class WishlistItemResponse {
       private Long id;
       private BookResponse book; // Full book details
       private LocalDateTime addedAt;
   }
   
   @Data
   public class WishlistResponse {
       private List<WishlistItemResponse> items;
       private Integer totalCount;
   }
   
   public record AddToWishlistRequest(Long bookId) {}
   ```

---

## 9. Promotions & Discounts

### ❌ Missing - No Backend Implementation

#### Frontend Expects
```typescript
// promotion.type.ts
{
  id: string
  name: string
  discountPercentage: number
  startDate: string
  endDate: string
  status: 'active' | 'scheduled' | 'expired' | 'paused'
  appliedBooks: string[] // Listing IDs
  totalRevenue?: number
  itemsSold?: number
}

// CreatePromotionDto
{
  name: string
  discountPercentage: number
  startDate: string
  endDate: string
  appliedBooks: string[]
}
```

#### Backend Reality
- **Voucher entity exists** (discount codes for checkout)
- **No Promotion entity** (automatic discounts on listings)
- **Listing has no discount relationship**

### 🔴 Critical - Required Implementation

**Note:** This is different from Vouchers (promo codes). Promotions are seller-managed discounts on specific listings.

1. **Create Promotion Entity:**
   ```java
   @Entity
   @Table(name = "promotion")
   public class Promotion {
       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long id;
       
       @ManyToOne
       @JoinColumn(name = "seller_id")
       private User seller;
       
       @Column(nullable = false)
       private String name;
       
       @Column(name = "discount_percentage")
       private Integer discountPercentage; // 0-100
       
       @Column(name = "start_date")
       private LocalDateTime startDate;
       
       @Column(name = "end_date")
       private LocalDateTime endDate;
       
       @Enumerated(EnumType.STRING)
       private PromotionStatus status; // ACTIVE, SCHEDULED, EXPIRED, PAUSED
       
       @ManyToMany
       @JoinTable(
           name = "promotion_listing",
           joinColumns = @JoinColumn(name = "promotion_id"),
           inverseJoinColumns = @JoinColumn(name = "listing_id")
       )
       private Set<Listing> appliedListings;
       
       private BigDecimal totalRevenue;
       private Integer itemsSold;
       
       @CreationTimestamp
       private LocalDateTime createdAt;
   }
   
   public enum PromotionStatus {
       ACTIVE, SCHEDULED, EXPIRED, PAUSED
   }
   ```

2. **Create Promotion DTOs:**
   ```java
   @Data
   public class CreatePromotionRequest {
       private String name;
       private Integer discountPercentage;
       private LocalDateTime startDate;
       private LocalDateTime endDate;
       private List<Long> appliedListingIds;
   }
   
   @Data
   public class PromotionResponse {
       private Long id;
       private String name;
       private Integer discountPercentage;
       private LocalDateTime startDate;
       private LocalDateTime endDate;
       private PromotionStatus status;
       private List<Long> appliedBooks;
       private BigDecimal totalRevenue;
       private Integer itemsSold;
       private LocalDateTime createdAt;
   }
   ```

3. **Update Listing/BookResponse:**
    - Add `activePromotion` field to show current discount
    - Calculate `finalPrice` = `price - (price * discount / 100)`

4. **Create PromotionController:**
   ```java
   @RestController
   @RequestMapping("/api/seller/promotions")
   public class PromotionController {
       // POST /api/seller/promotions - Create
       // GET /api/seller/promotions - List seller's promotions
       // PUT /api/seller/promotions/{id} - Update
       // DELETE /api/seller/promotions/{id} - Delete
       // PATCH /api/seller/promotions/{id}/pause - Pause
       // PATCH /api/seller/promotions/{id}/resume - Resume
   }
   ```

5. **Scheduled Job:**
    - Auto-expire promotions when `endDate` passes
    - Auto-activate promotions when `startDate` reached

---

## 10. Analytics & Statistics

### ❌ Missing - No Backend Implementation

#### Frontend Expects
```typescript
// analytic.type.ts
export type RevenueData = {
  total: number
  trend: number
  byDay: Array<{ date: string, amount: number }>
}

export type SalesData = {
  total: number
  trend: number
  byDay: Array<{ date: string, count: number }>
}

export type ProductPerformance = {
  productId: string
  title: string
  imageUrl?: string
  price: number
  sales: number
  revenue: number
  views: number
  conversionRate: number
}

export type TrafficSource = {
  source: 'direct' | 'organic' | 'social' | 'referral' | 'email'
  percentage: number
  visits: number
}

export type CustomerInsights = {
  totalCustomers: number
  repeatCustomers: number
  repeatRate: number
  avgLifetimeValue: number
  topRegions: Array<{ region: string, orders: number, percentage: number }>
}
```

### 🔴 Critical - Required Implementation

**Priority: HIGH** (Seller dashboard won't work without this)

1. **Create Analytics Service:**
   ```java
   @Service
   public class SellerAnalyticsService {
       // Calculate revenue data for date range
       public RevenueData getRevenueData(Long sellerId, LocalDate startDate, LocalDate endDate);
       
       // Calculate sales data
       public SalesData getSalesData(Long sellerId, LocalDate startDate, LocalDate endDate);
       
       // Get order breakdown by status
       public OrdersBreakdown getOrdersBreakdown(Long sellerId);
       
       // Get listing statistics
       public ListingsStats getListingsStats(Long sellerId);
       
       // Get product performance
       public List<ProductPerformance> getTopProducts(Long sellerId, int limit);
       
       // Get customer insights
       public CustomerInsights getCustomerInsights(Long sellerId);
   }
   ```

2. **Create Analytics DTOs:** (See Section 7 for examples)

3. **Create Analytics Controller:**
   ```java
   @RestController
   @RequestMapping("/api/seller/analytics")
   public class SellerAnalyticsController {
       // GET /api/seller/analytics/overview?period=7d|30d|90d|1y
       // GET /api/seller/analytics/revenue?startDate=X&endDate=Y
       // GET /api/seller/analytics/top-products?limit=10
       // GET /api/seller/analytics/customers
   }
   ```

4. **Database Queries Needed:**
    - Aggregate order totals by seller and date
    - Calculate conversion rate (orders / listing views)
    - Identify repeat customers
    - Calculate average order value
    - Geographic distribution of orders

---

## 11. Missing Entities

### 🔴 Critical Missing Entities

1. **ProSellerApplication**
   ```java
   @Entity
   @Table(name = "pro_seller_application")
   public class ProSellerApplication {
       @Id @GeneratedValue
       private Long id;
       @ManyToOne private User user;
       private String businessName;
       private String businessAddress;
       // ... (see Section 7)
   }
   ```

2. **Promotion**
   ```java
   @Entity
   @Table(name = "promotion")
   public class Promotion {
       @Id @GeneratedValue
       private Long id;
       @ManyToOne private User seller;
       private String name;
       private Integer discountPercentage;
       // ... (see Section 9)
   }
   ```

### 🟡 Optional Entities (Good to Have)

3. **Collection** (Featured/Curated book lists)
   ```java
   @Entity
   public class Collection {
       @Id @GeneratedValue
       private Long id;
       private String title;
       private String description;
       
       @ManyToMany
       private Set<BookMeta> books;
       
       private Boolean featured;
       private Integer displayOrder;
   }
   ```

4. **Notification** (User notifications)
   ```java
   @Entity
   public class Notification {
       @Id @GeneratedValue
       private Long id;
       @ManyToOne private User user;
       private String type; // ORDER_UPDATE, REVIEW_RECEIVED, etc.
       private String message;
       private Boolean isRead;
       private LocalDateTime createdAt;
   }
   ```

5. **ReviewHelpful** (Track who found reviews helpful)
   ```java
   @Entity
   @Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "review_id"}))
   public class ReviewHelpful {
       @Id @GeneratedValue
       private Long id;
       @ManyToOne private User user;
       @ManyToOne private Review review;
   }
   ```

---

## 12. Refinements Needed

### 🔧 Entity Refinements

1. **BookMeta Entity**
    - ✅ Add: `language` field (String, default 'en')
    - ✅ Add: `averageRating` (Double, computed from reviews)
    - ✅ Add: `totalReviews` (Integer, count of reviews)
    - ✅ Add index on `isbn` for faster lookups

2. **Listing Entity**
    - ✅ Add: `activePromotion` relationship (ManyToOne to Promotion)
    - ✅ Add: `computedFinalPrice()` method to apply discount
    - ✅ Change: `status` enum to match FE ('active', 'out_of_stock', 'draft')
    - ✅ Add: `lastViewedAt` timestamp for analytics

3. **UserProfile Entity**
    - ✅ Add: `isProSeller` boolean flag
    - ✅ Add: `coverImageUrl` (for seller profile banner)
    - ✅ Add: `responseTime` (average response time metric)
    - ✅ Add: `fulfillmentRate` (percentage of successful deliveries)

4. **Order Entity**
    - ✅ Add: `buyerNotes` text field
    - ✅ Add: `sellerNotes` text field (per order item)
    - ✅ Simplify: `OrderStatus` enum to match FE expectations

5. **Review Entity**
    - ✅ Add: `helpfulCount` integer
    - ✅ Add: `verifiedPurchase` boolean
    - ✅ Add: `updatedAt` timestamp
    - ✅ Add: Unique constraint on (user_id, book_id)

6. **ShippingAddress Entity**
    - ✅ Add: `ward` field (for international addresses)
    - ✅ Add: `district` field
    - ✅ Add: `note` field (delivery instructions)
    - ✅ Rename: `addressLine1` → can map to `street` in response

### 🔧 DTO Refinements

1. **Pagination Format**
    - **Current:** Likely Spring's `Page<T>`
    - **Frontend Expects:**
   ```typescript
   {
     data: T[]
     pagination: {
       total: number
       page: number
       limit: number
       has_next: boolean
     }
   }
   ```
    - **Fix:** Create `PagedResponse<T>` wrapper

2. **Response Structure**
    - **Current:** Direct entity serialization
    - **Frontend Expects:** ApiResponse wrapper
   ```java
   @Data
   @AllArgsConstructor
   public class ApiResponse<T> {
       private int code;
       private String message;
       private T result;
       
       public static <T> ApiResponse<T> success(T data) {
           return new ApiResponse<>(200, "Success", data);
       }
   }
   ```

3. **Nested Objects vs IDs**
    - **Issue:** Some responses return only IDs (e.g., `sellerId: 123`)
    - **FE Expects:** Nested objects (e.g., `seller: { id, name, avatar }`)
    - **Fix:** Create minimal nested response DTOs

4. **Date Serialization**
    - **Ensure:** `LocalDateTime` serializes to ISO 8601 format
    - **Configure:** Jackson `@JsonFormat` or global settings

---

## 13. Implementation Priority Matrix

### 🔴 Priority 1: Critical (Blocking FE Features)

**Timeline: Immediate (1-2 weeks)**

1. **Review System** (🔴 Highest)
    - [ ] Create ReviewRequest/Response DTOs
    - [ ] Implement ReviewController with CRUD
    - [ ] Add review statistics aggregation
    - [ ] Update BookMeta with rating fields
    - **Effort:** 3-4 days
    - **Blocks:** Book detail pages, seller ratings

2. **Order Response DTOs** (🔴 Critical)
    - [ ] Create OrderResponse (buyer view)
    - [ ] Create SellerOrderResponse (seller view)
    - [ ] Create UpdateOrderStatusRequest
    - [ ] Implement order status update endpoint
    - **Effort:** 2 days
    - **Blocks:** Order management pages

3. **Wishlist API** (🔴 Critical)
    - [ ] Create WishlistController
    - [ ] Create Wishlist DTOs
    - [ ] Implement CRUD endpoints
    - **Effort:** 1-2 days
    - **Blocks:** Wishlist feature

4. **Seller Analytics** (🔴 Critical)
    - [ ] Create SellerAnalyticsService
    - [ ] Create all analytics DTOs (Revenue, Sales, etc.)
    - [ ] Implement SellerAnalyticsController
    - [ ] Write database aggregation queries
    - **Effort:** 5-7 days
    - **Blocks:** Entire seller dashboard

### 🟡 Priority 2: Important (Degraded UX)

**Timeline: 2-4 weeks**

5. **Promotion System** (🟡 High)
    - [ ] Create Promotion entity
    - [ ] Create Promotion DTOs
    - [ ] Implement PromotionController
    - [ ] Add promotion relationship to Listing
    - [ ] Create scheduled job for auto-expire
    - **Effort:** 4-5 days
    - **Blocks:** Seller promotions, discount display

6. **Pro Seller Application** (🟡 High)
    - [ ] Create ProSellerApplication entity
    - [ ] Create application DTOs
    - [ ] Implement application flow
    - [ ] Add admin approval workflow
    - **Effort:** 3-4 days
    - **Blocks:** Seller tier system

7. **Book Response Aggregation** (🟡 Medium)
    - [ ] Create unified BookResponse DTO
    - [ ] Include seller, authors, categories, discount
    - [ ] Add computed fields (finalPrice, etc.)
    - **Effort:** 2-3 days
    - **Blocks:** Book catalog display quality

8. **Pagination Standardization** (🟡 Medium)
    - [ ] Create PagedResponse wrapper
    - [ ] Update all list endpoints
    - [ ] Ensure consistent format
    - **Effort:** 2 days
    - **Blocks:** Pagination controls

### 🟢 Priority 3: Polish (Nice to Have)

**Timeline: 4-6 weeks**

9. **Entity Field Additions** (🟢 Low)
    - [ ] Add language to BookMeta
    - [ ] Add ward/district to ShippingAddress
    - [ ] Add isProSeller to UserProfile
    - **Effort:** 1-2 days

10. **Enum Alignment** (🟢 Low)
    - [ ] Align ListingStatus with FE
    - [ ] Align OrderStatus with FE
    - [ ] Add AccountType enum
    - **Effort:** 1 day

11. **Collection Entity** (🟢 Optional)
    - [ ] Create Collection entity
    - [ ] Implement CRUD
    - [ ] Add to admin panel
    - **Effort:** 2-3 days

---

## 14. Detailed Action Items

### For Backend Team

#### Week 1-2: Critical APIs
```
Day 1-2: Review System
- [ ] Create Review DTOs (Request, Response, Stats)
- [ ] Update Review entity (add helpfulCount, verifiedPurchase)
- [ ] Implement ReviewController (CRUD + helpful marking)
- [ ] Add review aggregation queries
- [ ] Test with Postman

Day 3-4: Order DTOs
- [ ] Create OrderResponse and SellerOrderResponse
- [ ] Create UpdateOrderStatusRequest
- [ ] Update order endpoints to return new DTOs
- [ ] Test order flow end-to-end

Day 5-6: Wishlist
- [ ] Create WishlistController
- [ ] Create Wishlist DTOs
- [ ] Implement endpoints (list, add, remove, check)
- [ ] Test integration with FE

Day 7-10: Seller Analytics
- [ ] Create all analytics DTOs
- [ ] Implement SellerAnalyticsService with queries
- [ ] Create SellerAnalyticsController
- [ ] Test with actual data
- [ ] Optimize slow queries
```

#### Week 3-4: Important Features
```
Day 11-15: Promotion System
- [ ] Create Promotion entity and migration
- [ ] Create Promotion DTOs
- [ ] Implement PromotionController
- [ ] Add promotion to Listing relationship
- [ ] Create scheduler for auto-expiry
- [ ] Update BookResponse to include discount

Day 16-18: Pro Seller Application
- [ ] Create ProSellerApplication entity
- [ ] Create application DTOs
- [ ] Implement application endpoints
- [ ] Add admin approval endpoints
- [ ] Test application workflow

Day 19-20: Book Response Aggregation
- [ ] Create unified BookResponse DTO
- [ ] Include all nested objects (seller, authors, etc.)
- [ ] Add computed fields
- [ ] Update book endpoints
```

### For Frontend Team (Parallel Work)

#### While Waiting for APIs
```
- [ ] Mock review responses for UI development
- [ ] Design wishlist UI components
- [ ] Build seller analytics dashboard UI with mock data
- [ ] Create promotion management UI
- [ ] Prepare test cases for integration
```

#### After APIs Ready
```
- [ ] Integrate review system
- [ ] Test review submission and display
- [ ] Integrate wishlist functionality
- [ ] Connect seller dashboard to analytics API
- [ ] Test promotion creation and display
- [ ] End-to-end testing of all flows
```

---

## 15. Testing Checklist

### Unit Tests Needed
- [ ] Review CRUD operations
- [ ] Wishlist uniqueness constraint
- [ ] Promotion date validation
- [ ] Analytics calculation accuracy
- [ ] Pagination logic

### Integration Tests Needed
- [ ] Full review flow (create → display → update → delete)
- [ ] Order lifecycle (create → process → ship → deliver)
- [ ] Promotion application to listings
- [ ] Analytics endpoint performance with large datasets
- [ ] Cart → Checkout → Order flow

### API Contract Tests
- [ ] Response format matches FE types
- [ ] Enum values align
- [ ] Pagination structure correct
- [ ] Error responses standardized

---

## 16. Database Migration Summary

### New Tables Required
```sql
-- Promotion table
CREATE TABLE promotion (
    id BIGSERIAL PRIMARY KEY,
    seller_id BIGINT NOT NULL REFERENCES "user"(id),
    name VARCHAR(255) NOT NULL,
    discount_percentage INTEGER NOT NULL CHECK (discount_percentage BETWEEN 0 AND 100),
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_revenue DECIMAL(12, 2),
    items_sold INTEGER,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Promotion-Listing junction table
CREATE TABLE promotion_listing (
    promotion_id BIGINT REFERENCES promotion(id),
    listing_id BIGINT REFERENCES listing(id),
    PRIMARY KEY (promotion_id, listing_id)
);

-- Pro Seller Application table
CREATE TABLE pro_seller_application (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES "user"(id),
    business_name VARCHAR(255) NOT NULL,
    business_address TEXT NOT NULL,
    business_phone VARCHAR(20) NOT NULL,
    tax_id VARCHAR(50) NOT NULL,
    business_license_number VARCHAR(50),
    business_description TEXT,
    years_in_business INTEGER,
    monthly_inventory INTEGER,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    review_notes TEXT,
    submitted_at TIMESTAMP DEFAULT NOW(),
    reviewed_at TIMESTAMP,
    reviewed_by BIGINT REFERENCES "user"(id)
);

-- Pro Seller Application Documents (optional)
CREATE TABLE pro_seller_documents (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT REFERENCES pro_seller_application(id),
    document_url TEXT NOT NULL,
    document_type VARCHAR(50)
);

-- Review Helpful tracking
CREATE TABLE review_helpful (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES "user"(id),
    review_id BIGINT NOT NULL REFERENCES review(id),
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_id, review_id)
);
```

### Alter Existing Tables
```sql
-- BookMeta additions
ALTER TABLE book_meta ADD COLUMN language VARCHAR(10) DEFAULT 'en';
ALTER TABLE book_meta ADD COLUMN average_rating DECIMAL(3, 2);
ALTER TABLE book_meta ADD COLUMN total_reviews INTEGER DEFAULT 0;

-- Review additions
ALTER TABLE review ADD COLUMN helpful_count INTEGER DEFAULT 0;
ALTER TABLE review ADD COLUMN verified_purchase BOOLEAN DEFAULT FALSE;
ALTER TABLE review ADD COLUMN updated_at TIMESTAMP;
ALTER TABLE review ADD CONSTRAINT unique_user_book UNIQUE (user_id, book_id);

-- Listing additions
ALTER TABLE listing ADD COLUMN active_promotion_id BIGINT REFERENCES promotion(id);
ALTER TABLE listing ADD COLUMN last_viewed_at TIMESTAMP;

-- UserProfile additions
ALTER TABLE user_profile ADD COLUMN is_pro_seller BOOLEAN DEFAULT FALSE;
ALTER TABLE user_profile ADD COLUMN cover_image_url TEXT;
ALTER TABLE user_profile ADD COLUMN response_time VARCHAR(50);
ALTER TABLE user_profile ADD COLUMN fulfillment_rate DECIMAL(5, 2);

-- ShippingAddress additions
ALTER TABLE shipping_address ADD COLUMN ward VARCHAR(100);
ALTER TABLE shipping_address ADD COLUMN district VARCHAR(100);
ALTER TABLE shipping_address ADD COLUMN note TEXT;

-- Order additions
ALTER TABLE orders ADD COLUMN buyer_notes TEXT;
```

---

## Summary Table: Missing vs Existing

| Feature | Entity | DTOs | Controller | Status |
|---------|--------|------|------------|--------|
| **Auth** | ✅ User | ✅ Mostly | ✅ Yes | ⚠️ Minor fixes |
| **Profile** | ✅ UserProfile | ⚠️ Incomplete | ✅ Yes | ⚠️ Need response DTOs |
| **Books** | ✅ BookMeta | ⚠️ Incomplete | ✅ Yes | ⚠️ Need aggregated response |
| **Listings** | ✅ Listing | ✅ Yes | ✅ Yes | ⚠️ Minor fields |
| **Cart** | ✅ Cart, CartItem | ⚠️ Basic | ✅ Yes | ⚠️ Need full response |
| **Orders** | ✅ Order, OrderItem | ❌ Missing | ✅ Yes | ❌ Need DTOs |
| **Payment** | ✅ Payment | ✅ Yes | ✅ Yes | ✅ Good |
| **Reviews** | ✅ Review | ❌ Missing | ❌ No | ❌ Critical gap |
| **Wishlist** | ✅ Wishlist | ❌ Missing | ❌ No | ❌ Critical gap |
| **Promotions** | ❌ Missing | ❌ Missing | ❌ No | ❌ Critical gap |
| **Analytics** | N/A | ❌ Missing | ❌ No | ❌ Critical gap |
| **Pro Seller** | ❌ Missing | ❌ Missing | ❌ No | ❌ Critical gap |
| **Vouchers** | ✅ Voucher | ✅ Yes | ✅ Yes | ✅ Good |
| **Shipping** | ✅ ShippingAddress | ✅ Yes | ✅ Yes | ⚠️ Minor fields |

---

## Final Recommendations

### Immediate Actions (This Sprint)
1. **Implement Review System** - 3-4 days - Blocks product ratings
2. **Create Order DTOs** - 2 days - Blocks order management
3. **Build Wishlist API** - 2 days - Blocks wishlist feature
4. **Develop Seller Analytics** - 5-7 days - Blocks seller dashboard

### Short Term (Next Sprint)
5. **Build Promotion System** - 4-5 days
6. **Implement Pro Seller Application** - 3-4 days
7. **Standardize Pagination** - 2 days
8. **Create Unified Book Response** - 2-3 days

### Medium Term (2-3 Sprints)
9. **Add missing entity fields** - 2 days
10. **Align all enums** - 1 day
11. **Implement Collection feature** - 3 days
12. **Add notification system** - 4-5 days

### Code Quality
- Add comprehensive unit tests for new controllers
- Document all new DTOs with Javadoc
- Create Postman collection for all endpoints
- Update API documentation (Swagger/OpenAPI)
- Perform load testing on analytics endpoints

---

**Document Version:** 1.0  
**Last Updated:** January 3, 2026  
**Next Review:** After Priority 1 implementation complete
