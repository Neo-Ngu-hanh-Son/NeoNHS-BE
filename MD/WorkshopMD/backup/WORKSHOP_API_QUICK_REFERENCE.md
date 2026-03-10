# Workshop API Quick Reference Guide

## 📚 Complete Endpoint List

### 🔓 Public Endpoints (No Authentication Required)

```
GET    /api/workshops/templates/{id}         Get template by ID
GET    /api/workshops/templates/filter       Search and filter templates
```

---

### 👨‍💼 Vendor Endpoints (VENDOR Role Required)

```
POST   /api/workshops/templates              Create new template
GET    /api/workshops/templates/my           Get my templates (paginated)
PUT    /api/workshops/templates/{id}         Update template
DELETE /api/workshops/templates/{id}         Delete template
POST   /api/workshops/templates/{id}/register Submit for approval
```

---

### 🛡️ Admin Endpoints (ADMIN Role Required)

```
GET    /api/workshops/templates              Get all templates (paginated)
POST   /api/workshops/templates/{id}/approve  Approve template
POST   /api/workshops/templates/{id}/reject   Reject template
```

---

## 🚀 Quick Start Examples

### Create Template (Vendor)
```http
POST /api/workshops/templates
Authorization: Bearer <vendor-jwt-token>
Content-Type: application/json

{
  "name": "Beginner Yoga Workshop",
  "shortDescription": "Learn basic yoga poses",
  "fullDescription": "A comprehensive introduction to yoga...",
  "defaultPrice": 99.99,
  "estimatedDuration": 120,
  "minParticipants": 5,
  "maxParticipants": 20,
  "tagIds": ["uuid-1", "uuid-2"],
  "imageUrls": ["https://example.com/image1.jpg"],
  "thumbnailIndex": 0
}
```

### Get Template by ID (Public)
```http
GET /api/workshops/templates/550e8400-e29b-41d4-a716-446655440000
```

### Get My Templates (Vendor)
```http
GET /api/workshops/templates/my?page=1&size=10&sortBy=createdAt&sortDir=desc
Authorization: Bearer <vendor-jwt-token>
```

### Search Templates (Public)
```http
GET /api/workshops/templates/filter?keyword=yoga&minPrice=50&maxPrice=100&status=ACTIVE
```

### Submit for Approval (Vendor)
```http
POST /api/workshops/templates/550e8400-e29b-41d4-a716-446655440000/register
Authorization: Bearer <vendor-jwt-token>
```

### Approve Template (Admin)
```http
POST /api/workshops/templates/550e8400-e29b-41d4-a716-446655440000/approve
Authorization: Bearer <admin-jwt-token>
```

### Reject Template (Admin)
```http
POST /api/workshops/templates/550e8400-e29b-41d4-a716-446655440000/reject
Authorization: Bearer <admin-jwt-token>
Content-Type: application/json

{
  "rejectReason": "Images are low quality. Please upload higher resolution images."
}
```

### Update Template (Vendor)
```http
PUT /api/workshops/templates/550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer <vendor-jwt-token>
Content-Type: application/json

{
  "name": "Updated Workshop Name",
  "defaultPrice": 109.99
}
```

### Delete Template (Vendor)
```http
DELETE /api/workshops/templates/550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer <vendor-jwt-token>
```

---

## 📊 Status Flow

```
DRAFT ──────► PENDING ──────► ACTIVE
  ▲               │
  │               ▼
  └──────── REJECTED
```

### Status Permissions

| Status | Can Edit? | Can Delete? | Can Submit? | Can Approve? |
|--------|-----------|-------------|-------------|--------------|
| DRAFT | ✅ Vendor | ✅ Vendor | ✅ Vendor | ❌ |
| PENDING | ❌ | ✅ Vendor | ❌ | ✅ Admin |
| ACTIVE | ❌ | ❌ | ❌ | ❌ |
| REJECTED | ✅ Vendor | ✅ Vendor | ✅ Vendor | ❌ |

---

## 🔍 Search & Filter Parameters

### Available Filters
- `keyword` - Search in name and descriptions
- `name` - Exact or partial name match
- `status` - DRAFT, PENDING, ACTIVE, REJECTED
- `vendorId` - Filter by vendor UUID
- `tagId` - Filter by tag/category UUID
- `minPrice` / `maxPrice` - Price range
- `minDuration` / `maxDuration` - Duration range (minutes)
- `minRating` - Minimum average rating

### Example Queries

**Find yoga workshops:**
```
GET /api/workshops/templates/filter?keyword=yoga
```

**Price range $50-$100:**
```
GET /api/workshops/templates/filter?minPrice=50&maxPrice=100
```

**Active templates only:**
```
GET /api/workshops/templates/filter?status=ACTIVE
```

**60-120 minute workshops:**
```
GET /api/workshops/templates/filter?minDuration=60&maxDuration=120
```

**Highly rated (4+ stars):**
```
GET /api/workshops/templates/filter?minRating=4.0
```

**Combine filters:**
```
GET /api/workshops/templates/filter?keyword=cooking&status=ACTIVE&minPrice=30&maxPrice=80&minRating=4.5
```

---

## 📋 Pagination Parameters

All paginated endpoints support:

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | int | 1 | Page number (1-based) |
| `size` | int | 10 | Items per page |
| `sortBy` | string | "createdAt" | Field to sort by |
| `sortDir` | string | "desc" | Sort direction (asc/desc) |

### Example
```
GET /api/workshops/templates?page=2&size=20&sortBy=name&sortDir=asc
```

---

## ⚠️ Common Error Responses

### 400 Bad Request
```json
{
  "statusCode": 400,
  "message": "Validation failed or business rule violated",
  "data": null
}
```

**Common Causes:**
- Template not in correct status for operation
- Missing required fields
- Invalid field values
- Vendor not verified

### 401 Unauthorized
```json
{
  "statusCode": 401,
  "message": "Authentication required",
  "data": null
}
```

**Cause:** Missing or invalid JWT token

### 403 Forbidden
```json
{
  "statusCode": 403,
  "message": "Access denied - insufficient permissions",
  "data": null
}
```

**Common Causes:**
- Wrong role (vendor trying admin endpoint)
- Not template owner
- Account not activated

### 404 Not Found
```json
{
  "statusCode": 404,
  "message": "Resource not found",
  "data": null
}
```

**Common Causes:**
- Template doesn't exist
- Invalid UUID format
- Vendor profile not found

---

## 🎯 Use Case Scenarios

### Scenario 1: Vendor Creates and Publishes Template

1. **Create draft template**
   ```
   POST /api/workshops/templates
   → Status: DRAFT
   ```

2. **Review and edit** (optional)
   ```
   PUT /api/workshops/templates/{id}
   → Status: Still DRAFT
   ```

3. **Submit for approval**
   ```
   POST /api/workshops/templates/{id}/register
   → Status: PENDING
   ```

4. **Admin approves**
   ```
   POST /api/workshops/templates/{id}/approve
   → Status: ACTIVE
   ```

5. **Template is now live!** 🎉

---

### Scenario 2: Admin Rejects Template

1. **Admin reviews pending template**
   ```
   GET /api/workshops/templates/{id}
   ```

2. **Admin finds issues and rejects**
   ```
   POST /api/workshops/templates/{id}/reject
   Body: { "rejectReason": "..." }
   → Status: REJECTED
   ```

3. **Vendor views rejection reason**
   ```
   GET /api/workshops/templates/{id}
   → See rejectReason field
   ```

4. **Vendor fixes issues**
   ```
   PUT /api/workshops/templates/{id}
   → Status: Still REJECTED
   ```

5. **Vendor resubmits**
   ```
   POST /api/workshops/templates/{id}/register
   → Status: PENDING (rejectReason cleared)
   ```

---

### Scenario 3: User Browses Workshops

1. **View all active workshops**
   ```
   GET /api/workshops/templates/filter?status=ACTIVE
   ```

2. **Search for specific topic**
   ```
   GET /api/workshops/templates/filter?keyword=cooking
   ```

3. **Filter by price and rating**
   ```
   GET /api/workshops/templates/filter?status=ACTIVE&minPrice=50&maxPrice=100&minRating=4.0
   ```

4. **View template details**
   ```
   GET /api/workshops/templates/{id}
   ```

---

## 🔐 Authentication

### JWT Token Format
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Obtaining Tokens
Use the authentication endpoints:
- `POST /api/auth/login` - Login to get JWT token
- `POST /api/auth/refresh` - Refresh expired token

### Token in Request
```http
GET /api/workshops/templates/my
Authorization: Bearer <your-jwt-token-here>
```

---

## 📱 Response Format

All endpoints return data in this format:

```json
{
  "statusCode": 200,
  "message": "Success message",
  "data": {
    // Response data here
  }
}
```

### Success Response Example
```json
{
  "statusCode": 200,
  "message": "Workshop template retrieved successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Beginner Yoga Workshop",
    "shortDescription": "Learn basic yoga poses",
    "fullDescription": "A comprehensive introduction...",
    "estimatedDuration": 120,
    "defaultPrice": 99.99,
    "minParticipants": 5,
    "maxParticipants": 20,
    "status": "ACTIVE",
    "averageRating": 4.5,
    "totalReview": 10,
    "vendorId": "vendor-uuid",
    "vendorName": "Yoga Studio",
    "createdAt": "2026-02-08T10:00:00",
    "updatedAt": "2026-02-08T15:00:00",
    "rejectReason": null,
    "approvedBy": "admin-uuid",
    "approvedAt": "2026-02-08T15:00:00",
    "images": [
      {
        "id": "img-uuid",
        "imageUrl": "https://example.com/image.jpg",
        "isThumbnail": true
      }
    ],
    "tags": [
      {
        "id": "tag-uuid",
        "name": "Yoga",
        "description": "Yoga workshops",
        "tagColor": "#FF5733",
        "iconUrl": "https://example.com/icon.png"
      }
    ]
  }
}
```

---

## 🧪 Testing with Swagger UI

1. Navigate to: `http://localhost:8080/swagger-ui/index.html`
2. Find **"Workshop Management"** section
3. Click on any endpoint to expand
4. Click **"Try it out"** button
5. Fill in required parameters
6. Click **"Authorize"** (🔒) to add JWT token
7. Click **"Execute"** to test
8. View response below

---

## 💡 Tips & Best Practices

### For Vendors
- ✅ Complete all fields before submitting
- ✅ Use high-quality images
- ✅ Write clear, detailed descriptions
- ✅ Review rejection reasons carefully
- ✅ Test pricing competitiveness

### For Admins
- ✅ Provide clear rejection reasons
- ✅ Check for policy compliance
- ✅ Verify image quality
- ✅ Review pricing reasonableness
- ✅ Ensure descriptions are complete

### For Developers
- ✅ Always include JWT token for protected endpoints
- ✅ Handle all error status codes
- ✅ Implement pagination for large lists
- ✅ Use filter parameters efficiently
- ✅ Cache public endpoint responses

---

## 📞 Support

For issues or questions:
1. Check Swagger UI documentation
2. Review error messages
3. Verify JWT token validity
4. Check user role permissions
5. Consult API documentation files

---

**Last Updated:** February 8, 2026  
**API Version:** 1.0  
**Documentation Status:** ✅ Complete
