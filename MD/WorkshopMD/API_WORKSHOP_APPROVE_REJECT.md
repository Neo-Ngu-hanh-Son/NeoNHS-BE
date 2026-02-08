# Workshop Template Approve/Reject API Testing Guide

## Overview
This document provides testing instructions for the newly implemented Workshop Template approval/rejection APIs.

## Endpoints Implemented

### 1. POST /api/workshops/templates/{id}/approve
**Description**: Approve a pending workshop template (Admin only)

**Authorization**: Requires ADMIN role

**Path Parameters**:
- `id` (UUID): Workshop Template ID

**Success Response** (200 OK):
```json
{
  "statusCode": 200,
  "message": "Workshop template approved successfully",
  "data": {
    "id": "uuid",
    "name": "Workshop Name",
    "shortDescription": "...",
    "fullDescription": "...",
    "estimatedDuration": 120,
    "defaultPrice": 99.99,
    "minParticipants": 5,
    "maxParticipants": 20,
    "status": "ACTIVE",
    "averageRating": 0.0,
    "totalReview": 0,
    "vendorId": "vendor-uuid",
    "vendorName": "Vendor Business Name",
    "createdAt": "2026-02-04T21:00:00",
    "updatedAt": "2026-02-04T21:30:00",
    "rejectReason": null,
    "approvedBy": "admin-user-uuid",
    "approvedAt": "2026-02-04T21:30:00",
    "images": [...],
    "tags": [...]
  }
}
```

**Error Responses**:
- `400 Bad Request`: Template is not in PENDING status
- `401 Unauthorized`: No valid JWT token
- `403 Forbidden`: User is not an admin
- `404 Not Found`: Template does not exist

---

### 2. POST /api/workshops/templates/{id}/reject
**Description**: Reject a pending workshop template with reason (Admin only)

**Authorization**: Requires ADMIN role

**Path Parameters**:
- `id` (UUID): Workshop Template ID

**Request Body**:
```json
{
  "rejectReason": "The template contains inappropriate content and unclear descriptions. Please revise and resubmit."
}
```

**Success Response** (200 OK):
```json
{
  "statusCode": 200,
  "message": "Workshop template rejected",
  "data": {
    "id": "uuid",
    "name": "Workshop Name",
    "shortDescription": "...",
    "fullDescription": "...",
    "estimatedDuration": 120,
    "defaultPrice": 99.99,
    "minParticipants": 5,
    "maxParticipants": 20,
    "status": "REJECTED",
    "averageRating": 0.0,
    "totalReview": 0,
    "vendorId": "vendor-uuid",
    "vendorName": "Vendor Business Name",
    "createdAt": "2026-02-04T21:00:00",
    "updatedAt": "2026-02-04T21:30:00",
    "rejectReason": "The template contains inappropriate content and unclear descriptions. Please revise and resubmit.",
    "approvedBy": null,
    "approvedAt": null,
    "images": [...],
    "tags": [...]
  }
}
```

**Validation**:
- `rejectReason` is mandatory (not blank)

**Error Responses**:
- `400 Bad Request`: Template is not in PENDING status OR reject reason is missing/empty
- `401 Unauthorized`: No valid JWT token
- `403 Forbidden`: User is not an admin
- `404 Not Found`: Template does not exist

---

## Testing Workflow

### Prerequisites
1. Have at least one Admin user account
2. Have at least one Vendor user account with verified profile
3. Workshop template in PENDING status

### Step-by-Step Testing

#### Test Case 1: Approve a Workshop Template
1. **Login as Vendor** and create a workshop template (status: DRAFT)
2. **Submit template for approval** using POST `/api/workshops/templates/{id}/register`
   - Template status changes to PENDING
3. **Login as Admin**
4. **Call approve endpoint**: POST `/api/workshops/templates/{id}/approve`
   - Headers: `Authorization: Bearer {admin-jwt-token}`
5. **Verify Response**:
   - Status: 200 OK
   - `data.status` = "ACTIVE"
   - `data.approvedBy` = admin user ID
   - `data.approvedAt` = current timestamp
   - `data.rejectReason` = null

#### Test Case 2: Reject a Workshop Template
1. **Login as Vendor** and create another workshop template
2. **Submit template for approval**
3. **Login as Admin**
4. **Call reject endpoint**: POST `/api/workshops/templates/{id}/reject`
   - Headers: `Authorization: Bearer {admin-jwt-token}`
   - Body:
     ```json
     {
       "rejectReason": "Images are low quality. Please upload higher resolution images."
     }
     ```
5. **Verify Response**:
   - Status: 200 OK
   - `data.status` = "REJECTED"
   - `data.rejectReason` = the reason provided
   - `data.approvedBy` = null
   - `data.approvedAt` = null

#### Test Case 3: Vendor Resubmits After Rejection
1. **Login as Vendor**
2. **Update the rejected template** using PUT `/api/workshops/templates/{id}`
   - Upload better quality images
3. **Resubmit for approval** using POST `/api/workshops/templates/{id}/register`
   - Status changes from REJECTED → PENDING
   - `rejectReason` is cleared
4. **Admin can now approve or reject again**

#### Test Case 4: Error - Approve Non-Pending Template
1. **Try to approve a template** that is already ACTIVE or DRAFT
2. **Expected Response**: 400 Bad Request
   - Message: "Only templates with 'Pending' status can be approved. Current status: {status}"

#### Test Case 5: Error - Reject Without Reason
1. **Call reject endpoint** with empty or missing rejectReason
2. **Expected Response**: 400 Bad Request
   - Validation error for rejectReason field

#### Test Case 6: Error - Non-Admin User
1. **Login as Vendor**
2. **Try to approve or reject** any template
3. **Expected Response**: 403 Forbidden

---

## Database Verification

After approval, check the `workshop_templates` table:
```sql
SELECT id, name, status, approved_by, approved_at, reject_reason 
FROM workshop_templates 
WHERE id = 'your-template-uuid';
```

**Approved Template**:
- `status` = 'ACTIVE'
- `approved_by` = admin user UUID
- `approved_at` = timestamp
- `reject_reason` = NULL

**Rejected Template**:
- `status` = 'REJECTED'
- `approved_by` = NULL
- `approved_at` = NULL
- `reject_reason` = rejection message

---

## Swagger UI Testing

1. Navigate to: `http://localhost:8080/swagger-ui/index.html`
2. Locate "Workshop Management" section
3. Find endpoints:
   - `POST /api/workshops/templates/{id}/approve`
   - `POST /api/workshops/templates/{id}/reject`
4. Click "Try it out"
5. Enter template ID and (for reject) request body
6. Click "Authorize" and enter admin JWT token
7. Click "Execute"

---

## Postman Testing Examples

### Approve Template
```
POST http://localhost:8080/api/workshops/templates/{template-id}/approve
Headers:
  Authorization: Bearer eyJhbGc...
  Content-Type: application/json
```

### Reject Template
```
POST http://localhost:8080/api/workshops/templates/{template-id}/reject
Headers:
  Authorization: Bearer eyJhbGc...
  Content-Type: application/json
Body:
{
  "rejectReason": "Template needs improvement in the following areas: 1) Add more detailed description, 2) Include pricing breakdown, 3) Upload professional images"
}
```

---

## Status Flow Diagram

```
┌──────────┐
│  DRAFT   │ ← Vendor creates template
└────┬─────┘
     │ Vendor registers (POST /templates/{id}/register)
     ▼
┌──────────┐
│ PENDING  │ ← Vendor submitted for review
└────┬─────┘
     │
     ├─── Admin approves (POST /templates/{id}/approve) ──→ ┌────────┐
     │                                                       │ ACTIVE │
     │                                                       └────────┘
     │
     └─── Admin rejects (POST /templates/{id}/reject) ──→ ┌──────────┐
                                                           │ REJECTED │
                                                           └────┬─────┘
                                                                │ Vendor can edit
                                                                │ and resubmit
                                                                ▼
                                                           (back to PENDING)
```

---

## Notes
- Only templates with **PENDING** status can be approved or rejected
- Vendors can only edit templates with **DRAFT** or **REJECTED** status
- Once **ACTIVE**, templates cannot be edited by vendors
- Admin user ID is stored in `approvedBy` field when approving
- Approval timestamp is stored in `approvedAt` field
- Rejection reason is mandatory and stored for vendor to see
- When resubmitting a rejected template, the rejection reason is cleared
