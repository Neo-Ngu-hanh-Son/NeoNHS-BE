# Blog Frontend Implementation Guide (for AI Frontend Agent)

## 1) Scope
This guide describes how the frontend should integrate with the current backend for **admin blog management** and related dependencies (auth + blog categories).

Base URL examples:
- Local backend: `http://localhost:8080`
- Protected admin endpoints require `Authorization: Bearer <accessToken>` and user role `ADMIN`.

---

## 2) Standard API Envelope
All endpoints return a common response object:

```json
{
  "status": 200,
  "success": true,
  "message": "...",
  "data": {},
  "timestamp": "2026-02-15T12:34:56.789"
}
```

### Error Envelope
```json
{
  "status": 400,
  "success": false,
  "message": "Validation or business error message",
  "data": null,
  "timestamp": "2026-02-15T12:34:56.789"
}
```

### Frontend parsing rule
- Always read `success` first.
- Show `message` directly for user-facing toast/alert.
- Read payload from `data` only when `success === true`.

---

## 3.2 Fetch Blog Categories (for Create/Edit form)
### GET `/api/admin/blog-categories`
Query params:
- `page` (default `0`)
- `size` (default `10`)
- `search` (optional)
- `status` (optional: `ACTIVE | ARCHIVED`)
- `sortBy` (default `createdAt`)
- `sortDir` (default `desc`)

Response `data` is Spring Page:
```json
{
  "content": [
    {
      "id": "uuid",
      "name": "Travel",
      "slug": "travel",
      "description": "...",
      "status": "ACTIVE",
      "postCount": 12,
      "createdAt": "...",
      "updatedAt": "..."
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0,
  "first": true,
  "last": true
}
```

Frontend requirements:
- In blog form dropdown, prefer categories where `status === "ACTIVE"`.
- Send selected `id` as `blogCategoryId` in blog create/update payload.

---

## 3.3 List Blogs (admin table)
### GET `/api/admin/blogs`
Query params:
- `page` (default `0`)
- `size` (default `10`)
- `search` (optional, title contains)
- `status` (optional: `DRAFT | PUBLISHED | ARCHIVED`)
- `tags` (optional repeated param)
- `sortBy` (default `createdAt`)
- `sortDir` (default `desc`)

Example with tags:
`/api/admin/blogs?page=0&size=10&tags=food&tags=local`

Response `data` is Spring Page with blog items:
```json
{
  "content": [
    {
      "id": "uuid",
      "title": "My blog",
      "slug": "my-blog",
      "summary": "...",
      "contentJSON": "...",
      "contentHTML": "<p>...</p>",
      "thumbnailUrl": "...",
      "bannerUrl": "...",
      "isFeatured": false,
      "status": "DRAFT",
      "publishedAt": null,
      "tags": "food,local",
      "viewCount": 0,
      "blogCategory": {
        "id": "uuid",
        "name": "Travel",
        "slug": "travel",
        "description": "...",
        "status": "ACTIVE",
        "postCount": 12,
        "createdAt": "...",
        "updatedAt": "..."
      },
      "user": {
        "id": "uuid",
        "fullname": "Admin",
        "email": "admin@example.com",
        "phoneNumber": "...",
        "avatarUrl": "...",
        "role": "ADMIN",
        "isActive": true,
        "isVerified": true,
        "isBanned": false,
        "createdAt": "...",
        "updatedAt": "..."
      }
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 10,
  "number": 0,
  "first": true,
  "last": true
}
```

Important backend behavior:
- Soft-deleted blogs are excluded from list.
- Tag filtering is `contains` matching in backend (not exact array matching).

---

## 3.4 Get Blog Detail
### GET `/api/admin/blogs/{id}`
Returns single blog object in `data` with same shape as list item.

Possible errors:
- `404`: `"Blog not found"`
- `400`: invalid UUID format message from global exception handler.

---

## 3.5 Create Blog
### POST `/api/admin/blogs`
Request body (required fields marked):
```json
{
  "title": "My First Blog",                  // required, not blank
  "summary": "Short summary",                // optional
  "contentJSON": "{\"type\":\"doc\"}",    // required, not blank
  "contentHTML": "<p>Hello</p>",            // required, not blank
  "thumbnailUrl": "https://...",            // optional
  "bannerUrl": "https://...",               // optional
  "isFeatured": false,                        // optional, defaults false
  "status": "DRAFT",                        // optional, defaults DRAFT
  "tags": "food,local",                     // optional string
  "blogCategoryId": "uuid"                  // required, not null
}
```

Validation / business rules enforced by backend:
- `title` must be non-blank.
- `contentJSON` must be non-blank.
- `contentHTML` must be non-blank.
- `blogCategoryId` must be provided and exist.
- Category must not be deleted.
- Blog title must be unique among non-deleted blogs.

Publish behavior:
- If `status = PUBLISHED`, backend sets `publishedAt` automatically.
- If `status != PUBLISHED`, `publishedAt` is `null`.

---

## 3.6 Update Blog
### PUT `/api/admin/blogs/{id}`
Request body is same contract as create.

Validation / business rules:
- Blog must exist and not be soft-deleted.
- Title must remain unique (excluding current blog id).
- Category must exist and not be deleted.
- Authorization check in service allows admin users.

Publish behavior on update:
- If status changes to `PUBLISHED` and `publishedAt` is empty, backend sets current time.
- If status changes away from `PUBLISHED`, backend clears `publishedAt`.

---

## 3.7 Delete Blog (Soft Delete)
### DELETE `/api/admin/blogs/{id}`
Behavior:
- Marks blog as deleted internally (soft delete).
- Deleted blogs disappear from list/get operations for active items.

Success:
- `success = true`, message `"Blog deleted successfully"`, `data = null`.

---

## 4) Frontend Validation Rules (mirror backend)
Validate before request to reduce round-trips:
- `title`: required, trim, non-empty.
- `contentJSON`: required, non-empty.
- `contentHTML`: required, non-empty.
- `blogCategoryId`: required valid UUID string.
- `status`: only `DRAFT | PUBLISHED | ARCHIVED`.

Recommended UX:
- Disable submit when required fields invalid.
- Show backend `message` as authoritative when API rejects request.

---

## 5) Error Handling Matrix
- `400 Bad Request`
  - DTO validation: e.g. `"title: Title is required"`
  - Invalid UUID body/path parse
  - Business constraints: duplicate title, deleted category
- `401 Unauthorized`
  - Missing/invalid token, or unauthenticated principal
- `403 Forbidden`
  - Token exists but non-admin calls admin endpoint
- `404 Not Found`
  - Blog/category does not exist
- `500 Internal Server Error`
  - Generic unexpected backend failure

Always parse and display:
- `response.data.message` from `ApiResponse` envelope

---

## 6) Suggested Frontend API Client Contracts (TypeScript)

```ts
type ApiResponse<T> = {
  status: number;
  success: boolean;
  message: string;
  data: T | null;
  timestamp: string;
};

type Page<T> = {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
};

type BlogStatus = "DRAFT" | "PUBLISHED" | "ARCHIVED";
type BlogCategoryStatus = "ACTIVE" | "ARCHIVED";

type BlogRequest = {
  title: string;
  summary?: string;
  contentJSON: string;
  contentHTML: string;
  thumbnailUrl?: string;
  bannerUrl?: string;
  isFeatured?: boolean;
  status?: BlogStatus;
  tags?: string;
  blogCategoryId: string; // UUID
};
```

---

## 7) Known Contract Notes
- `BlogResponse` currently includes both `content` and `contentHTML`, but backend mapping only sets `contentHTML`; `content` may be `null`.
- For editor screens, store and send both `contentJSON` (contentJSON is for Lexical editor) and `contentHTML` in requests.
- For rendering returned detail/list, rely on `contentHTML`.

---

## 8) Minimum Call Flow for Blog Admin UI
1. Login admin and store bearer token.
2. Fetch categories for select options.
3. Fetch paginated blog list (with optional status/search/tags).
4. Create or update blog with full `BlogRequest` payload.
5. Delete blog using soft-delete endpoint.
6. On any failure, show `ApiResponse.message`.
