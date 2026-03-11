# NeoNHS-BE — Codebase Index

> **Last updated:** 2026-03-11
> **Project:** RESTful API for Neo-Ngu Hanh Son Tourism Application
> **FPT University Capstone Project © 2026**

---

## 1. Overview

**NeoNHS-BE** is a Spring Boot 4.0 backend API (Java 21) for a tourism application focused on **Ngũ Hành Sơn (Marble Mountains)** in Da Nang, Vietnam. It supports:

- User management & authentication (email + Google OAuth)
- Tourist attractions & points of interest management
- Events management with ticketing
- Workshop discovery, creation, and approval workflows (Vendor ↔ Admin)
- Ticket purchasing, orders, and transactions (VNPay/various payment methods)
- Reviews, blogs, vouchers, notifications, check-ins
- Role-based access (Tourist, Vendor, Admin)

---

## 2. Tech Stack

| Technology       | Version / Details                     |
| ---------------- | ------------------------------------- |
| Java             | 21                                    |
| Spring Boot      | 4.0.1                                 |
| Spring Data JPA  | Hibernate + MySQL Dialect             |
| Spring Security  | JWT (jjwt 0.12.6) + Google OAuth      |
| Database         | MySQL 8.0 (Docker, port 3307 → 3306)  |
| Cache / Sessions | Redis (cloud-hosted)                  |
| Email            | Spring Mail + Thymeleaf templates     |
| API Docs         | SpringDoc OpenAPI / Swagger UI        |
| Build            | Maven 3.9+ (wrapper included)         |
| Containerization | Docker + Docker Compose               |
| CI/CD            | GitHub Actions → Docker Hub → VPS SSH |
| Code Generation  | Lombok (1.18.30)                      |

---

## 3. Project Structure

```text
NeoNHS-BE/
├── .github/workflows/ci.yml         # CI/CD pipeline (Build → Docker → Deploy)
├── Dockerfile                        # JRE 21 Alpine image
├── docker-compose.yaml               # Local MySQL 8 container
├── pom.xml                           # Maven dependencies
├── .env / .env.example               # Environment variables
├── src/
│   ├── main/
│   │   ├── java/fpt/project/NeoNHS/
│   │   │   ├── NeoNhsApplication.java       # Entry point
│   │   │   ├── config/                      # Configuration classes
│   │   │   ├── constants/                   # Shared constants
│   │   │   ├── controller/                  # REST controllers (Root, admin/, vendor/)
│   │   │   ├── dto/                         # Request/Response DTOs
│   │   │   ├── entity/                      # (37 files) JPA entities
│   │   │   ├── enums/                       # (20 files) Enum types
│   │   │   ├── exception/                   # Custom exceptions + GlobalExceptionHandler
│   │   │   ├── helpers/                     # Utility classes
│   │   │   ├── repository/                  # (34 files) Spring Data JPA repositories
│   │   │   ├── security/                    # JWT + UserDetails
│   │   │   ├── service/                     # (42 interfaces + impl/) Service layer
│   │   │   └── specification/               # JPA Specifications for filtering
│   │   └── resources/
│   │       ├── application.yaml             # App config
│   │       └── templates/                   # Thymeleaf email templates
│   └── test/                                # Test sources
└── MD/WorkshopMD/                           # Workshop documentation
```

---

## 4. Entity (Domain Model) Map

### 4.1 Core Entities & Relationships

All main entities extend **`BaseEntity`** which provides `createdAt`, `updatedAt`, `deletedAt`, `deletedBy`, and UUID IDs.

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                              USER (users)                                   │
│  id, fullname, email, passwordHash, phoneNumber, avatarUrl,                │
│  role(TOURIST/VENDOR/ADMIN), isActive, isVerified, isBanned                │
├─────────────────────────────────────────────────────────────────────────────┤
│  1:1  → VendorProfile         1:N → UserVoucher, Notification, Report      │
│  1:N  → UserVisitedPoint       1:N → UserCheckIn, Order, Review, Blog      │
│  1:1  → Cart                                                               │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                       ATTRACTION (attractions)                              │
│  id, name, description, mapImageUrl, address, latitude, longitude,         │
│  status, thumbnailUrl, openHour, closeHour, isActive                       │
├─────────────────────────────────────────────────────────────────────────────┤
│  1:N  → Point                  1:N → TicketCatalog                         │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                           POINT (points)                                    │
│  id, name, description, thumbnailUrl, history, historyAudioUrl,            │
│  latitude, longitude, orderIndex, estTimeSpent, type                       │
├─────────────────────────────────────────────────────────────────────────────┤
│  N:1  → Attraction             1:N → CheckinPoint, UserVisitedPoint        │
│                                1:N → PointHistoryAudio, PanoramaHotSpot    │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                           EVENT (events)                                    │
│  id, name, shortDescription, fullDescription, locationName,                │
│  latitude, longitude, startTime, endTime, isTicketRequired,                │
│  price, maxParticipants, currentEnrolled, status                           │
├─────────────────────────────────────────────────────────────────────────────┤
│  1:N  → EventTag, TicketCatalog, Review, EventImage                        │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                   WORKSHOP_TEMPLATE (workshop_templates)                     │
│  id, name, shortDescription, fullDescription, estimatedDuration,           │
│  defaultPrice, minParticipants, maxParticipants, status,                   │
│  rejectReason, approvedBy, approvedAt, averageRating, totalReview          │
├─────────────────────────────────────────────────────────────────────────────┤
│  N:1  → VendorProfile          1:N → WorkshopSession, WorkshopImage        │
│  1:N  → WorkshopTag, Review                                                │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                    VENDOR_PROFILE (vendor_profiles)                          │
│  id, businessName, description, address, latitude, longitude,              │
│  taxCode, bankName, bankAccountNumber, bankAccountName, isVerified         │
├─────────────────────────────────────────────────────────────────────────────┤
│  1:1  → User                   1:N → WorkshopTemplate                      │
└─────────────────────────────────────────────────────────────────────────────┘

┌───────────────────────────────────────┐  ┌──────────────────────────────────┐
│        ORDER (orders)                 │  │      TICKET (tickets)            │
│  id, totalAmount, discountAmount,     │  │  id, qrCode, ticketCode,        │
│  finalAmount                          │  │  ticketType, status,             │
│  N:1 → User, Voucher                 │  │  issueDate, expiryDate,          │
│  1:N → OrderDetail, Transaction       │  │  redeemedAt                      │
└───────────────────────────────────────┘  │  N:1 → TicketCatalog,            │
                                           │         WorkshopSession,          │
                                           │         OrderDetail               │
                                           └──────────────────────────────────┘

┌───────────────────────────────────────┐  ┌──────────────────────────────────┐
│     TICKET_CATALOG (ticket_catalogs)  │  │      VOUCHER (vouchers)          │
│  id, name, description, customerType, │  │  id, code, description,          │
│  price, originalPrice, applyOnDays,   │  │  discountType, discountValue,    │
│  validFromDate, validToDate,          │  │  maxDiscountValue, minOrderValue, │
│  totalQuota, status                   │  │  startDate, endDate, usageLimit, │
│  N:1 → Attraction, Event             │  │  usageCount, status              │
│  1:N → Ticket, OrderDetail, CartItem  │  │  1:N → UserVoucher, Order        │
└───────────────────────────────────────┘  └──────────────────────────────────┘

┌───────────────────────────────────────┐  ┌──────────────────────────────────┐
│         REVIEW (reviews)              │  │         BLOG (blogs)             │
│  id, rating, comment, status          │  │  id, title, slug, summary,       │
│  N:1 → User, Event, WorkshopTemplate  │  │  content, thumbnailUrl,          │
│  1:N → ReviewImage                    │  │  bannerUrl, isFeatured, status,  │
└───────────────────────────────────────┘  │  publishedAt, tags, viewCount    │
                                           │  N:1 → BlogCategory, User        │
                                           └──────────────────────────────────┘
```

### 4.2 Supporting / Join Entities

| Entity              | Table                | Purpose                                     |
| ------------------- | -------------------- | ------------------------------------------- |
| `EventTag`          | composite key        | Event ↔ ETag many-to-many                   |
| `WorkshopTag`       | composite key        | WorkshopTemplate ↔ WTag M2M                 |
| `ETag`              | tags                 | Tag for events                              |
| `WTag`              | tags                 | Tag for workshops                           |
| `WorkshopSession`   | workshop_sessions    | Scheduled session of a template             |
| `WorkshopImage`     | workshop_images      | Image gallery for workshop                  |
| `ReviewImage`       | review_images        | Image attached to review                    |
| `CheckinPoint`      | checkin_points       | Checkin location at a point                 |
| `CheckinImage`      | checkin_images       | Image for a checkin                         |
| `UserCheckIn`       | user_check_ins       | User ↔ Checkin record                       |
| `UserVisitedPoint`  | user_visited_points  | User ↔ Point visit record                   |
| `UserVoucher`       | user_vouchers        | User ↔ Voucher assignment                   |
| `Cart`              | carts                | User's shopping cart                        |
| `CartItem`          | cart_items           | Item in a cart                              |
| `OrderDetail`       | order_details        | Line item in an order                       |
| `Transaction`       | transactions         | Payment transaction for an order            |
| `Notification`      | notifications        | Push/in-app notification                    |
| `Report`            | reports              | User report on content                      |
| `BlogCategory`      | blog_categories      | Category for blogs                          |
| `PointHistoryAudio` | point_history_audios | Audio guide and history content for a Point |
| `PanoramaHotSpot`   | panorama_hot_spots   | 360-degree panorama hotspots for a Point    |
| `EventImage`        | event_images         | Image gallery for event                     |

---

## 5. Enums (21 Total)

Key enums defining logic flows:
`UserRole`, `PointType`, `EventStatus`, `WorkshopStatus`, `AttractionStatus`, `BlogStatus`, `BlogCategoryStatus`, `ReviewStatus`, `TicketType`, `TicketStatus`, `TicketCatalogStatus`, `SessionStatus`, `TransactionStatus`, `OrderStatus`, `ReportStatus`, `VoucherStatus`, `VoucherType`, `VoucherScope`, `DiscountType`, `ApplicableProduct`.

---

## 6. API Endpoints (Controllers)

### 6.1 Public & User APIs

| Controller                        | Base Path                | Key Features                                         |
| --------------------------------- | ------------------------ | ---------------------------------------------------- |
| `AuthController`                  | `/api/auth`              | Login, Register, Google OAuth, Refresh, Verify Email |
| `EventController`                 | `/api/events`            | Event listing & details (Filtered, Search)           |
| `PointController`                 | `/api/points`            | Points within attractions, details                   |
| `UserController`                  | `/api/users`             | User profile management                              |
| `VendorProfileController`         | `/api/vendors`           | Vendor registration and basic profile operations     |
| `WorkshopTemplateController`      | `/api/workshops`         | Workshop listings, submission, search                |
| `PaymentController`               | `/api/payment`           | Payment processing (VNPay support)                   |
| `CartController`                  | `/api/cart`              | User cart management                                 |
| `OrderController`                 | `/api/orders`            | User orders management                               |
| `BlogController`                  | `/api/blogs`             | Viewing published blogs                              |
| `VoucherController`               | `/api/vouchers`          | Voucher claiming and application lists               |
| `HistoryAudioController`          | `/api/history-audio`     | Retrieving Point history audio details               |
| `CheckinPointController`          | `/api/checkins`          | Check-in locations and logic                         |
| `PayoutController`                | `/api/payout`            | Payout operations                                    |
| `UploadController`                | `/api/upload`            | Image and file uploads                               |
| `UserCheckinController`           | `/api/user-checkins`     | Checking in users to Points                          |
| `WorkshopSessionController`       | `/api/workshop-sessions` | Managing and booking workshop sessions               |
| `WorkshopTouristController`       | `/api/tourist/workshops` | Tourist specific workshop interfaces                 |
| `AdminVendorManagementController` | `/api/admin/vendors`     | Root endpoint variant for vendor management          |

### 6.2 Admin APIs (`/api/admin/...`)

Comprehensive CRUD and analytical capabilities restricted to `ADMIN` role. Specific controllers exist for:
`AdminAttractionController`, `AdminBlogCategoryController`, `AdminBlogController`, `AdminCheckinPointController`, `AdminDashboardController`, `AdminETagController`, `AdminEventController`, `AdminHistoryAudioController`, `AdminPanoramaController`, `AdminPointController`, `AdminReportController`, `AdminRevenueController`, `AdminTicketCatalogController`, `AdminUserController`, `AdminVoucherController`.

### 6.3 Vendor APIs (`/api/vendor/...`)

Vendor-specific endpoints like `VendorVoucherController` for managing vendor-issued vouchers.

---

## 7. Service Layer

### 7.1 Key Business Services

| Service                                                | Key Features                                                                       |
| ------------------------------------------------------ | ---------------------------------------------------------------------------------- |
| `AuthServiceImpl`                                      | Login, register, Google OAuth, OTP verification, password reset, JWT token refresh |
| `AttractionServiceImpl`                                | Full CRUD with complex pagination & `AttractionSpecification` filtering            |
| `EventServiceImpl`                                     | Creation, updates, soft delete/restore, advanced search                            |
| `CartServiceImpl`                                      | Shopping cart modifications and calculation logic                                  |
| `OrderServiceImpl`                                     | Checkout process, discount application, order state transitions                    |
| `VoucherServiceImpl`                                   | Claiming limits, validation checks, global/vendor-specific handling                |
| `WorkshopTemplateServiceImpl`                          | Full lifecycle (DRAFT, PENDING, ACTIVE), admin approval flow                       |
| `PanoramaServiceImpl`                                  | Management of 360 images and hotspots                                              |
| `PointHistoryAudioServiceImpl`                         | History audio text and JSON word playback sync configuration                       |
| `DashboardServiceImpl` / `RevenueAnalyticsServiceImpl` | Aggregated statistics, revenue tracking for admins                                 |
| `VnptEkycServiceImpl`                                  | Integration for eKYC (Know Your Customer) verifications                            |
| `MailServiceImpl`                                      | Asynchronous email service with Thymeleaf parsing                                  |
| `CloudinaryImageUploadServiceImpl`                     | Cloudinary Java SDK integration for image uploads                                  |
| `AdminVendorManagementServiceImpl`                     | Managing and monitoring vendor profiles for admins                                 |
| `FaceVerificationServiceImpl`                          | NeoNHS Python face verification microservice integration                           |

_(Many other supporting services exist for CartItems, OrderDetails, Reviews, Tags, etc, totaling 42 services.)_

---

## 8. Security Architecture

| Component                  | Description                                                       |
| -------------------------- | ----------------------------------------------------------------- |
| `SecurityConfig`           | Stateless JWT, CORS, BCrypt passwords, Route permissions          |
| `JwtAuthenticationFilter`  | Extracts JWT from `Authorization` header, validates, sets context |
| `JwtTokenProvider`         | Generates & validates JWT tokens (24h expiry, HS256 algorithm)    |
| `CustomUserDetailsService` | Loads `User` entity by email for Spring Security                  |
| `UserPrincipal`            | Implements `UserDetails`, wraps `User` entity                     |
| `GoogleTokenVerifier`      | Verifies Google OAuth ID tokens                                   |

---

## 9. Configuration

| Config Class         | Purpose                                                |
| -------------------- | ------------------------------------------------------ |
| `SecurityConfig`     | HTTP security, CORS, JWT filter chain                  |
| `RedisConfig`        | Redis connection (cloud-hosted endpoint)               |
| `EmailConfiguration` | JavaMailSender with Gmail SMTP                         |
| `OpenApiConfig`      | Swagger/OpenAPI metadata configuration + Bearer schema |
| `AsyncConfig`        | Async task execution                                   |

---

## 10. File Statistics

| Package        | Files     | Notes                                                        |
| -------------- | --------- | ------------------------------------------------------------ |
| `entity`       | 37        | All JPA entities including newer ones like `PanoramaHotSpot` |
| `repository`   | 34        | Spring Data JPA repos                                        |
| `service`      | 42        | Interface definitions                                        |
| `service/impl` | 42        | Concrete class implementations                               |
| `controller`   | 45        | 29 root + 15 admin + 1 vendor public/private APIs            |
| `enums`        | 21        | All status/type definitions                                  |
| **Total**      | **~221+** | Core Java source files (excluding DTOs/Exceptions/Configs)   |

---
