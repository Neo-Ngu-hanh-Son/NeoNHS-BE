# Workshop Template: `isPublished` Feature — Frontend Guide

## Overview

A new boolean field `isPublished` has been added to **Workshop Templates**. This gives vendors control over whether their approved template appears in the public catalog — similar to YouTube's "unlisted" feature.

**Key concept:** `status` (DRAFT → PENDING → ACTIVE / REJECTED) is the **admin approval workflow**. `isPublished` is the **vendor visibility toggle** that only applies to ACTIVE templates.

---

## 1. New Field: `isPublished`

### Response Change

`WorkshopTemplateResponse` now includes:

```json
{
  "id": "...",
  "name": "Yoga Workshop",
  "status": "ACTIVE",
  "isPublished": true,       // ← NEW FIELD
  "defaultPrice": 150000,
  ...
}
```

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `isPublished` | `Boolean` | `false` | Whether the template is visible in public catalog/search |

### Default Behavior

`isPublished` is **always `false`** in these scenarios:

| Event | `isPublished` value |
|-------|---------------------|
| Template created (DRAFT) | `false` |
| Template submitted for approval (PENDING) | `false` |
| Admin approves (ACTIVE) | `false` |
| Admin rejects (REJECTED) | `false` |

> **Important:** After admin approval, the vendor must **explicitly publish** the template. It does NOT auto-publish on approval.

---

## 2. New API: Toggle Publish

### `POST /api/workshops/templates/{id}/toggle-publish`

**Auth:** Vendor only (must own the template)

**Prerequisite:** Template must have status `ACTIVE`

**Behavior:** Flips `isPublished` between `true` ↔ `false`

#### Request

```
POST /api/workshops/templates/{id}/toggle-publish
Authorization: Bearer <vendor_token>
```

No request body needed.

#### Success Response (200)

```json
{
  "status": 200,
  "message": "Workshop template publish status toggled successfully",
  "data": {
    "id": "abc-123",
    "name": "Yoga Workshop",
    "status": "ACTIVE",
    "isPublished": true,
    ...
  }
}
```

#### Error Responses

| Code | Condition |
|------|-----------|
| 400 | Template is not ACTIVE (e.g., DRAFT, PENDING, REJECTED) |
| 403 | Not the template owner |
| 404 | Template not found |

---

## 3. Impact on Public (Tourist) Endpoints

### Templates

| Endpoint | Behavior |
|----------|----------|
| `GET /api/public/workshops/templates` | Only returns `ACTIVE` **AND** `isPublished=true` templates |
| `GET /api/public/workshops/templates/search` | Only returns `ACTIVE` **AND** `isPublished=true` templates |
| `GET /api/public/workshops/templates/{id}` | Returns any `ACTIVE` template (published or not) — **direct link works** |

### Sessions

| Endpoint | Behavior |
|----------|----------|
| `GET /api/workshops/sessions` | Only returns sessions from `isPublished=true` templates |
| `GET /api/workshops/sessions/filter` | Only returns sessions from `isPublished=true` templates |
| `GET /api/public/workshops/templates/{id}/sessions` | Returns sessions if template is `ACTIVE` (published or not) — **direct link works** |
| `GET /api/workshops/sessions/{id}` | Returns any session — **direct link works** |

### Summary Table

```
                              Catalog/Search    Direct Link
                              ──────────────    ───────────
Published template (true)     ✅ Visible        ✅ Accessible
Unpublished template (false)  ❌ Hidden         ✅ Accessible
Non-ACTIVE template           ❌ Hidden         ❌ Not found
```

---

## 4. Impact on Vendor Endpoints

| Endpoint | Behavior |
|----------|----------|
| `GET /api/workshops/templates/my` | Returns ALL vendor templates (no `isPublished` filter) |
| `GET /api/workshops/sessions/my` | Returns ALL vendor sessions (no `isPublished` filter) |
| `POST /api/workshops/sessions` | Still requires template to be `ACTIVE` (no `isPublished` check) |

> Vendors can always see and manage their own templates/sessions regardless of publish status.

---

## 5. Unpublish Behavior — What Happens to Existing Data

When a vendor **unpublishes** a template (`isPublished: true → false`):

| What | Impact |
|------|--------|
| **Existing sessions** | **No change.** All SCHEDULED/ONGOING/COMPLETED sessions remain as-is. |
| **Existing bookings (tickets/orders)** | **No change.** Tourist tickets remain ACTIVE. Orders are unaffected. |
| **New tourist browsing catalog** | **Cannot find** the template or its sessions via catalog/search. |
| **Tourist with direct link** | **Can still access** the template detail and its sessions. |

---

## 6. Frontend Implementation Guide

### Vendor Dashboard

1. **Template list page:** Show `isPublished` status with a toggle/badge
   ```
   Status: ACTIVE  |  Published: ✅ (or toggle switch)
   ```

2. **Toggle button:** Only show/enable when `status === "ACTIVE"`
   - Call `POST /api/workshops/templates/{id}/toggle-publish`
   - Update UI after success response

3. **After admin approval:** Show a prompt/banner:
   > "Your template has been approved! Click 'Publish' to make it visible to tourists."

4. **Visual states for template cards:**

   | status | isPublished | Badge/Label |
   |--------|-------------|-------------|
   | DRAFT | false | 🔘 Draft |
   | PENDING | false | ⏳ Pending Review |
   | REJECTED | false | ❌ Rejected |
   | ACTIVE | false | ✅ Approved (Unpublished) |
   | ACTIVE | true | 🟢 Published |

### Tourist / Public Pages

- No frontend changes needed for catalog/search — backend already filters.
- Template detail page works with direct links regardless of `isPublished`.

### Typical Vendor Flow

```
1. Create template                          → status=DRAFT, isPublished=false
2. Fill in details, images, tags
3. Submit for approval (POST .../register)  → status=PENDING, isPublished=false
4. Admin approves                           → status=ACTIVE, isPublished=false
5. Vendor clicks "Publish"                  → status=ACTIVE, isPublished=true  ← NOW VISIBLE
6. Vendor creates sessions
7. Tourists browse and book
8. Vendor wants to hide temporarily
9. Vendor clicks "Unpublish"                → status=ACTIVE, isPublished=false ← HIDDEN
   └── Existing sessions & bookings: UNCHANGED
10. Vendor clicks "Publish" again           → status=ACTIVE, isPublished=true  ← VISIBLE AGAIN
```

---

## 7. API Quick Reference

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/workshops/templates/{id}/toggle-publish` | Vendor | Toggle `isPublished` (ACTIVE only) |
| `GET` | `/api/public/workshops/templates` | Public | List — filtered by `isPublished=true` |
| `GET` | `/api/public/workshops/templates/search` | Public | Search — filtered by `isPublished=true` |
| `GET` | `/api/public/workshops/templates/{id}` | Public | Detail — direct link, no `isPublished` check |
| `GET` | `/api/public/workshops/templates/{id}/sessions` | Public | Sessions — direct link, no `isPublished` check |
| `GET` | `/api/workshops/sessions` | Public | Upcoming — filtered by `isPublished=true` |
| `GET` | `/api/workshops/sessions/filter` | Public | Search — filtered by `isPublished=true` |
| `GET` | `/api/workshops/sessions/{id}` | Public | Detail — direct link, no filter |
