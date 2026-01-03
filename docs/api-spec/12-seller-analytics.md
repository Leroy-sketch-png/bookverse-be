# Module 12: Seller Analytics

**Status:** ❌ Missing (0% Complete)  
**Priority:** 🟡 High  
**Sprint:** Sprint 2 (Week 4)  
**Assigned To:** _[To be assigned]_  
**Estimated Effort:** 2 days

---

## 📋 Overview

Comprehensive analytics dashboard for sellers to track their business performance including revenue, orders, products, and customer insights.

**Key Features:**
- Revenue and sales metrics
- Order statistics
- Product performance analysis
- Traffic and conversion analytics
- Time-based trend analysis
- Exportable reports

---

## 🎯 Business Rules

1. **Authorization:** Only sellers can access analytics
2. **Data Scope:** Sellers only see data for their own listings
3. **Time Periods:** Support multiple time ranges (7d, 30d, 90d, 1y, all-time, custom)
4. **Real-time Updates:** Analytics update within 1 hour of transactions
5. **Data Aggregation:** Daily aggregation for performance
6. **Privacy:** No buyer PII exposed in analytics

---

## 📡 API Endpoints

### Endpoint Summary Table

| Method | Endpoint | Description | Auth | Status |
|--------|----------|-------------|------|--------|
| GET | `/api/seller/analytics/overview` | Dashboard summary | Seller | ❌ Not Implemented |
| GET | `/api/seller/analytics/revenue` | Revenue chart data | Seller | ❌ Not Implemented |
| GET | `/api/seller/analytics/orders` | Order statistics | Seller | ❌ Not Implemented |
| GET | `/api/seller/analytics/products` | Product performance | Seller | ❌ Not Implemented |
| GET | `/api/seller/analytics/traffic` | Traffic & views | Seller | ❌ Not Implemented |
| GET | `/api/seller/analytics/customers` | Customer insights | Seller | ❌ Not Implemented |
| GET | `/api/seller/analytics/export` | Export report | Seller | ❌ Not Implemented |

---

## 🔧 Implementation Details

### 1. Analytics Overview
**GET** `/api/seller/analytics/overview`

**Authorization:** `@PreAuthorize("hasRole('SELLER')")`

**Query Parameters:**
```
?period=7d|30d|90d|1y|all|custom
&startDate=2026-01-01  (if period=custom)
&endDate=2026-01-31    (if period=custom)
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "period": "30d",
    "revenue": {
      "total": 12450.50,
      "change": 23.5,
      "changeType": "increase"
    },
    "orders": {
      "total": 195,
      "change": 15.2,
      "changeType": "increase",
      "breakdown": {
        "pending": 12,
        "processing": 23,
        "shipped": 45,
        "delivered": 105,
        "cancelled": 10
      }
    },
    "products": {
      "totalListings": 87,
      "activeListings": 72,
      "soldOut": 8,
      "avgPrice": 38.50
    },
    "customers": {
      "total": 142,
      "newCustomers": 38,
      "returningCustomers": 104
    },
    "traffic": {
      "totalViews": 5420,
      "uniqueVisitors": 3210,
      "conversionRate": 6.1
    },
    "averageOrderValue": 63.85,
    "topSellingProduct": {
      "id": 123,
      "title": "Clean Code",
      "sales": 45,
      "revenue": 2069.55
    }
  }
}
```

**Implementation:**
```java
@GetMapping("/overview")
public ResponseEntity<ApiResponse<AnalyticsOverviewDto>> getOverview(
    @RequestParam(defaultValue = "30d") String period,
    @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate startDate,
    @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate endDate,
    @AuthenticationPrincipal User currentUser
) {
    DateRange dateRange = parsePeriod(period, startDate, endDate);
    
    AnalyticsOverviewDto analytics = analyticsService.getOverview(
        currentUser.getId(),
        dateRange
    );
    
    return ResponseEntity.ok(ApiResponse.success(analytics));
}
```

**Service Layer:**
```java
@Service
@RequiredArgsConstructor
public class SellerAnalyticsService {
    private final OrderItemRepository orderItemRepository;
    private final ListingRepository listingRepository;
    
    public AnalyticsOverviewDto getOverview(Long sellerId, DateRange range) {
        // Calculate revenue
        BigDecimal totalRevenue = calculateRevenue(sellerId, range);
        Double revenueChange = calculateRevenueChange(sellerId, range);
        
        // Calculate order stats
        OrderStats orderStats = calculateOrderStats(sellerId, range);
        
        // Calculate product stats
        ProductStats productStats = calculateProductStats(sellerId);
        
        // Calculate customer stats
        CustomerStats customerStats = calculateCustomerStats(sellerId, range);
        
        // Calculate traffic stats
        TrafficStats trafficStats = calculateTrafficStats(sellerId, range);
        
        // Find top selling product
        ProductSummary topProduct = findTopSellingProduct(sellerId, range);
        
        return AnalyticsOverviewDto.builder()
            .period(range.getPeriodString())
            .revenue(new RevenueDto(totalRevenue, revenueChange))
            .orders(orderStats)
            .products(productStats)
            .customers(customerStats)
            .traffic(trafficStats)
            .topSellingProduct(topProduct)
            .build();
    }
    
    private BigDecimal calculateRevenue(Long sellerId, DateRange range) {
        return orderItemRepository.sumRevenueForSeller(
            sellerId,
            range.getStart(),
            range.getEnd()
        );
    }
}
```

**Repository Queries:**
```java
@Query("""
    SELECT SUM(oi.subtotal)
    FROM OrderItem oi
    JOIN oi.listing l
    WHERE l.seller.id = :sellerId
    AND oi.order.createdAt BETWEEN :start AND :end
    AND oi.order.paymentStatus = 'COMPLETED'
""")
BigDecimal sumRevenueForSeller(
    @Param("sellerId") Long sellerId,
    @Param("start") LocalDateTime start,
    @Param("end") LocalDateTime end
);
```

---

### 2. Revenue Chart Data
**GET** `/api/seller/analytics/revenue`

**Query Parameters:**
```
?period=30d
&groupBy=day|week|month
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "period": "30d",
    "groupBy": "day",
    "currency": "USD",
    "dataPoints": [
      {
        "date": "2026-01-01",
        "revenue": 450.50,
        "orders": 7,
        "avgOrderValue": 64.36
      },
      {
        "date": "2026-01-02",
        "revenue": 380.25,
        "orders": 5,
        "avgOrderValue": 76.05
      }
    ],
    "summary": {
      "totalRevenue": 12450.50,
      "totalOrders": 195,
      "avgDailyRevenue": 415.02,
      "peakDay": {
        "date": "2026-01-15",
        "revenue": 825.00
      }
    }
  }
}
```

**Use Case:** Powers revenue chart on seller dashboard

**Implementation:**
```java
@Query("""
    SELECT DATE(oi.order.createdAt) as date,
           SUM(oi.subtotal) as revenue,
           COUNT(DISTINCT oi.order.id) as orders
    FROM OrderItem oi
    JOIN oi.listing l
    WHERE l.seller.id = :sellerId
    AND oi.order.createdAt BETWEEN :start AND :end
    AND oi.order.paymentStatus = 'COMPLETED'
    GROUP BY DATE(oi.order.createdAt)
    ORDER BY DATE(oi.order.createdAt)
""")
List<RevenueDataPoint> getRevenueByDay(
    Long sellerId,
    LocalDateTime start,
    LocalDateTime end
);
```

---

### 3. Order Statistics
**GET** `/api/seller/analytics/orders`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "period": "30d",
    "total": 195,
    "statusBreakdown": {
      "PENDING": 12,
      "PROCESSING": 23,
      "SHIPPED": 45,
      "DELIVERED": 105,
      "CANCELLED": 10
    },
    "fulfillmentMetrics": {
      "avgProcessingTime": "1.5 days",
      "avgShippingTime": "3.2 days",
      "fulfillmentRate": 94.9,
      "onTimeDeliveryRate": 92.3
    },
    "trends": [
      {
        "date": "2026-01-01",
        "orders": 7
      }
    ]
  }
}
```

---

### 4. Product Performance
**GET** `/api/seller/analytics/products`

**Query Parameters:**
```
?period=30d
&sortBy=revenue|sales|views
&page=0
&size=20
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "listingId": 301,
      "book": {
        "id": 123,
        "title": "Clean Code",
        "coverImage": "...",
        "isbn": "978-0132350884"
      },
      "performance": {
        "sales": 45,
        "revenue": 2069.55,
        "views": 1250,
        "conversionRate": 3.6,
        "avgPrice": 45.99,
        "stockRemaining": 15
      },
      "ranking": 1
    }
  ],
  "meta": {
    "page": 0,
    "totalPages": 4,
    "totalItems": 72
  }
}
```

**Use Case:** Identify best and worst performing products

---

### 5. Traffic & Conversion Analytics
**GET** `/api/seller/analytics/traffic`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "period": "30d",
    "totalViews": 5420,
    "uniqueVisitors": 3210,
    "pageViews": {
      "listings": 4820,
      "sellerProfile": 600
    },
    "conversionFunnel": {
      "views": 5420,
      "addedToCart": 420,
      "checkoutInitiated": 245,
      "completed": 195
    },
    "conversionRate": 3.6,
    "bounceRate": 42.5,
    "avgSessionDuration": "3m 25s",
    "topReferrers": [
      {
        "source": "Google Search",
        "visits": 1850,
        "conversions": 89
      },
      {
        "source": "Direct",
        "visits": 1200,
        "conversions": 52
      }
    ]
  }
}
```

---

### 6. Customer Insights
**GET** `/api/seller/analytics/customers`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "period": "30d",
    "totalCustomers": 142,
    "newCustomers": 38,
    "returningCustomers": 104,
    "repeatPurchaseRate": 28.5,
    "customerLifetimeValue": {
      "avg": 185.50,
      "median": 95.00,
      "top10Percent": 850.00
    },
    "topCustomers": [
      {
        "customerId": 50,
        "username": "book_lover",
        "totalOrders": 12,
        "totalSpent": 1250.00,
        "lastOrderDate": "2026-01-25"
      }
    ],
    "customerAcquisition": [
      {
        "date": "2026-01-01",
        "newCustomers": 2
      }
    ]
  }
}
```

**Privacy Note:** Only show username, no email or personal data

---

### 7. Export Analytics Report
**GET** `/api/seller/analytics/export`

**Query Parameters:**
```
?period=30d
&format=csv|pdf|excel
```

**Response:** File download

**Headers:**
```
Content-Type: application/vnd.ms-excel
Content-Disposition: attachment; filename="analytics-report-2026-01.xlsx"
```

**Report Contents:**
- Overview summary
- Revenue breakdown
- Top products
- Order details
- Customer summary

---

## 🗄️ Database Schema

### AnalyticsSnapshot Entity (Optional - for performance)

```java
@Entity
@Table(name = "analytics_snapshots")
public class AnalyticsSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;
    
    @Column(name = "snapshot_date")
    private LocalDate snapshotDate;
    
    @Column(name = "total_revenue", precision = 12, scale = 2)
    private BigDecimal totalRevenue;
    
    @Column(name = "total_orders")
    private Integer totalOrders;
    
    @Column(name = "total_views")
    private Integer totalViews;
    
    @Column(name = "new_customers")
    private Integer newCustomers;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
```

**Note:** This is optional. Snapshots can be pre-calculated daily for better performance.

### Indexes for Analytics Queries

```sql
-- Order analytics
CREATE INDEX idx_order_items_seller_date ON order_items(listing_id, created_at);
CREATE INDEX idx_orders_payment_date ON orders(payment_status, created_at);

-- Listing analytics
CREATE INDEX idx_listing_seller_views ON listing(seller_id, view_count);
CREATE INDEX idx_listing_seller_sold ON listing(seller_id, sold_count);

-- Composite indexes
CREATE INDEX idx_orderitem_seller_payment ON order_items(listing_id) 
    INCLUDE (subtotal, quantity, created_at);
```

---

## 📦 DTOs

### AnalyticsOverviewDto
```java
public class AnalyticsOverviewDto {
    private String period;
    private RevenueDto revenue;
    private OrderStatsDto orders;
    private ProductStatsDto products;
    private CustomerStatsDto customers;
    private TrafficStatsDto traffic;
    private BigDecimal averageOrderValue;
    private ProductSummaryDto topSellingProduct;
}
```

### RevenueDto
```java
public class RevenueDto {
    private BigDecimal total;
    private Double change;
    private String changeType; // "increase" or "decrease"
}
```

### RevenueChartDto
```java
public class RevenueChartDto {
    private String period;
    private String groupBy;
    private String currency;
    private List<RevenueDataPointDto> dataPoints;
    private RevenueSummaryDto summary;
}
```

### RevenueDataPointDto
```java
public class RevenueDataPointDto {
    private String date;
    private BigDecimal revenue;
    private Integer orders;
    private BigDecimal avgOrderValue;
}
```

---

## 🧪 Testing Requirements

### Unit Tests

```java
@Test
void getOverview_CalculatesRevenueCorrectly() {
    // Given: Orders with known totals
    // When: getOverview() called
    // Then: Correct total revenue returned
}

@Test
void getRevenue_GroupsByDay_ReturnsCorrectDataPoints() {
    // Given: Orders across multiple days
    // When: getRevenue(groupBy="day")
    // Then: One data point per day
}

@Test
void getProductPerformance_SortsBySales_ReturnsTopProducts() {
    // Given: Multiple products with different sales
    // When: getProducts(sortBy="sales")
    // Then: Products sorted correctly
}

@Test
void getCustomerInsights_CalculatesRepeatRate() {
    // Given: Customers with multiple orders
    // When: getCustomerInsights()
    // Then: Repeat purchase rate calculated correctly
}
```

### Integration Tests

```java
@Test
@WithMockUser(roles = "SELLER", userId = "50")
void getOverview_Returns200() throws Exception {
    mockMvc.perform(get("/api/seller/analytics/overview")
        .param("period", "30d"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.revenue.total").exists());
}

@Test
@WithMockUser(roles = "USER")
void getOverview_NonSeller_Returns403() throws Exception {
    mockMvc.perform(get("/api/seller/analytics/overview"))
        .andExpect(status().isForbidden());
}
```

---

## 🔗 Dependencies

### Related Modules
- **Module 11:** Seller Dashboard (uses analytics data)
- **Module 04:** Listings (view counts)
- **Module 07:** Orders (revenue calculations)

### External Services
- None (purely internal calculations)

---

## ✅ Acceptance Criteria

- [ ] Sellers can view analytics overview for different time periods
- [ ] Revenue chart shows daily/weekly/monthly breakdown
- [ ] Order statistics calculate correctly
- [ ] Product performance ranks by sales/revenue/views
- [ ] Traffic analytics show conversion funnel
- [ ] Customer insights calculate repeat rate correctly
- [ ] Analytics export works for CSV/PDF/Excel
- [ ] All calculations exclude cancelled/refunded orders
- [ ] Analytics update within 1 hour of transactions
- [ ] Performance: Overview loads in < 1 second
- [ ] All endpoints return standardized format
- [ ] Unit test coverage ≥ 80%

---

## 📅 Timeline

| Task | Estimated Time | Status |
|------|----------------|--------|
| Implement overview endpoint | 4 hours | ❌ |
| Implement revenue chart | 3 hours | ❌ |
| Implement order statistics | 2 hours | ❌ |
| Implement product performance | 3 hours | ❌ |
| Implement traffic analytics | 2 hours | ❌ |
| Implement customer insights | 3 hours | ❌ |
| Implement export functionality | 2 hours | ❌ |
| Create database indexes | 1 hour | ❌ |
| Write tests | 3 hours | ❌ |
| **Total** | **23 hours (3 days)** | |

---

## 📝 Notes

**Performance Considerations:**
- Consider caching analytics for 1 hour
- Use database indexes for complex queries
- Consider pre-calculating daily snapshots
- Monitor query performance

**Future Enhancements:**
- Real-time analytics dashboard
- Predictive analytics (forecast sales)
- Comparative analytics (vs. previous period)
- Competitor benchmarking (anonymous)
- Custom report builder
- Email analytics reports
- Mobile app analytics

**Data Privacy:**
- Never expose buyer PII
- Aggregate data only
- Comply with data retention policies

---

## ✔️ Sign-off

**Developer:** _________________ Date: _______  
**Reviewer:** _________________ Date: _______  
**QA:** _________________ Date: _______