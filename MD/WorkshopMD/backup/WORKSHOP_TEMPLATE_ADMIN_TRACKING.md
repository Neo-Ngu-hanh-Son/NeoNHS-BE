# Workshop Template Admin Tracking Enhancement

## Overview
Added `rejectedBy` field to track which admin rejected a workshop template, providing complete audit trail for approval/rejection actions.

## Changes Made

### 1. Entity Changes
**File:** `WorkshopTemplate.java`

Added new field:
```java
private UUID rejectedBy;
```

This field stores the UUID of the admin user who rejected the template.

### 2. DTO Changes
**File:** `WorkshopTemplateResponse.java`

Added corresponding field in the response DTO:
```java
private UUID rejectedBy;
```

### 3. Service Layer Changes
**File:** `WorkshopTemplateServiceImpl.java`

#### Approve Method Updates:
- Sets `approvedBy` to the admin's user ID
- Sets `approvedAt` to current timestamp
- Clears `rejectReason` (previous rejection reason)
- Clears `rejectedBy` (previous rejection admin)

```java
template.setApprovedBy(admin.getId());
template.setApprovedAt(LocalDateTime.now());
template.setRejectReason(null);
template.setRejectedBy(null);
```

#### Reject Method Updates:
- Sets `rejectedBy` to the admin's user ID
- Sets `rejectReason` to the provided reason
- Clears `approvedBy` (previous approval admin)
- Clears `approvedAt` (previous approval timestamp)

```java
template.setRejectedBy(admin.getId());
template.setRejectReason(rejectReason);
template.setApprovedBy(null);
template.setApprovedAt(null);
```

#### Mapper Updates:
- Updated `mapToResponse()` to include `rejectedBy` field in the response

### 4. Database Migration
**File:** `add_rejected_by_column.sql`

SQL script to add the new column:
```sql
ALTER TABLE workshop_templates 
ADD COLUMN rejected_by BINARY(16) NULL COMMENT 'UUID of the admin who rejected this template';
```

## Database Migration Steps

1. **Backup your database** before running the migration
2. Run the migration script:
   ```sql
   SOURCE /path/to/add_rejected_by_column.sql;
   ```
   Or manually execute:
   ```sql
   ALTER TABLE workshop_templates 
   ADD COLUMN rejected_by BINARY(16) NULL COMMENT 'UUID of the admin who rejected this template';
   ```

3. Verify the column was added:
   ```sql
   DESCRIBE workshop_templates;
   ```

## API Behavior

### POST /api/admin/workshop-templates/{id}/approve
**What it does:**
- Validates admin credentials from JWT token
- Sets approval tracking fields:
  - `approvedBy` = admin's user ID
  - `approvedAt` = current timestamp
  - `status` = ACTIVE
- Clears rejection tracking fields:
  - `rejectReason` = null
  - `rejectedBy` = null

**Response includes:**
```json
{
  "id": "uuid",
  "status": "ACTIVE",
  "approvedBy": "admin-user-id",
  "approvedAt": "2026-03-01T10:00:00",
  "rejectedBy": null,
  "rejectReason": null
}
```

### POST /api/admin/workshop-templates/{id}/reject
**What it does:**
- Validates admin credentials from JWT token
- Sets rejection tracking fields:
  - `rejectedBy` = admin's user ID
  - `rejectReason` = provided reason text
  - `status` = REJECTED
- Clears approval tracking fields:
  - `approvedBy` = null
  - `approvedAt` = null

**Request body:**
```json
{
  "rejectReason": "Inappropriate content found in description"
}
```

**Response includes:**
```json
{
  "id": "uuid",
  "status": "REJECTED",
  "rejectedBy": "admin-user-id",
  "rejectReason": "Inappropriate content found in description",
  "approvedBy": null,
  "approvedAt": null
}
```

## Admin Identity Verification

Both APIs now properly track admin identity:

1. **Authentication**: Admin's JWT token is extracted from the request
2. **Authorization**: Admin role is verified by Spring Security
3. **Identity Extraction**: Admin's email is retrieved from `Principal.getName()`
4. **User Lookup**: Admin user entity is fetched from database using email
5. **ID Storage**: Admin's UUID is stored in either `approvedBy` or `rejectedBy`

This provides a complete audit trail showing:
- **Who** approved/rejected the template (via UUID)
- **When** the action was performed (via timestamp)
- **Why** it was rejected (via reason text)

## Testing

### Test Approve Flow:
1. Login as admin to get JWT token
2. Find a workshop template in PENDING status
3. Call `POST /api/admin/workshop-templates/{id}/approve`
4. Verify response includes `approvedBy` with your admin user ID
5. Verify `rejectedBy` is null

### Test Reject Flow:
1. Login as admin to get JWT token
2. Find a workshop template in PENDING status
3. Call `POST /api/admin/workshop-templates/{id}/reject` with a reason
4. Verify response includes `rejectedBy` with your admin user ID
5. Verify `approvedBy` is null

### Test Status Transitions:
```
DRAFT → (vendor submits) → PENDING
PENDING → (admin approves) → ACTIVE (approvedBy set, rejectedBy cleared)
PENDING → (admin rejects) → REJECTED (rejectedBy set, approvedBy cleared)
REJECTED → (vendor resubmits) → PENDING (keeps rejection data for reference)
PENDING → (admin approves) → ACTIVE (clears all rejection data)
```

## Benefits

1. **Complete Audit Trail**: Know exactly which admin performed each action
2. **Accountability**: Admins are accountable for their approval/rejection decisions
3. **Transparency**: Vendors can see which admin handled their template
4. **Compliance**: Meets audit requirements for tracking administrative actions
5. **Debugging**: Easier to troubleshoot issues by knowing who made decisions

## Notes

- Both `approvedBy` and `rejectedBy` are nullable UUID fields
- Only one should be set at a time (either approved OR rejected, not both)
- When status changes, the opposite field is cleared to maintain data integrity
- The admin's email comes from the JWT token (Principal), ensuring security
- The system looks up the full User entity to get the UUID for storage
