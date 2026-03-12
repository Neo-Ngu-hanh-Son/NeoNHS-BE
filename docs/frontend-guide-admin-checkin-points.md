# Frontend API Guideline: Admin Checkin Points

## Scope
This guideline covers the Admin Checkin Point APIs exposed by AdminCheckinPointController.

- Base path: /api/admin/checkin-points
- Required role: ADMIN
- Auth: Bearer JWT
- Content-Type: application/json

Source controller:
- src/main/java/fpt/project/NeoNHS/controller/admin/AdminCheckinPointController.java

---

## 1) Response Envelope (All Endpoints)

All successful responses use ApiResponse<T>:

```json
{
  "status": 200,
  "success": true,
  "message": "Success",
  "data": {},
  "timestamp": "2026-03-12T10:30:00"
}
```

Common error envelope (from GlobalExceptionHandler):

```json
{
  "status": 400,
  "success": false,
  "message": "fieldName: validation message",
  "data": null,
  "timestamp": "2026-03-12T10:30:00"
}
```

Error status patterns:
- 400: validation, malformed JSON, business rule failures
- 401: missing/invalid token
- 403: authenticated but not ADMIN
- 404: resource not found (point/checkin-point)
- 500: unexpected server error

---

## 2) Data Contracts

### 2.1 Request Body: CheckinPointRequest

```json
{
  "pointId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Main Gate Check-in",
  "description": "Check-in at main gate",
  "position": "North Entrance",
  "thumbnailUrl": "https://cdn.example.com/checkin/main.jpg",
  "isActive": true,
  "qrCode": "CHECKIN_MAIN_GATE_001",
  "longitude": 108.246,
  "latitude": 16.047,
  "rewardPoints": 10,
  "panoramaImageUrl": "https://cdn.example.com/checkin/pano.jpg",
  "defaultYaw": 0,
  "defaultPitch": 0
}
```

Validation and constraints:
- pointId: required
- name: required, non-blank, max 255
- position: max 255
- thumbnailUrl: max 255
- qrCode: max 255
- longitude: between -180 and 180
- latitude: between -90 and 90
- rewardPoints: >= 0
- panoramaImageUrl: max 2048

Important defaults applied by backend on create:
- isActive: defaults to true when omitted/null
- defaultYaw: defaults to 0.0 when omitted/null
- defaultPitch: defaults to 0.0 when omitted/null

### 2.2 Response Body: PointCheckinResponse

```json
{
  "id": "9e4024f7-a5f1-4bbd-bf1a-a95f9d9bd7a7",
  "name": "Main Gate Check-in",
  "description": "Check-in at main gate",
  "position": "North Entrance",
  "thumbnailUrl": "https://cdn.example.com/checkin/main.jpg",
  "isActive": true,
  "qrCode": "CHECKIN_MAIN_GATE_001",
  "longitude": 108.246,
  "latitude": 16.047,
  "rewardPoints": 10,
  "panoramaImageUrl": "https://cdn.example.com/checkin/pano.jpg",
  "defaultYaw": 0,
  "defaultPitch": 0,
  "isUserCheckedIn": false
}
```

Notes:
- For admin endpoints in this controller, isUserCheckedIn is typically null or false (not user-context driven).
- longitude/latitude are returned as numbers (Double in response).

---

## 3) Endpoint-by-Endpoint Guide

## 3.1 Create Checkin Point

- Method: POST
- URL: /api/admin/checkin-points
- Body: CheckinPointRequest
- Success: 201 Created

Example:

```http
POST /api/admin/checkin-points
Authorization: Bearer <admin_jwt>
Content-Type: application/json
```

Frontend behavior:
- Use create form validation matching backend constraints.
- pointId must reference an existing point or API returns 404.
- On success, show message from response and refresh list.

---

## 3.2 Get Checkin Point By ID

- Method: GET
- URL: /api/admin/checkin-points/{id}
- Success: 200 OK

Example:

```http
GET /api/admin/checkin-points/9e4024f7-a5f1-4bbd-bf1a-a95f9d9bd7a7
Authorization: Bearer <admin_jwt>
```

Frontend behavior:
- Use for detail screen and prefill edit form.
- If 404, show not-found state and return to list.

---

## 3.3 Get All Checkin Points (Admin List)

- Method: GET
- URL: /api/admin/checkin-points/all
- Success: 200 OK (paginated)

Query params:
- page (int): default from backend constant, usually 0
- size (int): default from backend constant, usually 10
- sortBy (string): default name
- sortDir (string): asc or desc, default asc
- search (string): optional keyword search by name
- includeDeleted (boolean): default true

Example:

```http
GET /api/admin/checkin-points/all?page=0&size=10&sortBy=name&sortDir=asc&search=main&includeDeleted=true
Authorization: Bearer <admin_jwt>
```

Important includeDeleted behavior:
- includeDeleted=true: returns both active and soft-deleted records.
- includeDeleted=false: returns only non-deleted records.

Why:
- Backend applies deletedAt filter only when includeDeleted=false.

Frontend recommendations:
- Provide a toggle/filter for Include deleted.
- Default UI choice:
  - Admin maintenance screen: includeDeleted=true
  - Operational list screen: includeDeleted=false
- Render pagination from Spring Page object (content, totalElements, totalPages, number, size).

---

## 3.4 Update Checkin Point

- Method: PUT
- URL: /api/admin/checkin-points/{id}
- Body: CheckinPointRequest
- Success: 200 OK

Example:

```http
PUT /api/admin/checkin-points/9e4024f7-a5f1-4bbd-bf1a-a95f9d9bd7a7
Authorization: Bearer <admin_jwt>
Content-Type: application/json
```

Implementation details to account for:
- Service supports partial field updates internally.
- Controller enforces @Valid with CheckinPointRequest (pointId and name are still required in request class).

Frontend recommendation:
- Send full object from form (including pointId and name) to avoid validation failures.
- If pointId changes, backend verifies target point exists; 404 if not.

---

## 3.5 Delete Checkin Point (Soft Delete)

- Method: DELETE
- URL: /api/admin/checkin-points/{id}
- Success: 200 OK

Example:

```http
DELETE /api/admin/checkin-points/9e4024f7-a5f1-4bbd-bf1a-a95f9d9bd7a7
Authorization: Bearer <admin_jwt>
```

Important behavior:
- This is a soft delete (sets deletedAt and deletedBy).
- Record still exists in DB and can appear in list when includeDeleted=true.
- No restore endpoint is provided by this controller.

Frontend recommendation:
- Label action as Soft delete in admin UI.
- After delete, refresh list with current includeDeleted filter.

---

## 4) Suggested TypeScript Types

```ts
export interface ApiResponse<T> {
  status: number;
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface CheckinPointRequest {
  pointId: string;
  name: string;
  description?: string;
  position?: string;
  thumbnailUrl?: string;
  isActive?: boolean;
  qrCode?: string;
  longitude?: number;
  latitude?: number;
  rewardPoints?: number;
  panoramaImageUrl?: string;
  defaultYaw?: number;
  defaultPitch?: number;
}

export interface PointCheckinResponse {
  id: string;
  name: string;
  description?: string;
  position?: string;
  thumbnailUrl?: string;
  isActive?: boolean;
  qrCode?: string;
  longitude?: number;
  latitude?: number;
  rewardPoints?: number;
  panoramaImageUrl?: string;
  defaultYaw?: number;
  defaultPitch?: number;
  isUserCheckedIn?: boolean;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}
```

---

## 5) Suggested Frontend API Methods

```ts
// Base: /api/admin/checkin-points

create(payload: CheckinPointRequest): Promise<ApiResponse<PointCheckinResponse>>
getById(id: string): Promise<ApiResponse<PointCheckinResponse>>
getAll(params: {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
  search?: string;
  includeDeleted?: boolean;
}): Promise<ApiResponse<PageResponse<PointCheckinResponse>>>
update(id: string, payload: CheckinPointRequest): Promise<ApiResponse<PointCheckinResponse>>
delete(id: string): Promise<ApiResponse<null>>
```

---

## 6) UI and Validation Checklist

Create/Edit form:
- pointId required (select input)
- name required and trimmed
- latitude range: -90 to 90
- longitude range: -180 to 180
- rewardPoints min 0
- URL length constraints

List screen:
- server-side pagination
- search by name
- sort controls
- includeDeleted toggle
- clear state for empty results

Error handling:
- Show response.message for 4xx
- 401: redirect to login / refresh token flow
- 403: show access denied state
- 404: resource not found UI

---

## 7) Known Backend Nuances (Frontend Should Know)

1. Update endpoint uses full request DTO validation.
- Even though service supports partial update logic, controller-level validation still requires pointId and name in payload.

2. Deleted records are still fetchable by id for admin.
- getById does not exclude soft-deleted records.

3. includeDeleted defaults to true.
- If frontend does not send this param, list includes soft-deleted records.

4. Delete is soft only.
- There is no hard delete or restore endpoint in this controller.

---

## 8) Example End-to-End Flow for Frontend

1. Load list with includeDeleted=false for normal operations.
2. Open create modal, submit POST.
3. Refresh list.
4. Open detail/edit with GET by id.
5. Submit PUT with full request body.
6. Soft delete using DELETE.
7. In audit/admin view, switch includeDeleted=true to inspect deleted records.
