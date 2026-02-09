# Workshop Template Registration - API Testing Guide

## Prerequisites
1. Running NeoNHS backend server (default: http://localhost:8080)
2. Valid JWT token for a vendor account
3. Existing workshop template in DRAFT or REJECTED status

## Test Scenarios

### Scenario 1: Successful Template Registration (Happy Path)

#### Step 1: Create a Workshop Template (DRAFT)
```http
POST http://localhost:8080/api/workshops/templates
Authorization: Bearer <VENDOR_JWT_TOKEN>
Content-Type: application/json

{
  "name": "Ceramic Workshop for Beginners",
  "shortDescription": "Learn the basics of pottery and ceramic making",
  "fullDescription": "A comprehensive 3-hour workshop where you'll learn pottery basics, wheel throwing, hand-building techniques, and glazing. Perfect for beginners with no prior experience.",
  "estimatedDuration": 180,
  "defaultPrice": 150.00,
  "minParticipants": 5,
  "maxParticipants": 15,
  "imageUrls": [
    "https://example.com/images/ceramic-workshop-1.jpg",
    "https://example.com/images/ceramic-workshop-2.jpg"
  ],
  "tagIds": [
    "tag-uuid-1",
    "tag-uuid-2"
  ]
}
```

**Expected Response (201 Created):**
```json
{
  "success": true,
  "message": "Workshop template created successfully",
  "data": {
    "id": "newly-created-template-uuid",
    "name": "Ceramic Workshop for Beginners",
    "status": "DRAFT",
    ...
  }
}
```

#### Step 2: Register/Submit the Template for Approval
```http
POST http://localhost:8080/api/workshops/templates/{template-id}/register
Authorization: Bearer <VENDOR_JWT_TOKEN>
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Template submitted successfully. Please wait for admin approval.",
  "data": {
    "id": "template-uuid",
    "name": "Ceramic Workshop for Beginners",
    "status": "PENDING",
    ...
  }
}
```

---

### Scenario 2: Register Incomplete Template (Validation Error)

#### Create Template with Missing Fields
```http
POST http://localhost:8080/api/workshops/templates
Authorization: Bearer <VENDOR_JWT_TOKEN>
Content-Type: application/json

{
  "name": "Incomplete Workshop",
  "shortDescription": "Short desc only",
  "defaultPrice": 100.00,
  "estimatedDuration": 60
  // Missing: fullDescription, minParticipants, maxParticipants, imageUrls, tagIds
}
```

Template created with status DRAFT.

#### Try to Register Incomplete Template
```http
POST http://localhost:8080/api/workshops/templates/{template-id}/register
Authorization: Bearer <VENDOR_JWT_TOKEN>
```

**Expected Response (400 Bad Request):**
```json
{
  "success": false,
  "message": "Cannot submit incomplete template. Missing required fields: Full Description, Minimum Participants, Maximum Participants, Image (at least one required), Category/Tag (at least one required)"
}
```

---

### Scenario 3: Try to Register Already Pending Template

#### First Registration
```http
POST http://localhost:8080/api/workshops/templates/{template-id}/register
Authorization: Bearer <VENDOR_JWT_TOKEN>
```
✅ Success - Status changed to PENDING

#### Second Registration Attempt (Same Template)
```http
POST http://localhost:8080/api/workshops/templates/{template-id}/register
Authorization: Bearer <VENDOR_JWT_TOKEN>
```

**Expected Response (400 Bad Request):**
```json
{
  "success": false,
  "message": "Only templates with 'Draft' or 'Rejected' status can be submitted for approval"
}
```

---

### Scenario 4: Try to Update Pending Template (Should Fail)

```http
PUT http://localhost:8080/api/workshops/templates/{pending-template-id}
Authorization: Bearer <VENDOR_JWT_TOKEN>
Content-Type: application/json

{
  "name": "Updated Name",
  "shortDescription": "Updated description"
}
```

**Expected Response (400 Bad Request):**
```json
{
  "success": false,
  "message": "Cannot update a template that is pending approval. Please wait for admin review."
}
```

---

### Scenario 5: Resubmit After Rejection

#### Assumption: Admin rejected the template
Template status is now REJECTED with a reason.

#### Step 1: Update the Rejected Template
```http
PUT http://localhost:8080/api/workshops/templates/{rejected-template-id}
Authorization: Bearer <VENDOR_JWT_TOKEN>
Content-Type: application/json

{
  "name": "Improved Ceramic Workshop",
  "fullDescription": "Enhanced description addressing admin feedback..."
}
```

✅ Update successful (allowed for REJECTED status)

#### Step 2: Register Again
```http
POST http://localhost:8080/api/workshops/templates/{rejected-template-id}/register
Authorization: Bearer <VENDOR_JWT_TOKEN>
```

**Expected Response (200 OK):**
```json
{
  "success": true,
  "message": "Template submitted successfully. Please wait for admin approval.",
  "data": {
    "id": "template-uuid",
    "status": "PENDING",
    "rejectReason": null,
    ...
  }
}
```

---

### Scenario 6: Unauthorized Access

#### Try to Register Another Vendor's Template
```http
POST http://localhost:8080/api/workshops/templates/{other-vendor-template-id}/register
Authorization: Bearer <VENDOR_JWT_TOKEN>
```

**Expected Response (400 Bad Request):**
```json
{
  "success": false,
  "message": "You do not have permission to submit this workshop template"
}
```

---

### Scenario 7: No Authentication

```http
POST http://localhost:8080/api/workshops/templates/{template-id}/register
# No Authorization header
```

**Expected Response (401 Unauthorized):**
```json
{
  "success": false,
  "message": "Unauthorized"
}
```

---

### Scenario 8: Wrong Role (Not a Vendor)

```http
POST http://localhost:8080/api/workshops/templates/{template-id}/register
Authorization: Bearer <STUDENT_OR_ADMIN_JWT_TOKEN>
```

**Expected Response (403 Forbidden):**
```json
{
  "success": false,
  "message": "Access Denied"
}
```

---

## Complete Workflow Test

### 1. Vendor Creates Draft Template
```bash
POST /api/workshops/templates
→ Status: DRAFT
```

### 2. Vendor Can Edit Draft
```bash
PUT /api/workshops/templates/{id}
→ Status: DRAFT (still)
```

### 3. Vendor Submits for Approval
```bash
POST /api/workshops/templates/{id}/register
→ Status: PENDING
```

### 4. Vendor Cannot Edit Pending Template
```bash
PUT /api/workshops/templates/{id}
→ Error: "Cannot update a template that is pending approval"
```

### 5a. Admin Approves (separate endpoint, not implemented here)
```
Status: ACTIVE
Vendor cannot edit anymore
```

### 5b. Admin Rejects (separate endpoint, not implemented here)
```
Status: REJECTED
Rejection reason provided
```

### 6. Vendor Updates Rejected Template
```bash
PUT /api/workshops/templates/{id}
→ Status: REJECTED (still)
```

### 7. Vendor Resubmits
```bash
POST /api/workshops/templates/{id}/register
→ Status: PENDING
→ Rejection reason cleared
```

---

## cURL Examples

### Create Workshop Template
```bash
curl -X POST "http://localhost:8080/api/workshops/templates" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Photography Basics Workshop",
    "shortDescription": "Learn photography fundamentals",
    "fullDescription": "A detailed 4-hour workshop covering camera settings, composition, lighting, and post-processing.",
    "estimatedDuration": 240,
    "defaultPrice": 200.00,
    "minParticipants": 4,
    "maxParticipants": 12,
    "imageUrls": ["https://example.com/photo1.jpg"],
    "tagIds": ["tag-uuid-1"]
  }'
```

### Register Template
```bash
curl -X POST "http://localhost:8080/api/workshops/templates/YOUR_TEMPLATE_ID/register" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Get Template Details
```bash
curl -X GET "http://localhost:8080/api/workshops/templates/YOUR_TEMPLATE_ID" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Notes

1. **Template ID**: Replace `{template-id}` or `YOUR_TEMPLATE_ID` with actual UUID
2. **JWT Token**: Replace `YOUR_JWT_TOKEN` with actual vendor token
3. **Tag IDs**: Use valid tag UUIDs from your database
4. **Image URLs**: Use valid, accessible image URLs
5. **Minimum Fields**: All fields shown in Scenario 1 are required for registration

## Status Flow Reference

```
DRAFT → (register) → PENDING → (admin approve) → ACTIVE
  ↑                      ↓
  └──── (admin reject) ──┘
           REJECTED
```
