# Bookverse — Online Book Marketplace

A full-stack, multi-sided marketplace where users buy and sell books with real Stripe payment processing, AI-powered recommendations, hybrid content moderation, and a complete trust-and-safety pipeline. Built with Spring Boot 3.2 and Next.js 15.

> **This README covers the backend.** Frontend repo: [bookverse-fe](https://github.com/Leroy-sketch-png/bookverse-fe)

---

## Architecture

```
                        ┌─────────────────────────┐
                        │   Next.js 15 Frontend    │
                        │  React 19 · TypeScript   │
                        │  Stripe.js · STOMP Chat  │
                        └────────────┬────────────┘
                                     │ HTTPS
                                     ▼
                        ┌─────────────────────────┐
                        │   Spring Boot 3.2 API    │
                        │   37 Controllers · JWT   │
                        │   95+ Endpoints          │
                        └─────┬─────┬─────┬───────┘
                              │     │     │
              ┌───────────────┘     │     └───────────────┐
              ▼                     ▼                     ▼
     ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
     │  Core Commerce   │  │   Payments &    │  │ Trust & Safety  │
     │                 │  │   Seller Ops    │  │                 │
     │ Books/Listings  │  │                 │  │ Content Moder.  │
     │ Cart/Checkout   │  │ Stripe Intents  │  │ Flagged Listings│
     │ Orders          │  │ Connect Express │  │ User Reports    │
     │ Search/Filter   │  │ Subscriptions   │  │ Disputes        │
     │ Reviews         │  │ Refunds         │  │ Warnings        │
     │ Collections     │  │ Seller Payouts  │  │ Suspensions     │
     │ Wishlists       │  │ Commission Calc │  │ Audit Trail     │
     └────────┬────────┘  └────────┬────────┘  └────────┬────────┘
              │                    │                     │
              ▼                    ▼                     ▼
     ┌──────────────────────────────────────────────────────────┐
     │                    PostgreSQL 15                          │
     │   38+ JPA entities · Flyway migrations · JPA Specs       │
     └──────────────────────────────────────────────────────────┘
              │                    │                     │
              ▼                    ▼                     ▼
     ┌──────────────┐   ┌─────────────────┐   ┌──────────────┐
     │  Cloudinary   │   │  Stripe API     │   │  7 LLM       │
     │  + AWS S3     │   │  (Payments,     │   │  Providers   │
     │  (images)     │   │   Connect,      │   │  (AI recs,   │
     │               │   │   Webhooks)     │   │   moderation)│
     └──────────────┘   └─────────────────┘   └──────────────┘
```

---

## System Boundaries

| Domain | Entities | External Dependencies |
|--------|----------|-----------------------|
| **User & Auth** | User, UserProfile, Role, ShippingAddress, AuthProvider, OwnedBook, ReadingLog | Keycloak (OAuth2), Google, Brevo (email OTP) |
| **Product Catalog** | BookMeta, Listing, Author, Category, Collection, CuratedCollection, Review, Wishlist, BookTag | Open Library API (ISBN enrichment), Cloudinary |
| **Orders & Payments** | Cart, CartItem, CheckoutSession, Order, OrderItem, OrderTimeline, Payment, SellerPayout, Voucher, Promotion | Stripe (Payment Intents, Connect Express, Subscriptions, Webhooks) |
| **Seller Operations** | ProSellerApplication, SellerSettings | Stripe Connect (onboarding, payouts, billing portal) |
| **Trust & Safety** | FlaggedListing, UserReport, Dispute, Warning, Suspension, ModerationAction | AI moderation (7 LLM providers with failover) |
| **Messaging** | Conversation, ChatMessage | WebSocket/STOMP |

---

## Failure Handling

### Stripe Webhook Resilience

```
Stripe event arrives at /api/payments/webhook
         │
         ▼
Signature verification (Webhook.constructEvent)
         │
         ├── Invalid signature → 400 (reject silently)
         └── Valid →
              │
              ▼
         Switch on event type:
              │
              ├── payment_intent.succeeded  → Payment COMPLETED, Order CONFIRMED
              ├── payment_intent.failed     → Payment FAILED (no order state change)
              ├── charge.refunded           → Payment REFUNDED, Order REFUNDED
              ├── customer.subscription.*   → PRO tier upgrade/downgrade
              │
              └── Fallback: Payment verification endpoint
                   (polls Stripe if webhook is delayed)
```

### Content Moderation (hybrid pipeline)

```
User-generated text
         │
         ▼
Rule-based scorer
   Blocked terms (with leet-speak normalization + Cyrillic homoglyph detection)
   Troll patterns, spam detection
         │
         ├── Score ≥ 75  → BLOCK (no AI call needed)
         ├── Score ≤ 24  → APPROVE
         └── Score 25-74 → AI decides (intent analysis)
                              │
                              ├── AI available → nuanced verdict
                              └── AI down → FLAG for manual review
```

### Payment Failure Handling

Orders use a state machine: `PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED`. Payment failure at any point doesn't corrupt order state — the payment entity tracks its own lifecycle independently. Refunds are processed through Stripe's Refund API and reflected back via webhook.

---

## Design Decisions

**Why 5 roles (not just user/admin)?**
A marketplace has fundamentally different actors. Buyers browse and purchase. Sellers manage inventory and fulfill orders. PRO Sellers get reduced commission (3% vs 8%) and enhanced tools. Moderators handle flagged content without access to financial data. Admins manage the platform. Collapsing these into fewer roles would either over-privilege or under-serve each actor.

**Why Stripe Connect Express (not just Payment Intents)?**
The platform takes a commission and pays sellers. Connect Express handles seller onboarding (KYC/identity verification), holds funds, and allows the platform to transfer earnings to sellers' bank accounts. Without Connect, the platform would need to handle money transmission compliance directly.

**Why hybrid content moderation (not pure AI)?**
Same reasoning as Chefkix — rule-based scoring handles the clear cases instantly (< 5ms), AI only activates for the ambiguous 25-74 score band. Leet-speak normalization (`"f u c k"` → `"fuck"`, `"4ss"` → `"ass"`) and Cyrillic homoglyph detection catch evasion attempts that naive regex misses.

**Why 7 AI providers with rotation?**
Book recommendations and mood-based discovery are AI-powered. Free-tier rate limits on any single provider would bottleneck the feature. The multi-provider rotator with failover keeps the feature available without API costs.

**Why JPA Specifications (not just repository queries)?**
The listing search supports 10+ filter dimensions (price range, condition, category, author, seller rating, etc.) in any combination. Writing a repository method for every filter permutation is impossible. JPA Specifications compose dynamically — each filter is a reusable predicate that combines at query time.

---

## Stripe Integration (complete)

| Feature | Stripe API | Flow |
|---------|-----------|------|
| **Book purchases** | Payment Intents | Client → create intent → confirm → webhook confirms |
| **Refunds** | Refund API | Admin/seller triggers → Stripe processes → webhook updates |
| **Seller onboarding** | Connect Express | Seller applies → onboarding URL → KYC → account active |
| **Seller payouts** | Transfers + Payouts | Platform → transfer to connected account → payout to bank |
| **PRO subscriptions** | Checkout Sessions (subscription mode) | Seller → checkout → monthly billing → webhook manages tier |
| **Seller dashboard** | Balance, Transfers | Seller views earnings, transfer history, billing portal |

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| **Backend** | Java 21, Spring Boot 3.2, Spring Security, Spring Data JPA |
| **Frontend** | Next.js 15, React 19, TypeScript, Tailwind CSS 4, Zustand, TanStack Query |
| **Database** | PostgreSQL 15, Flyway |
| **Payments** | Stripe (Payment Intents, Connect Express, Subscriptions, Webhooks, Refunds) |
| **Auth** | JWT + OAuth2 (Google), email OTP via Brevo |
| **AI** | 7 LLM providers (Gemini, Groq, Cohere, Mistral, HuggingFace, Fireworks, OpenRouter) |
| **Storage** | Cloudinary, AWS S3 |
| **Real-time** | WebSocket/STOMP (buyer-seller chat) |
| **Mapping** | MapStruct (19 mappers) |
| **API Docs** | SpringDoc OpenAPI / Swagger (all 37 controllers documented) |
| **Infrastructure** | Docker, GitHub Actions CI/CD → DockerHub → Render |

---

## Running Locally

```bash
git clone https://github.com/Leroy-sketch-png/bookverse-be.git
cd bookverse-be

# Start PostgreSQL
docker-compose up -d

# Configure environment
cp .env.example .env   # Add Stripe keys, JWT secret, OAuth credentials

# Run
./mvnw spring-boot:run
```

API: `http://localhost:8080` · Swagger: `http://localhost:8080/swagger-ui.html`

---

## Stats

- **306 commits** over 5 months (backend)
- **483 Java source files**, **38+ JPA entities**, **95+ API endpoints**
- **19 MapStruct mappers**, **66 request DTOs**, **121 response DTOs**
- Full CI/CD pipeline: lint → build → Docker → deploy to cloud
