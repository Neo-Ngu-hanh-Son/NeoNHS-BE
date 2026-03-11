# Workshop Template Approve/Reject Implementation Summary

## Date: February 8, 2026

## Overview
Successfully implemented the Admin approval/rejection workflow for Workshop Templates, including proper tracking of approval metadata.

---

## Changes Made

### 1. New DTO Created
**File**: `src/main/java/fpt/project/NeoNHS/dto/request/workshop/RejectWorkshopTemplateRequest.java`
- Created request DTO for rejecting templates
- Contains `rejectReason` field with `@NotBlank` validation
- Used by admin when rejecting a workshop template

### 2. Updated Response DTO
**File**: `src/main/java/fpt/project/NeoNHS/dto/response/workshop/WorkshopTemplateResponse.java`
- Added `rejectReason` field (String)
- Added `approvedBy` field (UUID) - stores admin user ID
- Added `approvedAt` field (LocalDateTime) - stores approval timestamp
- These fields are now included in all workshop template responses

### 3. Updated Service Interface
**File**: `src/main/java/fpt/project/NeoNHS/service/WorkshopTemplateService.java`
- Added `approveWorkshopTemplate(String adminEmail, UUID id)` method
- Added `rejectWorkshopTemplate(String adminEmail, UUID id, String rejectReason)` method

### 4. Implemented Service Logic
**File**: `src/main/java/fpt/project/NeoNHS/service/impl/WorkshopTemplateServiceImpl.java`

**Changes**:
- Added `UserRepository` dependency for admin user lookup
- Imported `LocalDateTime` for timestamp handling

**New Methods**:

#### `approveWorkshopTemplate(String adminEmail, UUID id)`
- Validates admin user exists
- Validates template exists
- Checks template is in PENDING status
- Updates status to ACTIVE
- Sets `approvedBy` to admin user ID
- Sets `approvedAt` to current timestamp
- Clears any previous `rejectReason`
- Returns updated template response

#### `rejectWorkshopTemplate(String adminEmail, UUID id, String rejectReason)`
- Validates admin user exists
- Validates template exists
- Checks template is in PENDING status
- Validates reject reason is not empty
- Updates status to REJECTED
- Sets `rejectReason` with provided reason
- Clears `approvedBy` and `approvedAt` fields
- Returns updated template response

**Updated Mapper**:
- Modified `mapToResponse()` to include `rejectReason`, `approvedBy`, and `approvedAt` in response

### 5. Updated Controller
**File**: `src/main/java/fpt/project/NeoNHS/controller/WorkshopController.java`

**Added Import**:
- Imported `RejectWorkshopTemplateRequest` DTO

**New Endpoints**:

#### POST `/api/workshops/templates/{id}/approve`
- **Authorization**: `@PreAuthorize("hasRole('ADMIN')")`
- **Path Parameter**: `id` (UUID)
- **Description**: Approve a pending workshop template
- **Response**: 200 OK with approved template details
- **Error Cases**:
  - 400: Template not in PENDING status
  - 401: Unauthorized
  - 403: Not an admin
  - 404: Template not found
- **Swagger Documentation**: Comprehensive OpenAPI annotations

#### POST `/api/workshops/templates/{id}/reject`
- **Authorization**: `@PreAuthorize("hasRole('ADMIN')")`
- **Path Parameter**: `id` (UUID)
- **Request Body**: `RejectWorkshopTemplateRequest` with reject reason
- **Description**: Reject a pending workshop template with reason
- **Response**: 200 OK with rejected template details
- **Validation**: Reject reason is mandatory
- **Error Cases**:
  - 400: Template not in PENDING status OR missing reject reason
  - 401: Unauthorized
  - 403: Not an admin
  - 404: Template not found
- **Swagger Documentation**: Comprehensive OpenAPI annotations

---

## Database Schema

The `workshop_templates` table already has these fields (verified):
- `reject_reason` (TEXT) - stores rejection message
- `approved_by` (UUID) - stores admin user ID
- `approved_at` (TIMESTAMP) - stores approval timestamp

No database migration needed.

---

## Workflow

### Template Lifecycle

1. **DRAFT** (initial state)
   - Vendor creates template
   - Can be edited by vendor
   - Can be submitted for approval

2. **PENDING** (after submission)
   - Vendor submits via POST `/templates/{id}/register`
   - Cannot be edited
   - Awaiting admin review

3. **ACTIVE** (after approval)
   - Admin approves via POST `/templates/{id}/approve`
   - `approvedBy` and `approvedAt` are set
   - Template is live and available
   - Cannot be edited by vendor

4. **REJECTED** (after rejection)
   - Admin rejects via POST `/templates/{id}/reject`
   - `rejectReason` is set
   - Approval fields are cleared
   - Vendor can view rejection reason
   - Vendor can edit and resubmit

---

## Security

- Both endpoints require authenticated user (JWT token)
- Both endpoints are protected with `@PreAuthorize("hasRole('ADMIN')")`
- Only users with ADMIN role can access these endpoints
- Vendor ownership is NOT checked (admin can approve/reject any template)
- Admin user existence is validated before processing

---

## Validation

### Approve Endpoint
- Template must exist (404 if not)
- Template status must be PENDING (400 if not)
- Admin user must exist (404 if not)

### Reject Endpoint
- Template must exist (404 if not)
- Template status must be PENDING (400 if not)
- Admin user must exist (404 if not)
- Reject reason must not be blank (400 if empty)

---

## Testing

### Compilation Status
✅ **BUILD SUCCESS** - Project compiles without errors

### Documentation Created
- Created `API_WORKSHOP_APPROVE_REJECT.md` with comprehensive testing guide
- Includes test cases, error scenarios, and Postman examples
- Status flow diagram included

### Recommended Testing Steps
1. Create vendor account and verify
2. Create workshop template (DRAFT)
3. Submit for approval (PENDING)
4. Test approve endpoint as admin
5. Create another template and submit
6. Test reject endpoint as admin
7. Test vendor can update rejected template
8. Test vendor can resubmit
9. Test error cases (non-admin user, wrong status, etc.)

---

## API Endpoints Summary

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/api/workshops/templates/{id}/register` | VENDOR | Submit template for approval |
| POST | `/api/workshops/templates/{id}/approve` | ADMIN | Approve pending template |
| POST | `/api/workshops/templates/{id}/reject` | ADMIN | Reject pending template with reason |

---

## Files Modified/Created

### Created (1 file)
1. `src/main/java/fpt/project/NeoNHS/dto/request/workshop/RejectWorkshopTemplateRequest.java`
2. `API_WORKSHOP_APPROVE_REJECT.md` (testing guide)

### Modified (4 files)
1. `src/main/java/fpt/project/NeoNHS/dto/response/workshop/WorkshopTemplateResponse.java`
2. `src/main/java/fpt/project/NeoNHS/service/WorkshopTemplateService.java`
3. `src/main/java/fpt/project/NeoNHS/service/impl/WorkshopTemplateServiceImpl.java`
4. `src/main/java/fpt/project/NeoNHS/controller/WorkshopController.java`

---

## Next Steps

1. **Start the application** and test via Swagger UI
2. **Create test data**:
   - At least one admin user
   - At least one verified vendor
   - Workshop templates in different statuses
3. **Test all endpoints** using Postman or Swagger
4. **Verify database** changes after approve/reject operations
5. **Test frontend integration** when ready

---

## Notes

- Entity `WorkshopTemplate` already had the required fields, no entity changes needed
- All approval/rejection tracking is properly stored in database
- Frontend can display rejection reason to vendors
- Frontend can display approval info to admins/vendors
- Swagger documentation is comprehensive and ready for API consumers

---

## Compilation Result

```
[INFO] BUILD SUCCESS
[INFO] Total time:  8.486 s
```

✅ All changes compiled successfully with no errors.
