# NeoNHS-BE Codebase Index

> Last updated: 2026-04-08
> Scope: Current repository state under `src/main`, `src/test`, `src/main/resources`, and top-level documentation folders.

## 1. Repository Overview

NeoNHS-BE is a Java 21 Spring Boot backend for tourism, events, workshops, tickets, blogs, vouchers, check-ins, and admin/vendor operations.

Top-level structure:

```text
NeoNHS-BE/
|- src/
|  |- main/java/fpt/project/NeoNHS/
|  |- main/resources/
|  |- test/java/fpt/project/NeoNHS/
|- docs/
|- pom.xml
|- Dockerfile
|- docker-compose.yaml
|- README.md
```

## 2. Stack Snapshot

- Java: 21
- Spring Boot parent: 4.x (from `pom.xml`)
- Spring modules in use: Web MVC, Data JPA, Security, Redis, Mail, Thymeleaf
- JWT: `jjwt-*`
- OpenAPI: `springdoc-openapi-starter-webmvc-ui`
- Database: MySQL (local via Docker Compose), MongoDB (for chat)
- Build: Maven Wrapper (`mvnw`, `mvnw.cmd`)

## 3. Source Inventory

High-level counts:

- Main Java files: 457
- Test Java files: 2
- Main resource files: 14

Core package counts (`src/main/java/fpt/project/NeoNHS`):

| Package | Files |
| --- | ---: |
| `config` | 15 |
| `constants` | 6 |
| `controller` | 32 |
| `controller/admin` | 18 |
| `controller/vendor` | 2 |
| `document` | 2 |
| `dto/chat` | 7 |
| `dto/request` | 69 |
| `dto/response` | 73 |
| `entity` | 40 |
| `enums` | 22 |
| `exception` | 11 |
| `helpers` | 7 |
| `repository` | 37 |
| `repository/mongo` | 2 |
| `repository/projection`| 1 |
| `security` | 4 |
| `service` | 47 |
| `service/impl` | 48 |
| `specification` | 12 |
| `tasks` | 1 |

## 4. Package Responsibilities

- `config`: Security, Redis, Cloudinary, mail, OpenAPI, startup/data initialization.
- `controller`: Public and authenticated REST APIs.
- `controller/admin`: Admin-only APIs.
- `controller/vendor`: Vendor-only APIs.
- `service` + `service/impl`: Business logic contracts and implementations.
- `repository`: Spring Data JPA repositories.
- `entity`: Persistence/domain model.
- `specification`: Dynamic JPA filter specifications.
- `dto/request`, `dto/response`: API payload contracts.
- `security`: JWT filter/provider and user details integration.
- `tasks`: Scheduled/background domain task(s).

## 5. API Surface Index

Primary controller groups by base path:

- Auth and identity: `/api/auth`, `/api/users`, `/api/vendors`
- Attractions/points/check-in: `/api/attractions`, `/api/points`, `/api/points/{pointId}/check-ins`, `/api/points/{pointId}/history-audios`, `/api/users/check-ins`
- Events/workshops/tags: `/api/events`, `/api/workshops`, `/api/tags`, `/api/wtags`, `/api/public/workshops`
- Commerce: `/api/cart`, `/api/orders`, `/api/tickets`, `/api/ticket-catalogs`, `/api/payment`, `/api/transactions`, `/api/vouchers`, `/api/v1/payouts`
- Content/community: `/api/blogs`, `/api/reviews`, `/api/reports`, `/api/notifications`, `/api/public/statistics`
- Admin namespace: `/api/admin/*` (users, vendors, attractions, events, points, tags, vouchers, reports, dashboard, revenue, etc.)
- Vendor namespace: `/api/vendor/vouchers`

Representative top-level controllers (`controller`):

- `AuthController`, `UserController`, `VendorProfileController`
- `AttractionController`, `PointController`, `CheckinPointController`, `HistoryAudioController`, `UserCheckinController`
- `EventController`, `WorkshopTemplateController`, `WorkshopSessionController`, `WorkshopTouristController`, `ETagController`, `WTagController`
- `CartController`, `OrderController`, `TicketController`, `TicketCatalogController`, `PaymentController`, `TransactionController`, `PayoutController`, `VoucherController`
- `BlogController`, `ReviewController`, `ReportController`, `NotificationController`, `StatisticsController`, `UploadController`
- `AdminVendorManagementController`

Admin controllers (`controller/admin`):

- `AdminAttractionController`, `AdminPointController`, `AdminCheckinPointController`, `AdminHistoryAudioController`, `AdminPanoramaController`
- `AdminEventController`, `AdminTicketCatalogController`, `AdminETagController`
- `AdminBlogController`, `AdminBlogCategoryController`
- `AdminUserController`, `AdminVoucherController`, `AdminReportController`
- `AdminDashboardController`, `AdminRevenueController`

## 6. Domain and Data Layer Index

Key domain aggregates represented in entities/repositories:

- Identity and access: `User`, `VendorProfile`
- Tourism map model: `Attraction`, `Point`, `CheckinPoint`, `PointHistoryAudio`, `PanoramaHotSpot`
- Event/workshop model: `Event`, `EventImage`, `ETag`, `EventTag`, `WorkshopTemplate`, `WorkshopSession`, `WorkshopImage`, `WTag`, `WorkshopTag`
- Commerce model: `TicketCatalog`, `Ticket`, `Cart`, `CartItem`, `Order`, `OrderDetail`, `Transaction`, `Voucher`, `UserVoucher`
- Community/content: `Blog`, `BlogCategory`, `Review`, `ReviewImage`, `Report`, `Notification`
- User activity: `UserCheckIn`, `UserVisitedPoint`

Repository layer includes 34 Spring Data interfaces, including custom query methods and paginated filtering patterns (plus native SQL where needed).

## 7. Resources and SQL Assets

Configuration/templates:

- `src/main/resources/application.yaml`
- `src/main/resources/templates/reset_password_email.html`
- `src/main/resources/templates/verification_email.html`

SQL and test-data resources (`src/main/resources/sql`):

- `add_rejected_by_column.sql`
- `add_test_vendors.sql`
- `fix_all_enum_columns.sql`
- `fix_status_column.sql`
- `migrate_workshop_template_review_fields.sql`
- `QUICK_TEST_REFERENCE.md`
- `README_TEST_DATA.md`
- `TEST_VENDORS_GUIDE.md`
- `test_vendors_postman.json`

## 8. Testing Snapshot

Current test source set is minimal (2 Java test files):

- `NeoNhsApplicationTests`
- `CloudinaryImageUploadServiceImplTest`

## 9. Notes

- This index is structural and inventory-focused; it does not validate runtime wiring or endpoint correctness.
- `target/` is intentionally excluded from source indexing because it is build output.
