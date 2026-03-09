# Vendor Workshop API Summary

> Complete reference for all Workshop APIs available to **Vendor** role.  
> Base URL: `/api/workshops`  
> Authentication: JWT Bearer Token (Role: `VENDOR`)

---

## Table of Contents

- [Overview](#overview)
- [Status Enums](#status-enums)
- [Response Wrapper](#response-wrapper)
- [Workshop Template APIs](#workshop-template-apis)
  - [1. Create Workshop Template](#1-create-workshop-template)
  - [2. Get Workshop Template by ID](#2-get-workshop-template-by-id)
  - [3. Get My Workshop Templates (Paginated)](#3-get-my-workshop-templates-paginated)
  - [4. Filter / Search Workshop Templates](#4-filter--search-workshop-templates)
  - [5. Update Workshop Template](#5-update-workshop-template)
  - [6. Submit Template for Approval](#6-submit-template-for-approval)
  - [7. Toggle Publish Status](#7-toggle-publish-status)
  - [8. Delete Workshop Template](#8-delete-workshop-template)
- [Workshop Session APIs](#workshop-session-apis)
  - [9. Create Workshop Session](#9-create-workshop-session)
  - [10. Get Workshop Session by ID](#10-get-workshop-session-by-id)
  - [11. Get All Upcoming Sessions](#11-get-all-upcoming-sessions)
  - [12. Get My Workshop Sessions (Paginated)](#12-get-my-workshop-sessions-paginated)
  - [13. Get Sessions by Template ID](#13-get-sessions-by-template-id)
  - [14. Filter / Search Workshop Sessions](#14-filter--search-workshop-sessions)
  - [15. Update Workshop Session](#15-update-workshop-session)
  - [16. Delete Workshop Session](#16-delete-workshop-session)
  - [17. Cancel Workshop Session](#17-cancel-workshop-session)
- [Vendor Workflow](#vendor-workflow)
- [Quick Reference Table](#quick-reference-table)

---

## Overview

The Workshop system follows a **Template → Session** architecture:

| Concept | Description |
|---------|-------------|
| **Workshop Template** | The blueprint/design of a workshop. Requires **admin approval** before it becomes active. |
| **Workshop Session** | A scheduled, bookable instance created from an ACTIVE template. **No admin approval** needed. |

### Workflow Summary

```
Vendor creates Template (DRAFT)
  → Vendor submits for approval (PENDING)
    → Admin approves (ACTIVE) or rejects (REJECTED)
      → If ACTIVE: Vendor toggles publish, then creates Sessions (SCHEDULED)
      → If REJECTED: Vendor edits and resubmits
```

---

## Status Enums

### WorkshopStatus (Template)

| Status | Description |
|--------|-------------|
| `DRAFT` | Template saved but not submitted for approval. Editable. |
| `PENDING` | Submitted and awaiting admin approval. **Locked** — cannot edit. |
| `ACTIVE` | Approved by admin. Can create sessions from it. |
| `REJECTED` | Rejected by admin. Editable — can fix issues and resubmit. |

### SessionStatus (Session)

| Status | Description |
|--------|-------------|
| `SCHEDULED` | Upcoming session, available for booking. Editable. |
| `ONGOING` | Session currently in progress. |
| `COMPLETED` | Session has ended. |
| `CANCELLED` | Session was cancelled (soft delete). |

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

## Workshop Template APIs

### 1. Create Workshop Template

Creates a new workshop template in `DRAFT` status.

| | |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/workshops/templates` |
| **Auth** | `VENDOR` role required |
| **Body** | `CreateWorkshopTemplateRequest` (JSON) |

#### Request Body

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| `name` | String | ✅ | Max 255 chars | Template title |
| `shortDescription` | String | ❌ | Max 500 chars | Brief summary |
| `fullDescription` | String | ❌ | — | Detailed description |
| `defaultPrice` | BigDecimal | ❌ | > 0 | Base price |
| `estimatedDuration` | Integer | ❌ | > 0 | Duration in minutes |
| `minParticipants` | Integer | ❌ | > 0 | Minimum participants |
| `maxParticipants` | Integer | ❌ | > 0 | Maximum participants |
| `imageUrls` | List\<String\> | ❌ | — | Image URLs |
| `thumbnailIndex` | Integer | ❌ | ≥ 0, default 0 | Index of thumbnail image |
| `tagIds` | List\<UUID\> | ✅ | Min 1 item | Category/tag IDs |

#### Example Request

```json
{
  "name": "Beginner Yoga Workshop",
  "shortDescription": "A gentle introduction to yoga",
  "fullDescription": "This workshop covers basic yoga poses...",
  "defaultPrice": 50.00,
  "estimatedDuration": 90,
  "minParticipants": 5,
  "maxParticipants": 20,
  "imageUrls": [
    "https://example.com/yoga1.jpg",
    "https://example.com/yoga2.jpg"
  ],
  "thumbnailIndex": 0,
  "tagIds": ["uuid-tag-1", "uuid-tag-2"]
}
```

#### Responses

| Code | Description |
|------|-------------|
| `201` | Template created successfully |
| `400` | Validation failed or vendor not verified |
| `401` | Unauthorized — JWT token required |
| `403` | Forbidden — VENDOR role required |
| `404` | Vendor profile or tags not found |

---

### 2. Get Workshop Template by ID

Retrieves detailed information about a specific workshop template.

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/workshops/templates/{id}` |
| **Auth** | 🔐 **Authenticated** — requires valid JWT token |

> ⚠️ **Note:** This is NOT a public endpoint. It requires authentication because `/api/workshops/**` is not in the public routes list. For the **public (tourist)** version, use `GET /api/public/workshops/templates/{id}` instead (only returns ACTIVE templates).

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
| `adminNote` | String | Admin review note (rejection reason) |
| `reviewedBy` | UUID | Admin who reviewed |
| `reviewedAt` | LocalDateTime | Review timestamp |
| `images` | List\<WorkshopImageResponse\> | Associated images |
| `tags` | List\<WTagResponse\> | Associated tags/categories |

#### Responses

| Code | Description |
|------|-------------|
| `200` | Template retrieved successfully |
| `401` | Unauthorized — JWT token required |
| `404` | Template does not exist |

---

### 3. Get My Workshop Templates (Paginated)

Retrieves all templates created by the authenticated vendor with pagination.

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/workshops/templates/my` |
| **Auth** | `VENDOR` role required |

#### Query Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | int | `0` | Page number (0-based) |
| `size` | int | `10` | Items per page |
| `sortBy` | String | `createdAt` | Field to sort by |
| `sortDir` | String | `desc` | Sort direction: `asc` or `desc` |

#### Response

`Page<WorkshopTemplateResponse>` — paginated list of templates in all statuses.

#### Responses

| Code | Description |
|------|-------------|
| `200` | Templates retrieved successfully |
| `401` | Unauthorized |
| `403` | Forbidden — VENDOR role required |
| `404` | Vendor profile not found |

---

### 4. Filter / Search Workshop Templates

Advanced search and filtering for templates. **⚠️ Requires authentication** (not a public endpoint).

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/workshops/templates/filter` |
| **Auth** | 🔐 **Authenticated** — requires valid JWT token |

> ⚠️ **Note:** Despite having no `@PreAuthorize` annotation, this endpoint is NOT public because `/api/workshops/**` requires authentication per SecurityConfig. For a **public (tourist)** search, use `GET /api/public/workshops/templates/search` instead.

#### Query Parameters (all optional)

| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| `keyword` | String | Search in name and descriptions | `yoga` |
| `name` | String | Filter by template name | `Beginner Yoga` |
| `status` | WorkshopStatus | Filter by status | `ACTIVE` |
| `vendorId` | UUID | Filter by vendor | — |
| `tagId` | UUID | Filter by tag/category | — |
| `minPrice` | BigDecimal | Minimum price | `50.00` |
| `maxPrice` | BigDecimal | Maximum price | `100.00` |
| `minDuration` | Integer | Minimum duration (minutes) | `60` |
| `maxDuration` | Integer | Maximum duration (minutes) | `120` |
| `minRating` | BigDecimal | Minimum average rating | `4.0` |

#### Response

`List<WorkshopTemplateResponse>` — filtered list of templates.

#### Responses

| Code | Description |
|------|-------------|
| `200` | Templates filtered successfully |
| `400` | Invalid filter parameters |
| `401` | Unauthorized — JWT token required |

---

### 5. Update Workshop Template

Updates an existing template. Only `DRAFT` or `REJECTED` templates can be updated.

| | |
|---|---|
| **Method** | `PUT` |
| **URL** | `/api/workshops/templates/{id}` |
| **Auth** | `VENDOR` role required + must be template owner |
| **Body** | `UpdateWorkshopTemplateRequest` (JSON) |

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | UUID | Workshop Template ID |

#### Request Body

All fields are **optional** — only send fields you want to update.

| Field | Type | Validation | Description |
|-------|------|------------|-------------|
| `name` | String | Max 255 chars | Template title |
| `shortDescription` | String | Max 500 chars | Brief summary |
| `fullDescription` | String | — | Detailed description |
| `defaultPrice` | BigDecimal | > 0 | Base price |
| `estimatedDuration` | Integer | > 0 | Duration in minutes |
| `minParticipants` | Integer | > 0 | Minimum participants |
| `maxParticipants` | Integer | > 0 | Maximum participants |
| `imageUrls` | List\<String\> | — | Image URLs |
| `thumbnailIndex` | Integer | ≥ 0 | Index of thumbnail image |
| `tagIds` | List\<UUID\> | Min 1 item | Category/tag IDs |

#### Editable Statuses

| Status | Can Edit? |
|--------|-----------|
| `DRAFT` | ✅ Freely editable |
| `REJECTED` | ✅ Fix rejection issues |
| `PENDING` | ❌ Locked — under admin review |
| `ACTIVE` | ❌ Locked — template is live |

#### Responses

| Code | Description |
|------|-------------|
| `200` | Template updated successfully |
| `400` | Validation failed or wrong status |
| `401` | Unauthorized |
| `403` | Forbidden — not template owner |
| `404` | Template does not exist |

---

### 6. Submit Template for Approval

Submits a `DRAFT` or `REJECTED` template for admin review. Status changes to `PENDING`.

| | |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/workshops/templates/{id}/register` |
| **Auth** | `VENDOR` role required + must be template owner |

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | UUID | Workshop Template ID |

#### Pre-conditions

Template must have all mandatory fields completed:
- Name, Short Description, Full Description
- Price, Duration
- Min/Max Participants
- At least one image
- At least one tag

#### After Submission

- Status changes from `DRAFT` / `REJECTED` → `PENDING`
- Template is **locked** for editing
- Admin will approve → `ACTIVE` or reject → `REJECTED`

#### Responses

| Code | Description |
|------|-------------|
| `200` | Template submitted successfully |
| `400` | Incomplete template, wrong status, or validation failed |
| `401` | Unauthorized |
| `403` | Forbidden — not template owner |
| `404` | Template does not exist |

---

### 7. Toggle Publish Status

Toggles the `isPublished` flag of an `ACTIVE` template.

| | |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/workshops/templates/{id}/toggle-publish` |
| **Auth** | `VENDOR` role required + must be template owner |

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | UUID | Workshop Template ID |

#### Behavior

| Current State | → New State |
|---------------|-------------|
| `isPublished = true` | `isPublished = false` (hidden from tourists) |
| `isPublished = false` | `isPublished = true` (visible in public catalog) |

> **Note:** Newly approved templates default to `isPublished = false`. Vendor must explicitly publish after admin approval.

#### Responses

| Code | Description |
|------|-------------|
| `200` | Publish status toggled successfully |
| `400` | Template is not ACTIVE |
| `401` | Unauthorized |
| `403` | Forbidden — not template owner |
| `404` | Template does not exist |

---

### 8. Delete Workshop Template

Permanently deletes a template. Only non-`ACTIVE` templates can be deleted.

| | |
|---|---|
| **Method** | `DELETE` |
| **URL** | `/api/workshops/templates/{id}` |
| **Auth** | `VENDOR` role required + must be template owner |

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | UUID | Workshop Template ID |

#### Deletable Statuses

| Status | Can Delete? |
|--------|-------------|
| `DRAFT` | ✅ |
| `PENDING` | ✅ (withdraw) |
| `REJECTED` | ✅ |
| `ACTIVE` | ❌ Has associated sessions |

#### What Gets Deleted

- Workshop template record
- All associated images (cascade)
- All tag associations (cascade)

> ⚠️ **This action is permanent and cannot be undone.**

#### Responses

| Code | Description |
|------|-------------|
| `200` | Template deleted successfully |
| `400` | Cannot delete ACTIVE template |
| `401` | Unauthorized |
| `403` | Forbidden — not template owner |
| `404` | Template does not exist |

---

## Workshop Session APIs

### 9. Create Workshop Session

Creates a new scheduled session from an `ACTIVE` template. **No admin approval required.**

| | |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/workshops/sessions` |
| **Auth** | `VENDOR` role required + must own the template |
| **Body** | `CreateWorkshopSessionRequest` (JSON) |

#### Request Body

| Field | Type | Required | Validation | Description |
|-------|------|----------|------------|-------------|
| `workshopTemplateId` | UUID | ✅ | Must be an ACTIVE template | Template to create session from |
| `startTime` | LocalDateTime | ✅ | Must be in the future | Session start time |
| `endTime` | LocalDateTime | ✅ | Must be after startTime | Session end time |
| `price` | BigDecimal | ❌ | > 0, defaults to template's defaultPrice | Session price |
| `maxParticipants` | Integer | ❌ | > 0, defaults to template's maxParticipants | Max attendees |

#### Example Request

```json
{
  "workshopTemplateId": "uuid-template-id",
  "startTime": "2026-04-01T09:00:00",
  "endTime": "2026-04-01T10:30:00",
  "price": 55.00,
  "maxParticipants": 15
}
```

#### After Creation

- Status is set to `SCHEDULED`
- Immediately available for users to book

#### Responses

| Code | Description |
|------|-------------|
| `201` | Session created successfully |
| `400` | Validation failed or template not ACTIVE |
| `401` | Unauthorized |
| `403` | Forbidden — not template owner |
| `404` | Template does not exist |

---

### 10. Get Workshop Session by ID

Retrieves detailed information about a specific session.

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/workshops/sessions/{id}` |
| **Auth** | 🔐 **Authenticated** — requires valid JWT token |

> ⚠️ **Note:** This is NOT a public endpoint. `/api/workshops/**` requires authentication per SecurityConfig.

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
| `availableSlots` | Integer | Remaining spots |
| `status` | SessionStatus | SCHEDULED / ONGOING / COMPLETED / CANCELLED |
| `createdAt` | LocalDateTime | Creation timestamp |
| `updatedAt` | LocalDateTime | Last update timestamp |
| `workshopTemplateId` | UUID | Parent template ID |
| `name` | String | Template name |
| `shortDescription` | String | Template brief summary |
| `fullDescription` | String | Template detailed description |
| `estimatedDuration` | Integer | Template estimated duration |
| `averageRating` | BigDecimal | Template average rating |
| `totalRatings` | Integer | Template total ratings |
| `vendorId` | UUID | Vendor ID |
| `vendorName` | String | Vendor display name |
| `images` | List\<WorkshopImageResponse\> | Template images |
| `tags` | List\<WTagResponse\> | Template tags |

#### Responses

| Code | Description |
|------|-------------|
| `200` | Session retrieved successfully |
| `401` | Unauthorized — JWT token required |
| `404` | Session does not exist |

---

### 11. Get All Upcoming Sessions

Retrieves all `SCHEDULED` sessions starting in the future. **⚠️ Requires authentication** (not a public endpoint).

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/workshops/sessions` |
| **Auth** | 🔐 **Authenticated** — requires valid JWT token |

> ⚠️ **Note:** `/api/workshops/**` requires authentication. For public session browsing, use the Tourist API at `GET /api/public/workshops/templates/{id}/sessions`.

#### Query Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | int | `0` | Page number (0-based) |
| `size` | int | `10` | Items per page |
| `sortBy` | String | `startTime` | Field to sort by |
| `sortDir` | String | `asc` | Sort direction |

#### Response

`Page<WorkshopSessionResponse>` — paginated list of upcoming sessions.

#### Responses

| Code | Description |
|------|-------------|
| `200` | Sessions retrieved successfully |
| `401` | Unauthorized — JWT token required |

---

### 12. Get My Workshop Sessions (Paginated)

Retrieves all sessions created by the authenticated vendor.

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/workshops/sessions/my` |
| **Auth** | `VENDOR` role required |

#### Query Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | int | `0` | Page number (0-based) |
| `size` | int | `10` | Items per page |
| `sortBy` | String | `startTime` | Field to sort by |
| `sortDir` | String | `desc` | Sort direction |

#### Response

`Page<WorkshopSessionResponse>` — sessions in all statuses (SCHEDULED, ONGOING, COMPLETED, CANCELLED).

#### Responses

| Code | Description |
|------|-------------|
| `200` | Sessions retrieved successfully |
| `401` | Unauthorized |
| `403` | Forbidden — VENDOR role required |
| `404` | Vendor profile not found |

---

### 13. Get Sessions by Template ID

Retrieves all sessions for a specific template. **⚠️ Requires authentication** (not a public endpoint).

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/workshops/sessions/template/{templateId}` |
| **Auth** | 🔐 **Authenticated** — requires valid JWT token |

> ⚠️ **Note:** `/api/workshops/**` requires authentication. For public session browsing by template, use `GET /api/public/workshops/templates/{id}/sessions` instead.

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `templateId` | UUID | Workshop Template ID |

#### Query Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | int | `0` | Page number (0-based) |
| `size` | int | `10` | Items per page |
| `sortBy` | String | `startTime` | Field to sort by |
| `sortDir` | String | `asc` | Sort direction |

#### Responses

| Code | Description |
|------|-------------|
| `200` | Sessions retrieved successfully |
| `401` | Unauthorized — JWT token required |
| `404` | Template does not exist |

---

### 14. Filter / Search Workshop Sessions

Advanced search and filtering for sessions. **⚠️ Requires authentication** (not a public endpoint).

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/workshops/sessions/filter` |
| **Auth** | 🔐 **Authenticated** — requires valid JWT token |

> ⚠️ **Note:** `/api/workshops/**` requires authentication per SecurityConfig. There is currently no public equivalent for session filtering.

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
| `401` | Unauthorized — JWT token required |

---

### 15. Update Workshop Session

Updates an existing session. Only `SCHEDULED` sessions can be updated.

| | |
|---|---|
| **Method** | `PUT` |
| **URL** | `/api/workshops/sessions/{id}` |
| **Auth** | `VENDOR` role required + must own the session |
| **Body** | `UpdateWorkshopSessionRequest` (JSON) |

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | UUID | Workshop Session ID |

#### Request Body

All fields are **optional** — only send fields you want to update.

| Field | Type | Validation | Description |
|-------|------|------------|-------------|
| `startTime` | LocalDateTime | Must be in the future | Session start time |
| `endTime` | LocalDateTime | Must be after startTime | Session end time |
| `price` | BigDecimal | > 0 | Session price |
| `maxParticipants` | Integer | > 0, ≥ currentEnrolled, ≥ template minParticipants | Max attendees |

#### Editable Statuses

| Status | Can Edit? |
|--------|-----------|
| `SCHEDULED` | ✅ |
| `ONGOING` | ❌ In progress |
| `COMPLETED` | ❌ Already ended |
| `CANCELLED` | ❌ Already cancelled |

#### Responses

| Code | Description |
|------|-------------|
| `200` | Session updated successfully |
| `400` | Validation failed or wrong status |
| `401` | Unauthorized |
| `403` | Forbidden — not session owner |
| `404` | Session does not exist |

---

### 16. Delete Workshop Session

Permanently deletes a session. Only `SCHEDULED` sessions with **no enrollments** can be deleted.

| | |
|---|---|
| **Method** | `DELETE` |
| **URL** | `/api/workshops/sessions/{id}` |
| **Auth** | `VENDOR` role required + must own the session |

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | UUID | Workshop Session ID |

#### Pre-conditions

- Session must be `SCHEDULED`
- Session must have **0 enrollments**
- If the session has enrollments, use [Cancel](#17-cancel-workshop-session) instead

> ⚠️ **This action is permanent and cannot be undone.**

#### Responses

| Code | Description |
|------|-------------|
| `200` | Session deleted successfully |
| `400` | Has enrollments or wrong status |
| `401` | Unauthorized |
| `403` | Forbidden — not session owner |
| `404` | Session does not exist |

---

### 17. Cancel Workshop Session

Cancels a `SCHEDULED` session (soft delete). Use this when the session has enrollments.

| | |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/workshops/sessions/{id}/cancel` |
| **Auth** | `VENDOR` role required + must own the session |

#### Path Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | UUID | Workshop Session ID |

#### Behavior

- Status changes from `SCHEDULED` → `CANCELLED`
- Session is no longer bookable
- Session record **remains** in the database
- Existing enrollments are preserved for reference

#### Delete vs Cancel

| Action | Use When |
|--------|----------|
| **Delete** | Session has no enrollments — removes the record permanently |
| **Cancel** | Session has enrollments — keeps record, changes status to CANCELLED |

#### Responses

| Code | Description |
|------|-------------|
| `200` | Session cancelled successfully |
| `400` | Session is not SCHEDULED |
| `401` | Unauthorized |
| `403` | Forbidden — not session owner |
| `404` | Session does not exist |

---

## Vendor Workflow

### Template Lifecycle

```
┌──────────┐     Submit      ┌──────────┐     Admin      ┌──────────┐
│          │  ──────────────>│          │  ──Approve───> │          │
│  DRAFT   │    /register    │  PENDING │                │  ACTIVE  │
│          │ <────────────── │          │                │          │
└──────────┘   Edit & Fix    └──────────┘                └──────────┘
      ▲                            │                          │
      │                      Admin Reject               Toggle Publish
      │                            │                     /toggle-publish
      │                            ▼                          │
      │                     ┌──────────┐                      ▼
      └──── Edit & Fix ──── │ REJECTED │              isPublished: true
                            └──────────┘              (visible to tourists)
```

### Session Lifecycle

```
Vendor creates from ACTIVE template
            │
            ▼
     ┌─────────────┐
     │  SCHEDULED   │──── Delete (no enrollments)
     └──────┬──────┘──── Cancel (has enrollments) → CANCELLED
            │
       Time arrives
            │
            ▼
     ┌─────────────┐
     │   ONGOING    │
     └──────┬──────┘
            │
       Time ends
            │
            ▼
     ┌─────────────┐
     │  COMPLETED   │
     └─────────────┘
```

### Step-by-Step Guide

1. **Create a Template** — `POST /api/workshops/templates`
2. **Edit if needed** — `PUT /api/workshops/templates/{id}`
3. **Submit for Approval** — `POST /api/workshops/templates/{id}/register`
4. **Wait for Admin Review** (status becomes `PENDING`)
5. **If Rejected** — Edit the template, then submit again
6. **If Approved** (status becomes `ACTIVE`):
   - **Publish** — `POST /api/workshops/templates/{id}/toggle-publish`
   - **Create Sessions** — `POST /api/workshops/sessions`
7. **Manage Sessions** — Update, Cancel, or Delete as needed

---

## Quick Reference Table

### Vendor / Authenticated APIs (`/api/workshops/...`)

> ⚠️ All `/api/workshops/**` routes require authentication (valid JWT token). Routes with `VENDOR` also require the VENDOR role via `@PreAuthorize`.

| # | Method | Endpoint | Auth | Description |
|---|--------|----------|------|-------------|
| 1 | `POST` | `/api/workshops/templates` | VENDOR | Create template (DRAFT) |
| 2 | `GET` | `/api/workshops/templates/{id}` | VENDOR | Get template by ID (any status) |
| 3 | `GET` | `/api/workshops/templates/my` | VENDOR | Get my templates (paginated) |
| 4 | `GET` | `/api/workshops/templates/filter` | VENDOR | Search/filter templates (all statuses) |
| 5 | `PUT` | `/api/workshops/templates/{id}` | VENDOR | Update template (DRAFT/REJECTED only) |
| 6 | `POST` | `/api/workshops/templates/{id}/register` | VENDOR | Submit for admin approval |
| 7 | `POST` | `/api/workshops/templates/{id}/toggle-publish` | VENDOR | Toggle publish (ACTIVE only) |
| 8 | `DELETE` | `/api/workshops/templates/{id}` | VENDOR | Delete template (non-ACTIVE only) |
| 9 | `POST` | `/api/workshops/sessions` | VENDOR | Create session from ACTIVE template |
| 10 | `GET` | `/api/workshops/sessions/{id}` | VENDOR | Get session by ID |
| 11 | `GET` | `/api/workshops/sessions` | VENDOR | Get all upcoming sessions |
| 12 | `GET` | `/api/workshops/sessions/my` | VENDOR | Get my sessions (paginated) |
| 13 | `GET` | `/api/workshops/sessions/template/{templateId}` | VENDOR | Get sessions by template |
| 14 | `GET` | `/api/workshops/sessions/filter` | VENDOR | Search/filter sessions |
| 15 | `PUT` | `/api/workshops/sessions/{id}` | VENDOR | Update session (SCHEDULED only) |
| 16 | `DELETE` | `/api/workshops/sessions/{id}` | VENDOR | Delete session (no enrollments) |
| 17 | `POST` | `/api/workshops/sessions/{id}/cancel` | VENDOR | Cancel session (soft delete) |

### Public Tourist APIs (`/api/public/workshops/...`)

> ✅ These are the **truly public** endpoints — no authentication required. Defined in `WorkshopTouristController` and whitelisted via `/api/public/**` in SecurityConfig.

| # | Method | Endpoint | Auth | Description |
|---|--------|----------|------|-------------|
| P1 | `GET` | `/api/public/workshops/templates` | PUBLIC | Get all ACTIVE templates (paginated, 1-based page) |
| P2 | `GET` | `/api/public/workshops/templates/{id}` | PUBLIC | Get ACTIVE template detail (404 if not ACTIVE) |
| P3 | `GET` | `/api/public/workshops/templates/search` | PUBLIC | Search/filter ACTIVE templates (keyword, tag, price, duration, rating) |
| P4 | `GET` | `/api/public/workshops/templates/{id}/sessions` | PUBLIC | Get upcoming SCHEDULED sessions for an ACTIVE template |
| P5 | `GET` | `/api/wtags/all` | PUBLIC | Get all workshop tags (no pagination) |

