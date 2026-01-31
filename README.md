# Bookverse — Backend API

A production-ready REST API for a full-featured online book marketplace, built with Spring Boot 3.2 and Java 21.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)
![Stripe](https://img.shields.io/badge/Stripe-Integrated-blueviolet)

---

## 🎯 What This Is

The backend API powering **Bookverse**, a marketplace where book lovers buy and sell pre-owned and new books with confidence. This is a complete, end-to-end implementation — not a tutorial project.

**Key Stats:**
- 305+ commits over 5 months of development
- 95+ REST API endpoints
- 5 user roles with granular permissions
- Real payment processing (Stripe)
- Production-grade architecture

---

## ✨ Features

### Core Marketplace
- **Book Catalog** — Rich metadata with Open Library integration, authors, categories
- **Listings** — Seller inventory with condition grading, pricing, photos
- **Search & Discovery** — Full-text search, filters, category browsing
- **Shopping Cart** — Persistent cart with real-time stock validation

### Transactions
- **Checkout Flow** — Session-based checkout with address management
- **Payment Processing** — Real Stripe integration (Payment Intents API)
- **Order Management** — Full lifecycle: pending → processing → shipped → delivered
- **Refunds & Cancellations** — Automated refund processing via Stripe

### Seller Tools
- **Seller Dashboard** — Sales analytics, order management, inventory tracking
- **Payout System** — Commission calculation, payout requests, balance tracking
- **Promotions** — Discount campaigns, voucher codes
- **PRO Seller Tier** — Application process, reduced commission (3%), bulk tools

### Platform Administration
- **User Management** — Role-based access control (USER, SELLER, PRO_SELLER, MODERATOR, ADMIN)
- **Content Moderation** — Flagged listings, user reports, dispute resolution
- **Analytics Dashboard** — Platform-wide metrics, revenue tracking

### Security & Auth
- **JWT Authentication** — Stateless auth with refresh tokens
- **OAuth 2.0** — Google sign-in integration
- **Email Verification** — OTP-based account verification
- **Role-Based Authorization** — Method-level security with Spring Security

---

## 🛠 Tech Stack

| Layer | Technology |
|-------|------------|
| **Framework** | Spring Boot 3.2 |
| **Language** | Java 21 |
| **Database** | PostgreSQL 15+ |
| **ORM** | Spring Data JPA / Hibernate |
| **Security** | Spring Security + JWT |
| **Payments** | Stripe API |
| **Email** | Brevo (Sendinblue) SMTP |
| **Object Mapping** | MapStruct |
| **Validation** | Jakarta Bean Validation |
| **API Docs** | SpringDoc OpenAPI (Swagger) |
| **Build** | Maven |

---

## 📁 Project Structure

```
src/main/java/com/example/bookverseserver/
├── controller/          # REST endpoints
├── service/             # Business logic
├── repository/          # Data access layer
├── entity/              # JPA entities
│   ├── User/            # User, UserProfile, ShippingAddress
│   ├── Product/         # BookMeta, Listing, Author, Category
│   └── Order_Payment/   # Order, Cart, Payment, Voucher
├── dto/                 # Request/Response objects
│   ├── request/
│   └── response/
├── mapper/              # MapStruct mappers
├── exception/           # Global error handling
├── configuration/       # Spring configuration
├── security/            # JWT, OAuth, filters
└── enums/               # Status enums
```

---

## 🚀 Getting Started

### Prerequisites
- Java 21+
- PostgreSQL 15+
- Maven 3.9+

### Configuration

Create `application.properties` or set environment variables:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/bookverse
spring.datasource.username=your_username
spring.datasource.password=your_password

# JWT
jwt.signerKey=your_secret_key
jwt.valid-duration=3600
jwt.refreshable-duration=86400

# Stripe
stripe.api.key=sk_test_xxx

# OAuth (Google)
spring.security.oauth2.client.registration.google.client-id=xxx
spring.security.oauth2.client.registration.google.client-secret=xxx
```

### Run

```bash
# Clone
git clone https://github.com/Leroy-sketch-png/bookverse-be.git
cd bookverse-be

# Build
./mvnw clean install

# Run
./mvnw spring-boot:run
```

API available at `http://localhost:8080`

Swagger UI at `http://localhost:8080/swagger-ui.html`

---

## 📖 API Overview

| Category | Endpoints | Description |
|----------|-----------|-------------|
| **Auth** | `/api/auth/*` | Login, register, OAuth, password reset |
| **Users** | `/api/users/*` | Profile, addresses, become seller |
| **Books** | `/api/books/*` | Catalog, metadata, authors, categories |
| **Listings** | `/api/listings/*` | CRUD, search, filters |
| **Cart** | `/api/cart/*` | Add, update, remove items |
| **Orders** | `/api/orders/*` | Checkout, status, history |
| **Payments** | `/api/payments/*` | Stripe intents, webhooks |
| **Seller** | `/api/seller/*` | Dashboard, analytics, payouts |
| **Admin** | `/api/admin/*` | Users, moderation, platform stats |

---

## 👨‍💻 Development Context

This is a complete rebuild of a marketplace platform. I architected and implemented the entire backend end-to-end as part of an academic team project, taking ownership of:

- Database schema design (15+ entities, complex relationships)
- RESTful API design following OpenAPI standards
- Authentication & authorization architecture
- Payment integration with real Stripe processing
- Seller ecosystem with analytics and payouts

**Total Contribution:** 305 commits | Aug 2025 – Jan 2026

---

## 📄 Related

- **Frontend Repository:** [bookverse-fe](https://github.com/Leroy-sketch-png/bookverse-fe)

---

## 📝 License

This project was developed for educational purposes.
