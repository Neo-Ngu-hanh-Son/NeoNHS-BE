# Admin Workshop API Summary

> Complete reference for all Workshop Management APIs available to **Admin** role.  
> Base URL: `/api/admin/workshops`  
> Authentication: JWT Bearer Token (Role: `ADMIN`)  
> All `/api/admin/**` routes are protected by `SecurityConfig` — requires `ADMIN` role.

---

## Table of Contents

- [Overview](#overview)
- [Admin Responsibilities](#admin-responsibilities)
- [Status Enums](#status-enums)
- [Response Wrapper](#response-wrapper)
- [Workshop Template Admin APIs](#workshop-template-admin-apis)
  - [A1. Get All Workshop Templates](#a1-get-all-workshop-templates)
  - [A2. Get Workshop Template by ID](#a2-get-workshop-template-by-id)
  - [A3. Get Templates by Vendor ID](#a3-get-templates-by-vendor-id)
  - [A4. Filter / Search Workshop Templates](#a4-filter--search-workshop-templates)
  - [A5. Approve Workshop Template](#a5-approve-workshop-template)
  - [A6. Reject Workshop Template](#a6-reject-workshop-template)
- [Workshop Session Admin APIs](#workshop-session-admin-apis)
  - [A7. Get All Workshop Sessions](#a7-get-all-workshop-sessions)
  - [A8. Get Workshop Session by ID](#a8-get-workshop-session-by-id)
  - [A9. Filter / Search Workshop Sessions](#a9-filter--search-workshop-sessions)
- [Admin Review Workflow](#admin-review-workflow)
- [Quick Reference Table](#quick-reference-table)

---

## Overview

The Admin Workshop API provides **oversight and approval control** over the vendor-driven workshop ecosystem. Admins do not create or manage workshop content directly — that is the vendor's responsibility. The admin's role is limited to:

| Concern | Admin Action |
|---------|--------------|
| Template quality control | Review, approve, or reject submitted templates |
| Platform oversight | Browse all templates and sessions across all vendors |
| Rejection feedback | Provide written reasons so vendors can improve |

> **Note:** The service layer (`WorkshopTemplateServiceImpl`) fully implements `approveWorkshopTemplate` and `rejectWorkshopTemplate`. These must be exposed via a new `AdminWorkshopController` at `/api/admin/workshops/...`.

---

## Admin Responsibilities

```
Vendor submits template (PENDING)
  └─► Admin reviews content, pricing, images, tags
        ├─► Approve → status: ACTIVE
        │     └─► Vendor can now toggle-publish and create Sessions
        └─► Reject → status: REJECTED + adminNote written
              └─► Vendor edits and re-submits
```

---

## Status Enums

### WorkshopStatus (Template)

| Status | Description |
|--------|-------------|
| `DRAFT` | Vendor saved but not submitted. Not visible to admin review queue. |
| `PENDING` | Submitted and awaiting admin review. **Primary admin concern.** |
| `ACTIVE` | Approved by admin. Vendor may publish and create sessions. |
| `REJECTED` | Rejected by admin. Contains `adminNote` with reason. |

### SessionStatus (Session — read-only for admin)

| Status | Description |
|--------|-------------|
| `SCHEDULED` | Upcoming, bookable by tourists. |
| `ONGOING` | Currently in progress. |
| `COMPLETED` | Session has ended. |
| `CANCELLED` | Soft-deleted by vendor. |

---

## Response Wrapper

All API responses use the standard `ApiResponse<T>` wrapper:

```json
{
  "status": 200,
  "success": true,
  "message": "Human-readable message",
  "data": { ... },
  "timestamp": "2026-03-07T10:00:00"
}
```

---

## Workshop Template Admin APIs

### A1. Get All Workshop Templates

Retrieves all workshop templates across all vendors with pagination. Useful for platform-wide oversight or building the admin review queue.

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/admin/workshops/templates` |
| **Auth** | `ADMIN` role required |
| **Service** | `workshopTemplateService.getAllWorkshopTemplates(pageable)` |

#### Query Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | int | `0` | Page number (0-based) |
| `size` | int | `10` | Items per page |
| `sortBy` | String | `createdAt` | Field to sort by |
| `sortDir` | String | `desc` | Sort direction: `asc` or `desc` |

> **Tip:** To build a pending-review queue, use the filter endpoint (A4) with `status=PENDING`.

#### Response

`Page<WorkshopTemplateResponse>` — all templates in all statuses across all vendors.

#### Responses

| Code | Description |
|------|-------------|
| `200` | Templates retrieved successfully |
| `401` | Unauthorized — JWT token required |
| `403` | Forbidden — ADMIN role required |

---

### A2. Get Workshop Template by ID

Retrieves full details of a specific template. Used by admin when reviewing a submitted template before approving or rejecting.

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/admin/workshops/templates/{id}` |
| **Auth** | `ADMIN` role required |
| **Service** | `workshopTemplateService.getWorkshopTemplateById(id)` |

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | UUID | Workshop Template ID |

#### Response Body — `WorkshopTemplateResponse`

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Template ID |
| `name` | String | Template title |
| `shortDescription` | String | Brief summary |
| `fullDescription` | String | Detailed description |
| `estimatedDuration` | Integer | Duration in minutes |
| `defaultPrice` | BigDecimal | Base price |
| `minParticipants` | Integer | Minimum participants |
| `maxParticipants` | Integer | Maximum participants |
| `status` | WorkshopStatus | DRAFT / PENDING / ACTIVE / REJECTED |
| `isPublished` | Boolean | Whether visible in public catalog |
| `averageRating` | BigDecimal | Average rating |
| `totalRatings` | Integer | Total number of ratings |
| `vendorId` | UUID | Vendor ID |
| `vendorName` | String | Vendor display name |
| `createdAt` | LocalDateTime | Creation timestamp |
| `updatedAt` | LocalDateTime | Last update timestamp |
| `adminNote` | String | Admin rejection reason (null if approved) |
| `reviewedBy` | UUID | ID of admin who last reviewed |
| `reviewedAt` | LocalDateTime | Timestamp of last review |
| `images` | List\<WorkshopImageResponse\> | Associated images |
| `tags` | List\<WTagResponse\> | Associated tags/categories |

#### Responses

| Code | Description |
|------|-------------|
| `200` | Template retrieved successfully |
| `401` | Unauthorized |
| `403` | Forbidden — ADMIN role required |
| `404` | Template does not exist |

---

### A3. Get Templates by Vendor ID

Retrieves all workshop templates submitted by a specific vendor. Useful for vendor-level audits or reviewing a vendor's history.

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/admin/workshops/templates/vendor/{vendorId}` |
| **Auth** | `ADMIN` role required |
| **Service** | `workshopTemplateService.getWorkshopTemplatesByVendorId(vendorId, pageable)` |

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `vendorId` | UUID | Vendor Profile ID |

#### Query Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | int | `0` | Page number (0-based) |
| `size` | int | `10` | Items per page |
| `sortBy` | String | `createdAt` | Field to sort by |
| `sortDir` | String | `desc` | Sort direction |

#### Response

`Page<WorkshopTemplateResponse>` — all templates belonging to the specified vendor.

#### Responses

| Code | Description |
|------|-------------|
| `200` | Templates retrieved successfully |
| `401` | Unauthorized |
| `403` | Forbidden — ADMIN role required |
| `404` | Vendor profile does not exist |

---

### A4. Filter / Search Workshop Templates

Advanced filtering for templates across all vendors. The primary tool for building the admin review queue (`status=PENDING`).

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/admin/workshops/templates/filter` |
| **Auth** | `ADMIN` role required |
| **Service** | `workshopTemplateService.searchWorkshopTemplates(...)` |

#### Query Parameters (all optional)

| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| `keyword` | String | Search in name and descriptions | `yoga` |
| `name` | String | Filter by template name | `Beginner Yoga` |
| `status` | WorkshopStatus | Filter by status | `PENDING` |
| `vendorId` | UUID | Filter by vendor | — |
| `tagId` | UUID | Filter by tag/category | — |
| `minPrice` | BigDecimal | Minimum price | `50.00` |
| `maxPrice` | BigDecimal | Maximum price | `100.00` |
| `minDuration` | Integer | Minimum duration (minutes) | `60` |
| `maxDuration` | Integer | Maximum duration (minutes) | `120` |
| `minRating` | BigDecimal | Minimum average rating | `4.0` |

> **Primary use case:** `GET /api/admin/workshops/templates/filter?status=PENDING` — retrieves all templates awaiting admin review.

#### Response

`List<WorkshopTemplateResponse>` — filtered list of templates.

#### Responses

| Code | Description |
|------|-------------|
| `200` | Templates filtered successfully |
| `400` | Invalid filter parameters |
| `401` | Unauthorized |
| `403` | Forbidden — ADMIN role required |

---

### A5. Approve Workshop Template

Approves a `PENDING` workshop template. Status transitions to `ACTIVE`. The vendor can then toggle-publish and create sessions.

| | |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/admin/workshops/templates/{id}/approve` |
| **Auth** | `ADMIN` role required |
| **Body** | None |
| **Service** | `workshopTemplateService.approveWorkshopTemplate(adminEmail, id)` |

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | UUID | Workshop Template ID |

#### Pre-conditions

- Template must be in `PENDING` status.
- Template must have been submitted by a vendor via `/register`.

#### What Happens on Approval

| Field | New Value |
|-------|-----------|
| `status` | `ACTIVE` |
| `isPublished` | `false` (vendor must explicitly publish) |
| `reviewedBy` | Admin's user ID |
| `reviewedAt` | Current timestamp |
| `adminNote` | Cleared (`null`) |

> **Note:** Newly approved templates default to `isPublished = false`. The vendor must call `POST /api/workshops/templates/{id}/toggle-publish` to make sessions visible to tourists.

#### Responses

| Code | Description |
|------|-------------|
| `200` | Template approved successfully |
| `400` | Template is not in PENDING status |
| `401` | Unauthorized |
| `403` | Forbidden — ADMIN role required |
| `404` | Template does not exist |

---

### A6. Reject Workshop Template

Rejects a `PENDING` template with a required written reason. Status transitions to `REJECTED`. The vendor can read the `adminNote`, fix the issues, and re-submit.

| | |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/admin/workshops/templates/{id}/reject` |
| **Auth** | `ADMIN` role required |
| **Body** | `RejectWorkshopTemplateRequest` (JSON) |
| **Service** | `workshopTemplateService.rejectWorkshopTemplate(adminEmail, id, adminNote)` |

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | UUID | Workshop Template ID |

#### Request Body

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `adminNote` | String | ✅ | Rejection reason / feedback for vendor. Cannot be blank. |

#### Example Request

```json
{
  "adminNote": "Images are low quality and pricing is inconsistent with duration. Please provide professional photos and revise the price."
}
```

#### What Happens on Rejection

| Field | New Value |
|-------|-----------|
| `status` | `REJECTED` |
| `isPublished` | `false` |
| `adminNote` | Admin's written reason |
| `reviewedBy` | Admin's user ID |
| `reviewedAt` | Current timestamp |

> **Vendor next step:** Vendor reads `adminNote` via `GET /api/workshops/templates/{id}`, edits the template (`PUT`), and re-submits via `POST /api/workshops/templates/{id}/register`.

#### Responses

| Code | Description |
|------|-------------|
| `200` | Template rejected successfully |
| `400` | Template is not in PENDING status, or `adminNote` is blank |
| `401` | Unauthorized |
| `403` | Forbidden — ADMIN role required |
| `404` | Template does not exist |

---

## Workshop Session Admin APIs

Admin has **read-only** oversight of sessions. Vendors own session lifecycle management (create, update, cancel, delete).

---

### A7. Get All Workshop Sessions

Retrieves all workshop sessions across all vendors with pagination.

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/admin/workshops/sessions` |
| **Auth** | `ADMIN` role required |
| **Service** | `workshopSessionService.getAllUpcomingSessions(pageable)` |

#### Query Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | int | `0` | Page number (0-based) |
| `size` | int | `10` | Items per page |
| `sortBy` | String | `startTime` | Field to sort by |
| `sortDir` | String | `asc` | Sort direction |

#### Response

`Page<WorkshopSessionResponse>` — upcoming SCHEDULED sessions from published templates.

#### Responses

| Code | Description |
|------|-------------|
| `200` | Sessions retrieved successfully |
| `401` | Unauthorized |
| `403` | Forbidden — ADMIN role required |

---

### A8. Get Workshop Session by ID

Retrieves full details of a specific session.

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/admin/workshops/sessions/{id}` |
| **Auth** | `ADMIN` role required |
| **Service** | `workshopSessionService.getWorkshopSessionById(id)` |

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | UUID | Workshop Session ID |

#### Response Body — `WorkshopSessionResponse`

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Session ID |
| `startTime` | LocalDateTime | Session start time |
| `endTime` | LocalDateTime | Session end time |
| `price` | BigDecimal | Session price |
| `maxParticipants` | Integer | Maximum attendees |
| `currentEnrolled` | Integer | Current enrollment count |
| `availableSlots` | Integer | `maxParticipants - currentEnrolled` |
| `status` | SessionStatus | SCHEDULED / ONGOING / COMPLETED / CANCELLED |
| `createdAt` | LocalDateTime | Creation timestamp |
| `updatedAt` | LocalDateTime | Last update timestamp |
| `workshopTemplateId` | UUID | Parent template ID |
| `name` | String | Template name |
| `shortDescription` | String | Template brief summary |
| `vendorId` | UUID | Vendor ID |
| `vendorName` | String | Vendor display name |
| `images` | List\<WorkshopImageResponse\> | Template images |
| `tags` | List\<WTagResponse\> | Template tags |

#### Responses

| Code | Description |
|------|-------------|
| `200` | Session retrieved successfully |
| `401` | Unauthorized |
| `403` | Forbidden — ADMIN role required |
| `404` | Session does not exist |

---

### A9. Filter / Search Workshop Sessions

Advanced filtering for sessions across all vendors.

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/admin/workshops/sessions/filter` |
| **Auth** | `ADMIN` role required |
| **Service** | `workshopSessionService.searchWorkshopSessions(...)` |

#### Query Parameters (all optional)

| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| `keyword` | String | Search in name and descriptions | `yoga` |
| `vendorId` | UUID | Filter by vendor | — |
| `tagId` | UUID | Filter by tag/category | — |
| `status` | SessionStatus | Filter by status | `SCHEDULED` |
| `startDate` | LocalDateTime | Sessions starting after this date | `2026-04-01T00:00:00` |
| `endDate` | LocalDateTime | Sessions starting before this date | `2026-04-07T23:59:59` |
| `minPrice` | BigDecimal | Minimum price | `50.00` |
| `maxPrice` | BigDecimal | Maximum price | `100.00` |
| `availableOnly` | Boolean | Only sessions with open slots | `true` |

#### Pagination Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | int | `0` | Page number (0-based) |
| `size` | int | `10` | Items per page |
| `sortBy` | String | `startTime` | Field to sort by |
| `sortDir` | String | `asc` | Sort direction |

#### Response

`Page<WorkshopSessionResponse>` — paginated filtered results.

#### Responses

| Code | Description |
|------|-------------|
| `200` | Sessions filtered successfully |
| `400` | Invalid filter parameters |
| `401` | Unauthorized |
| `403` | Forbidden — ADMIN role required |

---

## Admin Review Workflow

### Template Review Queue

```
1. GET /api/admin/workshops/templates/filter?status=PENDING
   └─► Admin sees all templates awaiting review

2. GET /api/admin/workshops/templates/{id}
   └─► Admin opens a specific template to inspect:
         - Content quality (name, descriptions, images, tags)
         - Pricing reasonableness
         - Participant limits

3a. POST /api/admin/workshops/templates/{id}/approve   ← Template meets standards
    └─► status: PENDING → ACTIVE
    └─► Vendor notified (out of scope) to publish and create sessions

3b. POST /api/admin/workshops/templates/{id}/reject    ← Template needs improvement
    Body: { "adminNote": "Reason for rejection..." }
    └─► status: PENDING → REJECTED
    └─► Vendor reads adminNote, fixes issues, and re-submits
```

### Template State Transitions (Admin perspective)

```
                   Admin Approves
PENDING  ─────────────────────────────►  ACTIVE
   │                                        │
   │  Admin Rejects                         │ Vendor toggles publish
   ▼                                        ▼
REJECTED ◄──── Vendor edits & re-submits   isPublished: true
```

---

## Quick Reference Table

### Admin APIs (`/api/admin/workshops/...`)

> ⚠️ All `/api/admin/**` routes require `ADMIN` role (enforced by `SecurityConfig`).

| # | Method | Endpoint | Description |
|---|--------|----------|-------------|
| A1 | `GET` | `/api/admin/workshops/templates` | Get all templates, all statuses (paginated) |
| A2 | `GET` | `/api/admin/workshops/templates/{id}` | Get template by ID (any status) |
| A3 | `GET` | `/api/admin/workshops/templates/vendor/{vendorId}` | Get templates by vendor (paginated) |
| A4 | `GET` | `/api/admin/workshops/templates/filter` | Search/filter templates (use `status=PENDING` for review queue) |
| A5 | `POST` | `/api/admin/workshops/templates/{id}/approve` | Approve PENDING template → ACTIVE |
| A6 | `POST` | `/api/admin/workshops/templates/{id}/reject` | Reject PENDING template → REJECTED + adminNote |
| A7 | `GET` | `/api/admin/workshops/sessions` | Get all upcoming sessions (paginated) |
| A8 | `GET` | `/api/admin/workshops/sessions/{id}` | Get session by ID |
| A9 | `GET` | `/api/admin/workshops/sessions/filter` | Search/filter sessions |

### Implementation Status

| # | Service Method | Controller Endpoint | Status |
|---|---------------|---------------------|--------|
| A1 | `getAllWorkshopTemplates(pageable)` | `GET /api/admin/workshops/templates` | ⚠️ Needs controller |
| A2 | `getWorkshopTemplateById(id)` | `GET /api/admin/workshops/templates/{id}` | ⚠️ Needs controller |
| A3 | `getWorkshopTemplatesByVendorId(vendorId, pageable)` | `GET /api/admin/workshops/templates/vendor/{vendorId}` | ⚠️ Needs controller |
| A4 | `searchWorkshopTemplates(...)` | `GET /api/admin/workshops/templates/filter` | ⚠️ Needs controller |
| A5 | `approveWorkshopTemplate(adminEmail, id)` | `POST /api/admin/workshops/templates/{id}/approve` | ⚠️ Needs controller |
| A6 | `rejectWorkshopTemplate(adminEmail, id, note)` | `POST /api/admin/workshops/templates/{id}/reject` | ⚠️ Needs controller |
| A7 | `getAllUpcomingSessions(pageable)` | `GET /api/admin/workshops/sessions` | ⚠️ Needs controller |
| A8 | `getWorkshopSessionById(id)` | `GET /api/admin/workshops/sessions/{id}` | ⚠️ Needs controller |
| A9 | `searchWorkshopSessions(...)` | `GET /api/admin/workshops/sessions/filter` | ⚠️ Needs controller |

> **All service methods are fully implemented** in `WorkshopTemplateServiceImpl` and `WorkshopSessionServiceImpl`. Only the `AdminWorkshopController` at `/api/admin/workshops/...` needs to be created to expose these endpoints.
