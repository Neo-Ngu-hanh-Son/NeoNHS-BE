# Workshop Template Registration API

## Overview
This document describes the newly implemented Workshop Template Registration API that allows vendors to submit their workshop templates for admin approval.

## Summary of Changes

### 1. Updated WorkshopStatus Enum
**File:** `src/main/java/fpt/project/NeoNHS/enums/WorkshopStatus.java`

Added new status to support the template lifecycle:
- `DRAFT` - Template created but not submitted (NEW)
- `PENDING` - Submitted and awaiting admin approval
- `ACTIVE` - Approved by admin and published
- `REJECTED` - Rejected by admin

### 2. Updated WorkshopTemplate Entity
**File:** `src/main/java/fpt/project/NeoNHS/entity/WorkshopTemplate.java`

Changed default status from `PENDING` to `DRAFT` so new templates start as drafts.

### 3. Service Interface
**File:** `src/main/java/fpt/project/NeoNHS/service/WorkshopTemplateService.java`

Added new method:
```java
WorkshopTemplateResponse registerWorkshopTemplate(String email, UUID id);
```

### 4. Service Implementation
**File:** `src/main/java/fpt/project/NeoNHS/service/impl/WorkshopTemplateServiceImpl.java`

#### Implemented `registerWorkshopTemplate` Method
- **Validates ownership**: Only the template owner can submit
- **Validates status**: Only DRAFT or REJECTED templates can be submitted
- **Validates completeness**: Ensures all mandatory fields are present:
  - Title/Name
  - Short Description
  - Full Description
  - Price
  - Duration
  - Min/Max Participants
  - At least one image
  - At least one category/tag
- **Updates status**: Changes status to PENDING
- **Clears rejection reason**: If previously rejected

#### Updated `updateWorkshopTemplate` Method
- Now only allows updates for DRAFT or REJECTED templates
- PENDING templates cannot be edited (locked for admin review)
- ACTIVE templates cannot be edited

#### Updated `deleteWorkshopTemplate` Method
- Updated comment to include DRAFT in allowed statuses

### 5. Controller Endpoint
**File:** `src/main/java/fpt/project/NeoNHS/controller/WorkshopController.java`

Added new POST endpoint:

```
POST /api/workshops/templates/{id}/register
```

**Security:** Requires `ROLE_VENDOR` authority

**Request:**
- Path Parameter: `id` (UUID) - Workshop Template ID
- Authentication: JWT token in Authorization header

**Response:**
```json
{
  "success": true,
  "message": "Template submitted successfully. Please wait for admin approval.",
  "data": {
    "id": "uuid",
    "name": "Workshop Title",
    "status": "PENDING",
    ...
  }
}
```

**Status Codes:**
- `200 OK` - Template submitted successfully
- `400 Bad Request` - Template incomplete, wrong status, or validation failed
- `401 Unauthorized` - Missing or invalid JWT token
- `403 Forbidden` - Not a vendor or not the template owner
- `404 Not Found` - Template does not exist

## Use Case Flow

### Normal Case
1. Vendor creates a workshop template → Status: `DRAFT`
2. Vendor fills in all required information
3. Vendor calls `POST /api/workshops/templates/{id}/register`
4. System validates completeness
5. System changes status to `PENDING`
6. Admin reviews and approves → Status: `ACTIVE`

### Rejection & Resubmission Case
1. Admin rejects template → Status: `REJECTED` (with reason)
2. Vendor updates the template (fixing issues)
3. Vendor calls `POST /api/workshops/templates/{id}/register` again
4. System changes status to `PENDING`
5. Admin reviews again

### Abnormal Cases

#### Incomplete Template
Request: `POST /api/workshops/templates/{id}/register`

Response (400 Bad Request):
```json
{
  "success": false,
  "message": "Cannot submit incomplete template. Missing required fields: Image (at least one required), Full Description"
}
```

#### Wrong Status
If template is already PENDING or ACTIVE:

Response (400 Bad Request):
```json
{
  "success": false,
  "message": "Only templates with 'Draft' or 'Rejected' status can be submitted for approval"
}
```

#### Not Owner
If user is not the template owner:

Response (400 Bad Request):
```json
{
  "success": false,
  "message": "You do not have permission to submit this workshop template"
}
```

## Testing

### Example Request (using curl)
```bash
curl -X POST "http://localhost:8080/api/workshops/templates/{template-id}/register" \
  -H "Authorization: Bearer {jwt-token}" \
  -H "Content-Type: application/json"
```

### Example Request (using Swagger UI)
1. Navigate to: http://localhost:8080/swagger-ui.html
2. Find: `POST /api/workshops/templates/{id}/register`
3. Click "Try it out"
4. Enter template ID
5. Click "Execute"

## Validation Rules

| Field | Rule |
|-------|------|
| Title/Name | Must not be empty |
| Short Description | Must not be empty |
| Full Description | Must not be empty |
| Price | Must be present |
| Duration | Must be > 0 |
| Min Participants | Must be > 0 |
| Max Participants | Must be > 0 |
| Images | At least 1 required |
| Tags/Categories | At least 1 required |

## Status Transitions

```
DRAFT ─────────register()─────────> PENDING
  ^                                     │
  │                                     │
  │                                     ▼
  │                                 (Admin Review)
  │                                     │
  │                              ┌──────┴──────┐
  │                              │             │
  │                          approve()     reject()
  │                              │             │
  │                              ▼             ▼
  │                           ACTIVE       REJECTED
  │                                            │
  └────────────update() + register()──────────┘
```

## Security Considerations
- Only vendors can create and register templates
- JWT authentication required
- Ownership validation prevents unauthorized access
- Template locking (PENDING status) prevents editing during review

## Related Files
- Entity: `WorkshopTemplate.java`
- Enum: `WorkshopStatus.java`
- Service: `WorkshopTemplateService.java`, `WorkshopTemplateServiceImpl.java`
- Controller: `WorkshopController.java`
- DTOs: `CreateWorkshopTemplateRequest.java`, `WorkshopTemplateResponse.java`
