# Quick Reference: New DTOs & Entities

## 📁 File Organization

### New Request DTOs
```
dto/request/
├── Authentication/
│   └── PasswordResetRequest.java ✅
├── Order/
│   └── UpdateOrderStatusRequest.java ✅
├── Promotion/
│   ├── CreatePromotionRequest.java ✅
│   └── UpdatePromotionRequest.java ✅
├── Review/
│   ├── CreateReviewRequest.java ✅
│   └── UpdateReviewRequest.java ✅
├── User/
│   └── ProSellerApplicationRequest.java ✅
└── Wishlist/
    └── AddToWishlistRequest.java ✅
```

### New Response DTOs
```
dto/response/
├── Analytics/
│   ├── ListingsStats.java ✅
│   ├── OrdersBreakdown.java ✅
│   ├── ProductPerformance.java ✅
│   ├── RatingData.java ✅
│   ├── RevenueData.java ✅
│   ├── SalesData.java ✅
│   ├── SellerStatsResponse.java ✅
│   └── ViewsData.java ✅
├── Book/
│   └── BookResponse.java ✅
├── Cart/
│   └── CartItemResponse.java ✅
├── Order/
│   ├── OrderItemResponse.java ✅
│   ├── OrderResponse.java ✅
│   ├── SellerOrderItemResponse.java ✅
│   └── SellerOrderResponse.java ✅
├── Promotion/
│   └── PromotionResponse.java ✅
├── Review/
│   ├── ReviewResponse.java ✅
│   ├── ReviewsListResponse.java ✅
│   └── ReviewStatsResponse.java ✅
├── User/
│   ├── ProSellerApplicationResponse.java ✅
│   ├── SellerProfileResponse.java ✅
│   └── SellerRegistrationResponse.java ✅
├── Wishlist/
│   ├── WishlistItemResponse.java ✅
│   └── WishlistResponse.java ✅
└── PagedResponse.java ✅
```

### New Entities
```
entity/
├── Product/
│   └── Promotion.java ✅
└── User/
    └── ProSellerApplication.java ✅
```

### New Enums
```
enums/
├── AccountType.java ✅
├── ApplicationStatus.java ✅
└── PromotionStatus.java ✅
```

### Updated Entities
```
entity/
├── Product/
│   ├── BookMeta.java ✅ (+ language, averageRating, totalReviews)
│   ├── Listing.java ✅ (+ activePromotion, lastViewedAt, getFinalPrice())
│   └── Review.java ✅ (+ helpfulCount, verifiedPurchase, updatedAt)
└── User/
    ├── ShippingAddress.java ✅ (+ ward, district, note)
    └── UserProfile.java ✅ (+ coverImageUrl, isProSeller, responseTime, fulfillmentRate)
```

### Updated Response DTOs
```
dto/response/
└── User/
    ├── ProfileResponse.java ✅ (added new fields)
    └── UserResponse.java ✅ (role → roles)
```

---

## 🔑 Key Changes at a Glance

### Critical Fixes
1. **UserResponse.role → roles** (Set<RoleResponse>)
2. **Review entity** - Added helpful/verified fields
3. **Promotion entity** - Complete new system
4. **ProSellerApplication** - New application flow

### New Systems
1. **Review System** - Complete CRUD with statistics
2. **Wishlist** - Add/remove books
3. **Promotions** - Seller discounts on listings
4. **Pro Seller** - Application and approval workflow
5. **Analytics** - Comprehensive seller dashboard data

### Enhanced Entities
- **BookMeta**: language, ratings, review count
- **UserProfile**: pro seller flag, metrics
- **Listing**: promotion relationship, computed prices
- **ShippingAddress**: ward, district, delivery notes

---

## 📊 Database Changes Summary

### New Tables (5)
1. `promotion` - Seller promotional campaigns
2. `promotion_listing` - Many-to-many junction
3. `pro_seller_application` - Application records
4. `pro_seller_documents` - Document URLs
5. `review_helpful` - Track helpful votes

### Updated Tables (6)
1. `book_meta` - +3 columns
2. `review` - +3 columns
3. `user_profile` - +4 columns
4. `shipping_address` - +3 columns
5. `listing` - +2 columns
6. `orders` - +1 column

### Indexes Created (15+)
- Performance optimized for common queries
- Foreign key indexes
- Composite indexes for filters

### Triggers Created (2)
1. Auto-update book review statistics
2. Auto-update review helpful count

---

## 🚀 Next Steps for Development

### Immediate (Week 1-2)
1. ✅ DTOs & Entities ← **DONE**
2. ⏳ Database Migration
3. ⏳ ReviewController + Service
4. ⏳ WishlistController + Service

### Short-term (Week 3-4)
5. ⏳ PromotionController + Service
6. ⏳ AnalyticsController + Service
7. ⏳ Update existing controllers
8. ⏳ Mapper classes

### Before Production
9. ⏳ Unit tests (80%+ coverage)
10. ⏳ Integration tests
11. ⏳ API documentation
12. ⏳ Performance testing

---

## 📝 Common Patterns Used

### Request DTOs
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class XxxRequest {
    @NotBlank(message = "...")
    String field;
}
```

### Response DTOs
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class XxxResponse {
    Long id;
    String field;
}
```

### Entities
```java
@Entity
@Table(name = "table_name")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Entity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    
    @CreationTimestamp
    LocalDateTime createdAt;
}
```

### Enums
```java
public enum Status {
    VALUE_ONE,
    VALUE_TWO
}
```

---

## 🔍 Finding What You Need

### By Feature
- **Authentication**: `dto/request/Authentication`, `dto/response/Authentication`
- **Reviews**: `dto/request/Review`, `dto/response/Review`, `entity/Product/Review.java`
- **Wishlist**: `dto/request/Wishlist`, `dto/response/Wishlist`, `entity/Product/Wishlist.java`
- **Promotions**: `dto/request/Promotion`, `dto/response/Promotion`, `entity/Product/Promotion.java`
- **Analytics**: `dto/response/Analytics`
- **Orders**: `dto/request/Order`, `dto/response/Order`, `entity/Order_Payment`

### By Type
- **All Requests**: `dto/request/**`
- **All Responses**: `dto/response/**`
- **All Entities**: `entity/**`
- **All Enums**: `enums/**`

---

## 📚 Documentation Files

1. **DTO_ENTITY_ANALYSIS.md** - Original analysis and requirements
2. **IMPLEMENTATION_SUMMARY.md** - Complete implementation guide
3. **database_migration_20260103.sql** - SQL migration script
4. **QUICK_REFERENCE.md** - This file

---

## ✅ Validation Rules

### Common Validations Used
- `@NotNull` - Field required
- `@NotBlank` - String required and not empty
- `@Email` - Valid email format
- `@Size(min=X, max=Y)` - String length
- `@Min(X)` / `@Max(Y)` - Number range
- `@Pattern(regexp="...")` - Regex match

### Review Validations
- Rating: 1-5 (inclusive)
- Comment: 10-1000 characters

### Promotion Validations
- Discount: 1-100%
- End date > Start date

---

## 🎯 Testing Strategy

### Unit Test Template
```java
@Test
void testCreateXxx() {
    // Given
    XxxRequest request = XxxRequest.builder()
        .field("value")
        .build();
    
    // When
    XxxResponse response = service.create(request);
    
    // Then
    assertNotNull(response.getId());
    assertEquals("value", response.getField());
}
```

### Integration Test Template
```java
@SpringBootTest
@AutoConfigureMockMvc
class XxxControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testEndpoint() throws Exception {
        mockMvc.perform(post("/api/xxx")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }
}
```

---

**Last Updated:** January 3, 2026  
**Version:** 1.0  
**Status:** All DTOs & Entities Complete ✅
