# NeoNHS-BE Codebase Index

> **Last Updated:** 2026-01-28  
> **Project:** Neo-NHS Backend API  
> **Description:** RESTful API for the Neo-Ngu Hanh Son Tourism Application

---

## 📋 Table of Contents

- [Overview](#overview)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Domain Model](#domain-model)
- [API Endpoints](#api-endpoints)
- [Configurations](#configurations)
- [Key Design Patterns](#key-design-patterns)

---

## 🎯 Overview

**NeoNHS-BE** is a Spring Boot 4.0 backend application serving the Neo-NHS tourism mobile application. The system provides:

- **User Authentication** - Email/password + Google OAuth login
- **Tourism Management** - Attractions, Points of Interest, Check-ins
- **E-Commerce** - Tickets, Workshops, Orders, Cart, Vouchers
- **Content Management** - Blogs, Events, Reviews
- **Notifications** - User notification system
- **Vendor System** - Vendor profiles for workshop management

---

## 🛠 Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 21 | Core language |
| **Spring Boot** | 4.0.1 | Application framework |
| **Spring Data JPA** | - | ORM / Data access |
| **Spring Security** | - | Authentication & Authorization |
| **MySQL** | 8.0 | Primary database |
| **Redis** | - | Caching / Session storage |
| **JWT (jjwt)** | 0.12.6 | Token-based authentication |
| **SpringDoc OpenAPI** | 2.8.4 | API documentation (Swagger) |
| **Thymeleaf** | - | Email templates |
| **Lombok** | - | Boilerplate reduction |
| **Docker** | - | Containerization |

---

## 📁 Project Structure

```
NeoNHS-BE/
├── .github/                    # GitHub Actions CI/CD
├── prompts/                    # Documentation prompts
├── src/
│   ├── main/
│   │   ├── java/fpt/project/NeoNHS/
│   │   │   ├── NeoNhsApplication.java      # Main entry point
│   │   │   ├── config/                     # Configuration classes (6 files)
│   │   │   ├── constants/                  # Application constants (1 file)
│   │   │   ├── controller/                 # REST Controllers (17 files)
│   │   │   ├── dto/                        # Data Transfer Objects
│   │   │   │   ├── request/                # Request DTOs
│   │   │   │   │   └── auth/               # Auth request DTOs (3 files)
│   │   │   │   └── response/               # Response DTOs (2 files + 1 dir)
│   │   │   ├── entity/                     # JPA Entities (33 files)
│   │   │   ├── enums/                      # Enum types (13 files)
│   │   │   ├── exception/                  # Custom exceptions (6 files)
│   │   │   ├── helpers/                    # Utility helpers (3 files)
│   │   │   ├── repository/                 # JPA Repositories (31 files)
│   │   │   ├── security/                   # Security components (4 files)
│   │   │   └── service/                    # Business logic
│   │   │       ├── *.java                  # Service interfaces (30 files)
│   │   │       └── impl/                   # Service implementations (30 files)
│   │   └── resources/
│   │       ├── application.yaml            # Application configuration
│   │       └── templates/                  # Email templates (5 files)
│   └── test/                               # Test classes
├── pom.xml                                 # Maven dependencies
├── Dockerfile                              # Docker configuration
├── docker-compose.yaml                     # Docker Compose setup
└── README.md                               # Project documentation
```

---

## 📊 Domain Model

### Core Entities

#### User Management
| Entity | Description | Key Fields |
|--------|-------------|------------|
| **User** | System users | id, fullname, email, passwordHash, role, isActive, isVerified, isBanned |
| **VendorProfile** | Vendor-specific profile | One-to-One with User |
| **UserRole** | Enum: USER, VENDOR, ADMIN | - |

#### Tourism & Check-in
| Entity | Description | Key Fields |
|--------|-------------|------------|
| **Attraction** | Tourist attractions | id, name, description, address, latitude, longitude, openHour, closeHour |
| **Point** | Points of interest within attractions | id, name, description, history, historyAudioUrl, orderIndex |
| **CheckinPoint** | QR code check-in locations | id, name, qrCode, latitude, longitude, rewardPoints |
| **UserCheckIn** | User check-in records | User ↔ CheckinPoint |
| **UserVisitedPoint** | Visited points tracking | User ↔ Point |
| **CheckinImage** | Images for check-in points | - |

#### Events & Workshops
| Entity | Description | Key Fields |
|--------|-------------|------------|
| **Event** | Tourism events | id, name, startTime, endTime, isTicketRequired, price, maxParticipants, status |
| **EventTag** | Event tagging (M:N) | Event ↔ ETag |
| **ETag** | Event tag definitions | - |
| **WorkshopTemplate** | Workshop definitions | id, name, estimatedDuration, defaultPrice, min/maxParticipants, status |
| **WorkshopSession** | Workshop instances/sessions | WorkshopTemplate relationship |
| **WorkshopImage** | Workshop images | - |
| **WorkshopTag** | Workshop tagging (M:N) | WorkshopTemplate ↔ WTag |
| **WTag** | Workshop tag definitions | - |

#### E-Commerce
| Entity | Description | Key Fields |
|--------|-------------|------------|
| **Ticket** | Individual tickets | id, qrCode, ticketCode, ticketType, status, issueDate, expiryDate |
| **TicketCatalog** | Ticket types/templates | Linked to Attraction or Event |
| **Order** | User orders | id, totalAmount, discountAmount, finalAmount |
| **OrderDetail** | Order line items | Order ↔ Ticket |
| **Cart** | User shopping cart | One-to-One with User |
| **CartItem** | Cart line items | - |
| **Transaction** | Payment transactions | Order relationship |
| **Voucher** | Discount vouchers | id, code, discountType, discountValue, usageLimit |
| **UserVoucher** | User's claimed vouchers | User ↔ Voucher |

#### Content & Reviews
| Entity | Description | Key Fields |
|--------|-------------|------------|
| **Blog** | Blog posts | id, title, slug, content, status, viewCount, isFeatured |
| **BlogCategory** | Blog categories | - |
| **Review** | User reviews | id, rating, comment, status |
| **ReviewImage** | Review images | - |
| **Report** | User reports | reporter (User), status |
| **Notification** | User notifications | User relationship |

### Entity Relationships Diagram (Simplified)

```
                           ┌────────────────┐
                           │     User       │
                           └───────┬────────┘
                                   │
        ┌──────────────────────────┼──────────────────────────┐
        │           │              │              │           │
        ▼           ▼              ▼              ▼           ▼
  ┌──────────┐ ┌────────┐    ┌─────────┐   ┌─────────┐  ┌──────────┐
  │VendorProf│ │  Cart  │    │  Order  │   │UserCheck│  │  Review  │
  └────┬─────┘ └────────┘    └────┬────┘   │   In    │  └──────────┘
       │                          │        └─────────┘
       ▼                          ▼
  ┌──────────┐              ┌───────────┐
  │Workshop  │              │OrderDetail│
  │Template  │              └─────┬─────┘
  └────┬─────┘                    │
       │                          ▼
       ▼                     ┌─────────┐
  ┌──────────┐               │ Ticket  │
  │Workshop  │               └────┬────┘
  │ Session  │                    │
  └──────────┘                    ▼
                            ┌───────────┐
  ┌────────────┐            │  Ticket   │
  │ Attraction │◄───────────│  Catalog  │
  └──────┬─────┘            └───────────┘
         │
         ▼
    ┌─────────┐
    │  Point  │
    └────┬────┘
         │
         ▼
    ┌──────────┐
    │CheckinPt │
    └──────────┘
```

---

## 🔌 API Endpoints

### Controllers Overview

| Controller | Base Path | Description |
|------------|-----------|-------------|
| **AuthController** | `/api/auth` | Authentication (login, register, Google OAuth) |
| **UserController** | `/api/users` | User management |
| **AttractionController** | `/api/attractions` | Tourist attractions |
| **EventController** | `/api/events` | Events management |
| **WorkshopController** | `/api/workshops` | Workshop management |
| **TicketController** | `/api/tickets` | Ticket management |
| **OrderController** | `/api/orders` | Order processing |
| **CartController** | `/api/cart` | Shopping cart |
| **CheckinController** | `/api/checkins` | Check-in operations |
| **BlogController** | `/api/blogs` | Blog posts |
| **ReviewController** | `/api/reviews` | Reviews |
| **VoucherController** | `/api/vouchers` | Voucher management |
| **PointController** | `/api/points` | Points of interest |
| **NotificationController** | `/api/notifications` | User notifications |
| **ReportController** | `/api/reports` | Content reporting |
| **TransactionController** | `/api/transactions` | Payment transactions |
| **VendorProfileController** | `/api/vendors` | Vendor profiles |

### Auth Endpoints (Detail)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | Email/password login |
| POST | `/api/auth/register` | User registration |
| POST | `/api/auth/google-login` | Google OAuth login |
| POST | `/api/auth/logout` | User logout |
| GET | `/api/auth/ping` | Health check |
| GET | `/api/auth/test-email` | Test email sending |

### API Response Format

All endpoints return a standardized `ApiResponse<T>` wrapper:

```json
{
  "status": 200,
  "success": true,
  "message": "Success message",
  "data": { ... },
  "timestamp": "2026-01-28T10:00:00"
}
```

---

## ⚙️ Configurations

### Configuration Classes

| Class | Purpose |
|-------|---------|
| **SecurityConfig** | Spring Security setup, JWT filter, CORS, endpoint security |
| **OpenApiConfig** | Swagger/OpenAPI documentation configuration |
| **RedisConfig** | Redis connection and serialization settings |
| **EmailConfiguration** | SMTP mail sender configuration |
| **DataInitializer** | Initial data seeding on startup |
| **StartupLogger** | Application startup logging |

### Security Configuration

- **Authentication**: JWT-based stateless authentication
- **Password Encoding**: BCrypt
- **Public Endpoints**:
  - `/api/auth/**` - Authentication routes
  - `/api/public/**` - Public content
  - `/swagger-ui/**`, `/v3/api-docs/**` - API documentation

### Environment Variables

| Variable | Description |
|----------|-------------|
| `REDIS_HOST` | Redis server hostname |
| `REDIS_USERNAME` | Redis authentication username |
| `REDIS_PASSWORD` | Redis authentication password |
| `GOOGLE_MAIL_PASSWORD` | Gmail app password for SMTP |
| `GOOGLE_AUTH_CLIENT_ID` | Google OAuth client ID |
| `GOOGLE_AUTH_CLIENT_SECRET` | Google OAuth client secret |

---

## 🎨 Key Design Patterns

### Architecture Pattern
- **Layered Architecture**: Controller → Service → Repository → Entity

### Service Layer Pattern
```
Service (Interface)
    └── ServiceImpl (Implementation)
           └── Repository (Data Access)
```

### DTO Pattern
- **Request DTOs**: `dto/request/` - Incoming data validation
- **Response DTOs**: `dto/response/` - Outgoing data formatting

### Security Pattern
- **JWT Authentication Filter**: `JwtAuthenticationFilter`
- **Custom User Details**: `CustomUserDetailsService`, `UserPrincipal`
- **Token Provider**: `JwtTokenProvider`

### Exception Handling
- **GlobalExceptionHandler**: Centralized exception handling with `@ControllerAdvice`
- **Custom Exceptions**:
  - `BadRequestException`
  - `UnauthorizedException`
  - `ResourceNotFoundException`
  - `RequestGoogleAccountException`
  - `EmailException`

---

## 📧 Email Templates

| Template | Purpose |
|----------|---------|
| `verification_email.html` | Email verification on registration |
| `reset_password_email.html` | Password reset flow |
| `appointment_reminder_email.html` | Appointment reminders |
| `lecture_reminder_email.html` | Lecture/workshop reminders |
| `certificate.html` | Certificate generation |

---

## 🚀 Getting Started

### Prerequisites
- JDK 21+
- Maven 3.9+
- Docker & Docker Compose

### Quick Start

```bash
# 1. Clone repository
git clone <repository-url>
cd NeoNHS-BE

# 2. Start MySQL with Docker
docker-compose up -d

# 3. Configure environment (.env file)
cp .env.example .env
# Edit .env with your values

# 4. Run application
./mvnw spring-boot:run
```

### Access Points
- **API**: `http://localhost:8080`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **API Docs**: `http://localhost:8080/v3/api-docs`

---

## 📈 Statistics

| Category | Count |
|----------|-------|
| Entities | 33 |
| Controllers | 17 |
| Services | 30 |
| Repositories | 31 |
| Enums | 13 |
| Email Templates | 5 |

---

*FPT University Capstone Project © 2026*
