# NeoNHS-BE — Codebase Index

> **Last updated:** 2026-02-10
> **Project:** RESTful API for Neo-Ngu Hanh Son Tourism Application
> **FPT University Capstone Project © 2026**

---

## 1. Overview

**NeoNHS-BE** is a Spring Boot 4.0 backend API (Java 21) for a tourism application focused on **Ngũ Hành Sơn (Marble Mountains)** in Da Nang, Vietnam. It supports:

- User management & authentication (email + Google OAuth)
- Tourist attractions & points of interest management
- Events management with ticketing
- Workshop discovery, creation, and approval workflows (Vendor ↔ Admin)
- Ticket purchasing, orders, and transactions
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

```
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
│   │   │   ├── config/                      # (7 files) Configuration classes
│   │   │   ├── constants/                   # (2 files) Constants
│   │   │   ├── controller/                  # (18 files + admin/) REST controllers
│   │   │   │   └── admin/                   # (1 file) Admin-only controllers
│   │   │   ├── dto/                         # (36 files) Request/Response DTOs
│   │   │   │   ├── request/                 # auth(11), attraction(2), event(3), point(1), workshop(5)
│   │   │   │   └── response/               # root(4), attraction(1), auth(3), event(2), point(1), workshop(3)
│   │   │   ├── entity/                      # (34 files) JPA entities
│   │   │   ├── enums/                       # (15 files) Enum types
│   │   │   ├── exception/                   # (9 files) Custom exceptions + GlobalExceptionHandler
│   │   │   ├── helpers/                     # (4 files) Utility classes
│   │   │   ├── repository/                  # (31 files) Spring Data JPA repositories
│   │   │   ├── security/                    # (4 files) JWT + UserDetails
│   │   │   ├── service/                     # (30 interfaces + impl/) Service layer
│   │   │   │   └── impl/                    # (30 files) Service implementations
│   │   │   └── specification/               # (3 files) JPA Specifications for filtering
│   │   └── resources/
│   │       ├── application.yaml             # App config
│   │       └── templates/                   # Thymeleaf email templates (2 files)
│   └── test/                                # Test sources
└── MD/WorkshopMD/                           # Workshop documentation (6 files)
```

---

## 4. Entity (Domain Model) Map

### 4.1 Core Entities & Relationships

All main entities extend **`BaseEntity`** which provides:

- `createdAt`, `updatedAt` (auto-timestamps)
- `deletedAt`, `deletedBy` (soft delete support)
- All IDs are `UUID` with `@GeneratedValue(strategy = GenerationType.UUID)`

```
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
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                           EVENT (events)                                    │
│  id, name, shortDescription, fullDescription, locationName,                │
│  latitude, longitude, startTime, endTime, isTicketRequired,                │
│  price, maxParticipants, currentEnrolled, status                           │
├─────────────────────────────────────────────────────────────────────────────┤
│  1:N  → EventTag, TicketCatalog, Review                                    │
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

| Entity             | Table               | Purpose                          |
| ------------------ | ------------------- | -------------------------------- |
| `EventTag`         | composite key       | Event ↔ ETag many-to-many        |
| `WorkshopTag`      | composite key       | WorkshopTemplate ↔ WTag M2M      |
| `ETag`             | tags                | Tag for events                   |
| `WTag`             | tags                | Tag for workshops                |
| `WorkshopSession`  | workshop_sessions   | Scheduled session of a template  |
| `WorkshopImage`    | workshop_images     | Image gallery for workshop       |
| `ReviewImage`      | review_images       | Image attached to review         |
| `CheckinPoint`     | checkin_points      | Checkin location at a point      |
| `CheckinImage`     | checkin_images      | Image for a checkin              |
| `UserCheckIn`      | user_check_ins      | User ↔ Checkin record            |
| `UserVisitedPoint` | user_visited_points | User ↔ Point visit record        |
| `UserVoucher`      | user_vouchers       | User ↔ Voucher assignment        |
| `Cart`             | carts               | User's shopping cart             |
| `CartItem`         | cart_items          | Item in a cart                   |
| `OrderDetail`      | order_details       | Line item in an order            |
| `Transaction`      | transactions        | Payment transaction for an order |
| `Notification`     | notifications       | Push/in-app notification         |
| `Report`           | reports             | User report on content           |
| `BlogCategory`     | blog_categories     | Category for blogs               |

---

## 5. Enums

| Enum                  | Values                                                                                                                                  |
| --------------------- | --------------------------------------------------------------------------------------------------------------------------------------- |
| `UserRole`            | `TOURIST`, `VENDOR`, `ADMIN`                                                                                                            |
| `PointType`           | `PAGODA`, `CAVE`, `VIEWPOINT`, `GENERAL`, `CHECKIN`, `STATUE`, `GATE`, `SHOP`, `ELEVATOR`, `EVENT`, `WORKSHOP`, `ATTRACTION`, `DEFAULT` |
| `EventStatus`         | `UPCOMING`, `ONGOING`, `COMPLETED`, `CANCELLED`                                                                                         |
| `WorkshopStatus`      | `DRAFT`, `PENDING`, `ACTIVE`, `REJECTED`                                                                                                |
| `AttractionStatus`    | (active/inactive status values)                                                                                                         |
| `BlogStatus`          | `DRAFT`, `PUBLISHED` (+ others)                                                                                                         |
| `ReviewStatus`        | `VISIBLE`, `HIDDEN`                                                                                                                     |
| `TicketType`          | Ticket types enum                                                                                                                       |
| `TicketStatus`        | `ACTIVE` + others                                                                                                                       |
| `TicketCatalogStatus` | `ACTIVE` + others                                                                                                                       |
| `SessionStatus`       | Workshop session status                                                                                                                 |
| `TransactionStatus`   | Payment status                                                                                                                          |
| `ReportStatus`        | Report lifecycle                                                                                                                        |
| `VoucherStatus`       | `ACTIVE` + others                                                                                                                       |
| `DiscountType`        | Discount types (percentage/fixed)                                                                                                       |

---

## 6. API Endpoints (Controllers)

### 6.1 Public APIs

| Controller                | Base Path        | Key Endpoints                                                                                                                                                                                                                                     |
| ------------------------- | ---------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `AuthController`          | `/api/auth`      | `POST /login`, `POST /register`, `POST /google-login`, `GET /me`, `POST /refresh-token`, `POST /logout`, `POST /change-password`, `POST /forgot-password`, `POST /reset-password`, `POST /verify`, `GET /verify-link`, `GET /resend-verify-email` |
| `EventController`         | `/api/events`    | `GET /` (paginated+filtered), `GET /all` (no pagination), `GET /{id}`                                                                                                                                                                             |
| `PointController`         | `/api/points`    | `POST /`, `GET /{id}`, `GET /all/{attractionId}`, `GET /attraction/{attractionId}` (paginated), `PUT /{id}`, `DELETE /{id}`                                                                                                                       |
| `UserController`          | `/api/users`     | `GET /profile`, `PUT /update-profile/{id}`                                                                                                                                                                                                        |
| `VendorProfileController` | `/api/vendors`   | `POST /register`, `GET /profile`, `PUT /{id}`                                                                                                                                                                                                     |
| `WorkshopController`      | `/api/workshops` | Full CRUD + lifecycle: create, get, list, filter, update, register (submit for approval), approve, reject, delete                                                                                                                                 |

### 6.2 Admin APIs

| Controller             | Base Path           | Key Endpoints                                             |
| ---------------------- | ------------------- | --------------------------------------------------------- |
| `AttractionController` | `/api/attractions`  | CRUD (Admin-only via `@PreAuthorize("hasRole('ADMIN')")`) |
| `AdminEventController` | `/api/admin/events` | CRUD + soft delete + restore (Admin-only)                 |

### 6.3 Stub Controllers (minimal/empty implementations)

`BlogController`, `CartController`, `CheckinController`, `NotificationController`, `OrderController`, `ReportController`, `ReviewController`, `TicketController`, `TransactionController`, `VoucherController`

---

## 7. Service Layer

### 7.1 Fully Implemented Services

| Service                       | Key Features                                                                                          |
| ----------------------------- | ----------------------------------------------------------------------------------------------------- |
| `AuthServiceImpl`             | Login, register, Google OAuth, email verification (OTP), password reset, JWT refresh, logout          |
| `AttractionServiceImpl`       | CRUD with pagination, search, filtering via `AttractionSpecification`                                 |
| `EventServiceImpl`            | CRUD, pagination, filtering via `EventSpecification`, soft delete/restore                             |
| `PointServiceImpl`            | CRUD, pagination by attraction, search                                                                |
| `WorkshopTemplateServiceImpl` | Full lifecycle: CRUD, submit for approval, approve/reject, filter via `WorkshopTemplateSpecification` |
| `UserServiceImpl`             | Profile retrieval and update                                                                          |
| `VendorProfileServiceImpl`    | Vendor account creation, profile get/update                                                           |
| `WTagServiceImpl`             | Workshop tag CRUD                                                                                     |
| `RedisAuthServiceImpl`        | Redis-backed OTP storage, refresh token management                                                    |
| `MailServiceImpl`             | Email sending via Thymeleaf templates                                                                 |

### 7.2 Stub Services (interface + basic impl)

`BlogService`, `BlogCategoryService`, `CartService`, `CartItemService`, `CheckinImageService`, `CheckinPointService`, `ETagService`, `NotificationService`, `OrderService`, `OrderDetailService`, `ReportService`, `ReviewService`, `ReviewImageService`, `TicketService`, `TicketCatalogService`, `TransactionService`, `UserCheckInService`, `VoucherService`, `WorkshopImageService`, `WorkshopSessionService`

---

## 8. Security Architecture

| Component                  | Description                                                       |
| -------------------------- | ----------------------------------------------------------------- |
| `SecurityConfig`           | Stateless JWT, CORS (localhost:5173, :3000), BCrypt passwords     |
| `JwtAuthenticationFilter`  | Extracts JWT from `Authorization` header, validates, sets context |
| `JwtTokenProvider`         | Generates & validates JWT tokens (24h expiry, HS256)              |
| `CustomUserDetailsService` | Loads `User` entity by email for Spring Security                  |
| `UserPrincipal`            | Implements `UserDetails`, wraps `User` entity                     |
| `GoogleTokenVerifier`      | Verifies Google OAuth ID tokens                                   |

### Security Rules (SecurityConfig)

- `OPTIONS /**` → permitAll
- `/api/auth/**` → permitAll (except `/api/auth/test-protected`)
- `/api/public/**` → permitAll
- `/api/attractions/**` → ADMIN only
- `/api/points/**` → permitAll
- `/swagger-ui/**`, `/v3/api-docs/**` → permitAll
- Individual methods use `@PreAuthorize` annotations

---

## 9. Configuration

| Config Class         | Purpose                                                  |
| -------------------- | -------------------------------------------------------- |
| `SecurityConfig`     | HTTP security, CORS, JWT filter chain                    |
| `RedisConfig`        | Redis connection (cloud-hosted)                          |
| `EmailConfiguration` | JavaMailSender with Gmail SMTP                           |
| `OpenApiConfig`      | Swagger/OpenAPI metadata + JWT security scheme           |
| `AsyncConfig`        | Async task execution                                     |
| `DataInitializer`    | Seeds admin user + 5 Ngu Hanh Son attractions on startup |
| `StartupLogger`      | Logs app startup info                                    |

### Environment Variables (`.env.example`)

- `GOOGLE_AUTH_CLIENT_ID`, `GOOGLE_AUTH_CLIENT_SECRET`
- `REDIS_HOST`, `REDIS_USERNAME`, `REDIS_PASSWORD`
- `GOOGLE_MAIL_PASSWORD`

---

## 10. Exception Handling

`GlobalExceptionHandler` (`@RestControllerAdvice`) wraps all errors in `ApiResponse<Void>`:

| Exception                         | HTTP Status      |
| --------------------------------- | ---------------- |
| `BadRequestException`             | 400              |
| `HttpMessageNotReadableException` | 400 (JSON parse) |
| `MethodArgumentNotValidException` | 400 (validation) |
| `IllegalArgumentException`        | 400              |
| `OTPException`                    | 400              |
| `UnauthorizedException`           | 401              |
| `BadCredentialsException`         | 401              |
| `AuthenticationException`         | 401              |
| `InvalidTokenException`           | 401              |
| `ResourceNotFoundException`       | 404              |
| `Exception` (catch-all)           | 500              |

---

## 11. CI/CD Pipeline

**File:** `.github/workflows/ci.yml`

```
Push/PR to main
    │
    ▼
┌─────────────────┐
│  Build & Test    │  JDK 21 + MySQL service container
│  mvn clean       │  → Uploads JAR artifact
│  package         │
└────────┬────────┘
         │ (push to main only)
         ▼
┌─────────────────┐
│  Docker Build    │  Downloads JAR → Builds image
│  & Push          │  → Pushes to Docker Hub (:latest + :sha)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Deploy to VPS   │  SSH → docker pull → stop old → run new
│                  │  → prune old images
└─────────────────┘
```

---

## 12. API Response Format

All endpoints return a standardized `ApiResponse<T>`:

```json
{
  "status": 200,
  "success": true,
  "message": "Success",
  "data": { ... },
  "timestamp": "2026-02-10T12:00:00"
}
```

Paginated responses use `PagedResponse<T>` or Spring's `Page<T>` as the data payload.

---

## 13. File Statistics

| Package         | Files    | Notes                            |
| --------------- | -------- | -------------------------------- |
| `entity`        | 34       | All JPA entities                 |
| `repository`    | 31       | Spring Data JPA repos            |
| `service`       | 30       | Interfaces                       |
| `service/impl`  | 30       | Implementations                  |
| `controller`    | 19       | 18 + 1 admin sub-package         |
| `dto/request`   | 22       | Grouped by domain                |
| `dto/response`  | 14       | Grouped by domain                |
| `enums`         | 15       | All status/type enums            |
| `exception`     | 9        | Custom exceptions + handler      |
| `config`        | 7        | App configuration                |
| `security`      | 4        | JWT + auth                       |
| `helpers`       | 4        | Utilities                        |
| `specification` | 3        | JPA Specifications for filtering |
| `constants`     | 2        | Shared constants                 |
| **Total**       | **~225** | Java source files                |
