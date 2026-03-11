# Frontend Implementation Guide: Workshop Template CRUD

## Overview
This guide explains how to build the frontend UI for the Workshop Template CRUD functionality based on the backend API endpoints. The UI allows vendors to create, manage, and submit workshop templates for admin approval.

---

## 📋 Table of Contents
1. [API Endpoints Reference](#api-endpoints-reference)
2. [Data Models & Types](#data-models--types)
3. [Folder Structure](#folder-structure)
4. [Components to Build](#components-to-build)
5. [Pages to Build](#pages-to-build)
6. [API Service Layer](#api-service-layer)
7. [State Management](#state-management)
8. [Form Validation](#form-validation)
9. [User Flows](#user-flows)
10. [Important Business Rules](#important-business-rules)

---

## 🔌 API Endpoints Reference

### Workshop Template Endpoints

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/api/workshops/templates` | VENDOR | Create new template (status: DRAFT) |
| GET | `/api/workshops/templates/{id}` | PUBLIC | Get single template by ID |
| GET | `/api/workshops/templates` | ADMIN | Get all templates (paginated) |
| GET | `/api/workshops/templates/my` | VENDOR | Get vendor's own templates (paginated) |
| GET | `/api/workshops/templates/filter` | PUBLIC | Search/filter templates |
| PUT | `/api/workshops/templates/{id}` | VENDOR | Update template (DRAFT/REJECTED only) |
| POST | `/api/workshops/templates/{id}/register` | VENDOR | Submit for approval (DRAFT/REJECTED → PENDING) |
| POST | `/api/workshops/templates/{id}/approve` | ADMIN | Approve template (PENDING → ACTIVE) |
| POST | `/api/workshops/templates/{id}/reject` | ADMIN | Reject template (PENDING → REJECTED) |
| DELETE | `/api/workshops/templates/{id}` | VENDOR | Delete template (cannot delete ACTIVE) |

### WTag (Workshop Tag) Endpoints

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| GET | `/api/wtags` | PUBLIC | Get all tags (paginated) |
| GET | `/api/wtags/all` | PUBLIC | Get all tags (no pagination) |
| GET | `/api/wtags/{id}` | PUBLIC | Get single tag |
| POST | `/api/wtags` | ADMIN | Create new tag |
| PUT | `/api/wtags/{id}` | ADMIN | Update tag |
| DELETE | `/api/wtags/{id}` | ADMIN | Delete tag |

---

## 📦 Data Models & Types

### TypeScript Interfaces

```typescript
// Workshop Template Status
enum WorkshopStatus {
  DRAFT = "DRAFT",       // Created but not submitted
  PENDING = "PENDING",   // Submitted, awaiting approval
  ACTIVE = "ACTIVE",     // Approved and published
  REJECTED = "REJECTED"  // Rejected by admin
}

// Workshop Template Response
interface WorkshopTemplateResponse {
  id: string; // UUID
  name: string;
  shortDescription: string;
  fullDescription: string;
  estimatedDuration: number; // in minutes
  defaultPrice: number; // BigDecimal
  minParticipants: number;
  maxParticipants: number;
  status: WorkshopStatus;
  averageRating: number | null;
  totalReview: number;
  vendorId: string; // UUID
  vendorName: string;
  createdAt: string; // ISO DateTime
  updatedAt: string; // ISO DateTime
  
  // Approval tracking
  rejectReason: string | null;
  approvedBy: string | null; // UUID
  approvedAt: string | null; // ISO DateTime
  
  images: WorkshopImageResponse[];
  tags: WTagResponse[];
}

// Workshop Image
interface WorkshopImageResponse {
  id: string; // UUID
  imageUrl: string;
  isThumbnail: boolean;
}

// Workshop Tag
interface WTagResponse {
  id: string; // UUID
  name: string;
  description: string;
  tagColor: string; // hex color
  iconUrl: string | null;
}

// Create Template Request
interface CreateWorkshopTemplateRequest {
  name: string; // max 255 chars, required
  shortDescription: string; // max 500 chars, optional
  fullDescription: string; // optional
  estimatedDuration: number; // positive, required
  defaultPrice: number; // > 0, required
  minParticipants: number; // positive, required
  maxParticipants: number; // positive, required
  imageUrls: string[]; // at least 1 image required
  thumbnailIndex: number; // default 0, must be valid index
  tagIds: string[]; // UUID[], at least 1 required
}

// Update Template Request (all fields optional)
interface UpdateWorkshopTemplateRequest {
  name?: string;
  shortDescription?: string;
  fullDescription?: string;
  estimatedDuration?: number;
  defaultPrice?: number;
  minParticipants?: number;
  maxParticipants?: number;
  imageUrls?: string[];
  thumbnailIndex?: number;
  tagIds?: string[];
}

// Reject Request
interface RejectWorkshopTemplateRequest {
  rejectReason: string; // required
}

// Pagination Response
interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number; // current page
  first: boolean;
  last: boolean;
}

// API Response Wrapper
interface ApiResponse<T> {
  status: number;
  message: string;
  data: T;
}
```

---

## 📁 Folder Structure

```
src/
├── services/
│   ├── api/
│   │   ├── workshopTemplateApi.ts    # Workshop template API calls
│   │   └── wtagApi.ts                # WTag API calls
│   └── auth.ts                        # Auth service (JWT handling)
│
├── types/
│   ├── workshop.types.ts              # All workshop-related types
│   └── common.types.ts                # Shared types (ApiResponse, etc.)
│
├── components/
│   ├── workshop/
│   │   ├── WTagSelector.tsx           # Multi-select tag component
│   │   ├── ImageUploader.tsx          # Image URL manager with thumbnail selector
│   │   ├── WorkshopTemplateCard.tsx   # Card for template list
│   │   ├── TemplateStatusBadge.tsx    # Status badge component
│   │   ├── TemplateStatusTimeline.tsx # Visual timeline (DRAFT→PENDING→ACTIVE/REJECTED)
│   │   └── RejectionAlert.tsx         # Alert box for rejection reason
│   │
│   ├── common/
│   │   ├── Pagination.tsx             # Reusable pagination component
│   │   ├── ConfirmDialog.tsx          # Confirmation modal
│   │   └── LoadingSpinner.tsx         # Loading indicator
│   │
│   └── forms/
│       └── FormField.tsx              # Reusable form field wrapper
│
├── pages/
│   ├── vendor/
│   │   └── workshop-templates/
│   │       ├── index.tsx              # List of vendor's templates
│   │       ├── create.tsx             # Create new template
│   │       ├── [id]/
│   │       │   ├── edit.tsx           # Edit template
│   │       │   └── view.tsx           # View template details
│   │       └── components/
│   │           └── TemplateForm.tsx   # Shared form component
│   │
│   └── admin/
│       └── workshop-templates/
│           ├── index.tsx              # List all templates for approval
│           └── [id]/
│               └── review.tsx         # Review and approve/reject
│
├── hooks/
│   ├── useWorkshopTemplates.ts        # Custom hook for template operations
│   ├── useWTags.ts                    # Custom hook for tag operations
│   └── usePagination.ts               # Custom hook for pagination logic
│
└── utils/
    ├── validation.ts                  # Form validation helpers
    ├── formatters.ts                  # Format price, duration, dates
    └── constants.ts                   # App constants
```

---

## 🧩 Components to Build

### 1. WTagSelector Component
**Purpose:** Allow users to select multiple workshop tags/categories

**Props:**
```typescript
interface WTagSelectorProps {
  selectedTagIds: string[]; // Array of selected tag UUIDs
  onChange: (tagIds: string[]) => void;
  error?: string;
  disabled?: boolean;
}
```

**Features:**
- Fetch all tags from `/api/wtags/all`
- Display tags as selectable chips/buttons
- Show tag name, icon (if available), and background color
- Allow multi-selection (click to toggle)
- Show selected count
- Enforce minimum 1 tag selection
- Display validation error if no tags selected

**API Call:**
```typescript
GET /api/wtags/all
Response: WTagResponse[]
```

---

### 2. ImageUploader Component
**Purpose:** Manage multiple image URLs and select thumbnail

**Props:**
```typescript
interface ImageUploaderProps {
  imageUrls: string[];
  thumbnailIndex: number;
  onChange: (imageUrls: string[], thumbnailIndex: number) => void;
  error?: string;
  disabled?: boolean;
}
```

**Features:**
- Input field to add image URLs (one at a time or paste multiple)
- Display image previews in a grid
- Drag and drop to reorder images
- Radio button on each image to select as thumbnail
- Delete button on each image
- Enforce minimum 1 image requirement
- Validate image URLs (basic format check)
- Display validation error

**Layout:**
```
[+Add Image URL]  [Input field] [Add Button]

┌──────────┐ ┌──────────┐ ┌──────────┐
│  Image 1 │ │  Image 2 │ │  Image 3 │
│ [📷]     │ │          │ │          │
│ (•) Thumb│ │ ( ) Thumb│ │ ( ) Thumb│
│   [X]    │ │   [X]    │ │   [X]    │
└──────────┘ └──────────┘ └──────────┘
```

---

### 3. WorkshopTemplateCard Component
**Purpose:** Display template summary in list view

**Props:**
```typescript
interface WorkshopTemplateCardProps {
  template: WorkshopTemplateResponse;
  onEdit?: () => void;
  onDelete?: () => void;
  onSubmit?: () => void;
  onView?: () => void;
}
```

**Display:**
- Thumbnail image
- Template name
- Short description (truncated)
- Status badge
- Price (formatted with $)
- Duration (formatted as "90 min" or "1h 30m")
- Tags (small chips)
- Participant range (e.g., "5-20 people")
- Created/Updated date
- Action buttons based on status:
  - DRAFT: [Edit] [Delete] [Submit for Approval]
  - PENDING: [View] (locked, no actions)
  - ACTIVE: [View]
  - REJECTED: [Edit] [Delete] [View Reason]

---

### 4. TemplateStatusBadge Component
**Purpose:** Visual indicator of template status

**Props:**
```typescript
interface TemplateStatusBadgeProps {
  status: WorkshopStatus;
  size?: 'sm' | 'md' | 'lg';
}
```

**Status Colors (Suggestions):**
- DRAFT: Gray/Neutral (⚪)
- PENDING: Yellow/Warning (🟡)
- ACTIVE: Green/Success (🟢)
- REJECTED: Red/Error (🔴)

---

### 5. TemplateStatusTimeline Component
**Purpose:** Show template lifecycle visually

**Props:**
```typescript
interface TemplateStatusTimelineProps {
  template: WorkshopTemplateResponse;
}
```

**Display:**
```
Created → Submitted → Approved/Rejected
  ✓         ✓            ✓
 [Date]    [Date]      [Date]
```

- Show completed steps with checkmarks
- Highlight current status
- Display timestamps for each stage
- Show rejection reason if applicable

---

### 6. RejectionAlert Component
**Purpose:** Display rejection reason prominently

**Props:**
```typescript
interface RejectionAlertProps {
  rejectReason: string;
  onDismiss?: () => void;
}
```

**Display:**
- Alert box with warning/error styling
- Icon (⚠️)
- Title: "Your template was rejected"
- Rejection reason text
- Suggestion: "Please address the issues and resubmit"

---

## 📄 Pages to Build

### 1. Vendor Templates List Page
**Path:** `/vendor/workshop-templates`

**Features:**
- Display all vendor's templates (paginated)
- Filter by status (dropdown: All, Draft, Pending, Active, Rejected)
- Search by name
- Sort by: Created Date, Updated Date, Name, Price
- Pagination controls (page size: 10, 25, 50)
- "Create New Template" button
- Display templates as grid/list of cards

**API Call:**
```typescript
GET /api/workshops/templates/my?page=0&size=10&sortBy=createdAt&sortDir=desc
Response: PageResponse<WorkshopTemplateResponse>
```

**Status Filter Implementation:**
- Client-side filtering OR
- Fetch all and filter OR
- Use filter endpoint: `/api/workshops/templates/filter?vendorId={vendorId}&status={status}`

---

### 2. Create Template Page
**Path:** `/vendor/workshop-templates/create`

**Form Sections:**

#### A. Basic Information
- **Name*** (text, max 255 chars)
- **Short Description** (textarea, max 500 chars)
- **Full Description** (rich text editor or textarea)

#### B. Pricing & Duration
- **Price*** (number input, min 0.01, format: $XX.XX)
- **Estimated Duration*** (number input in minutes, with hour/minute helper)
  - Example: Input "90" → Display "1 hour 30 minutes"

#### C. Participants
- **Min Participants*** (number, min 1)
- **Max Participants*** (number, must be ≥ min)
  - Show validation: "Max must be greater than or equal to Min"

#### D. Categories/Tags
- **Select Tags*** (WTagSelector component)
  - Display: "Select at least 1 category"

#### E. Images
- **Workshop Images*** (ImageUploader component)
  - Display: "Add at least 1 image"
  - Show which image is thumbnail

#### F. Actions
- **[Save as Draft]** button → POST to create endpoint, status = DRAFT
- **[Cancel]** button → Navigate back to list

**API Call:**
```typescript
POST /api/workshops/templates
Body: CreateWorkshopTemplateRequest
Response: WorkshopTemplateResponse (status: DRAFT)
```

**Validation:**
- Required fields must be filled
- Min ≤ Max participants
- Price > 0
- Duration > 0
- At least 1 tag
- At least 1 image
- Thumbnail index valid

---

### 3. Edit Template Page
**Path:** `/vendor/workshop-templates/{id}/edit`

**Same form as Create page, but:**
- Load existing template data
- Only allow editing if status is DRAFT or REJECTED
- Show rejection reason at top if status = REJECTED
- Display status timeline
- Actions:
  - **[Save Changes]** → PUT to update endpoint
  - **[Submit for Approval]** → POST to register endpoint (if ready)
  - **[Cancel]** → Navigate back

**API Calls:**
```typescript
// Load template
GET /api/workshops/templates/{id}
Response: WorkshopTemplateResponse

// Update template
PUT /api/workshops/templates/{id}
Body: UpdateWorkshopTemplateRequest
Response: WorkshopTemplateResponse

// Submit for approval
POST /api/workshops/templates/{id}/register
Response: WorkshopTemplateResponse (status: PENDING)
```

**Conditional Display:**
- If status = PENDING or ACTIVE:
  - Show read-only view
  - Hide edit/delete buttons
  - Show message: "This template is locked for editing"

---

### 4. View Template Details Page
**Path:** `/vendor/workshop-templates/{id}/view`

**Display (Read-Only):**
- All template information
- Status timeline
- Thumbnail and image gallery
- Tags as chips
- Price, duration, participants
- Descriptions
- Rejection reason (if applicable)
- Approval info (approvedBy, approvedAt if status = ACTIVE)

**Actions based on status:**
- DRAFT: [Edit] [Delete] [Submit for Approval]
- PENDING: [Cancel Submission?] or just view
- ACTIVE: [View] only
- REJECTED: [Edit] [Delete]

**API Call:**
```typescript
GET /api/workshops/templates/{id}
Response: WorkshopTemplateResponse
```

---

### 5. Admin Review Page (Bonus)
**Path:** `/admin/workshop-templates/{id}/review`

**For Admin Role:**
- Display template details (same as view page)
- Show vendor information
- Show submission date
- Actions:
  - **[Approve]** → POST to approve endpoint
  - **[Reject]** → Show rejection reason modal → POST to reject endpoint
  - **[Cancel]**

**API Calls:**
```typescript
// Approve
POST /api/workshops/templates/{id}/approve
Response: WorkshopTemplateResponse (status: ACTIVE)

// Reject
POST /api/workshops/templates/{id}/reject
Body: { rejectReason: string }
Response: WorkshopTemplateResponse (status: REJECTED)
```

---

## 🔧 API Service Layer

### workshopTemplateApi.ts

```typescript
import axios from 'axios';
import { CreateWorkshopTemplateRequest, UpdateWorkshopTemplateRequest, WorkshopTemplateResponse, PageResponse, ApiResponse } from '../types';

const API_BASE = '/api/workshops/templates';

export const workshopTemplateApi = {
  // Create template
  create: async (data: CreateWorkshopTemplateRequest) => {
    const response = await axios.post<ApiResponse<WorkshopTemplateResponse>>(API_BASE, data);
    return response.data.data;
  },

  // Get single template
  getById: async (id: string) => {
    const response = await axios.get<ApiResponse<WorkshopTemplateResponse>>(`${API_BASE}/${id}`);
    return response.data.data;
  },

  // Get vendor's templates (paginated)
  getMyTemplates: async (page: number, size: number, sortBy: string, sortDir: string) => {
    const response = await axios.get<ApiResponse<PageResponse<WorkshopTemplateResponse>>>(
      `${API_BASE}/my`,
      { params: { page, size, sortBy, sortDir } }
    );
    return response.data.data;
  },

  // Update template
  update: async (id: string, data: UpdateWorkshopTemplateRequest) => {
    const response = await axios.put<ApiResponse<WorkshopTemplateResponse>>(`${API_BASE}/${id}`, data);
    return response.data.data;
  },

  // Submit for approval
  register: async (id: string) => {
    const response = await axios.post<ApiResponse<WorkshopTemplateResponse>>(`${API_BASE}/${id}/register`);
    return response.data.data;
  },

  // Delete template
  delete: async (id: string) => {
    await axios.delete(`${API_BASE}/${id}`);
  },

  // Admin: Approve
  approve: async (id: string) => {
    const response = await axios.post<ApiResponse<WorkshopTemplateResponse>>(`${API_BASE}/${id}/approve`);
    return response.data.data;
  },

  // Admin: Reject
  reject: async (id: string, rejectReason: string) => {
    const response = await axios.post<ApiResponse<WorkshopTemplateResponse>>(
      `${API_BASE}/${id}/reject`,
      { rejectReason }
    );
    return response.data.data;
  },

  // Search/Filter
  filter: async (filters: {
    keyword?: string;
    status?: string;
    vendorId?: string;
    tagId?: string;
    minPrice?: number;
    maxPrice?: number;
    minDuration?: number;
    maxDuration?: number;
    minRating?: number;
  }) => {
    const response = await axios.get<ApiResponse<WorkshopTemplateResponse[]>>(
      `${API_BASE}/filter`,
      { params: filters }
    );
    return response.data.data;
  }
};
```

### wtagApi.ts

```typescript
import axios from 'axios';
import { WTagResponse, ApiResponse } from '../types';

const API_BASE = '/api/wtags';

export const wtagApi = {
  // Get all tags (no pagination)
  getAll: async () => {
    const response = await axios.get<ApiResponse<WTagResponse[]>>(`${API_BASE}/all`);
    return response.data.data;
  },

  // Get paginated tags
  getPaginated: async (page: number, size: number, sortBy: string, sortDir: string) => {
    const response = await axios.get<ApiResponse<PageResponse<WTagResponse>>>(
      API_BASE,
      { params: { page, size, sortBy, sortDir } }
    );
    return response.data.data;
  }
};
```

---

## 🎯 State Management

### Option 1: React Context + Hooks
Create a context for workshop templates:

```typescript
// contexts/WorkshopTemplateContext.tsx
interface WorkshopTemplateContextType {
  templates: WorkshopTemplateResponse[];
  loading: boolean;
  error: string | null;
  fetchMyTemplates: () => Promise<void>;
  createTemplate: (data: CreateWorkshopTemplateRequest) => Promise<WorkshopTemplateResponse>;
  updateTemplate: (id: string, data: UpdateWorkshopTemplateRequest) => Promise<WorkshopTemplateResponse>;
  deleteTemplate: (id: string) => Promise<void>;
  registerTemplate: (id: string) => Promise<WorkshopTemplateResponse>;
}
```

### Option 2: Redux Toolkit
Create slices for templates and tags.

### Option 3: React Query / TanStack Query (Recommended)
Excellent for API data fetching with caching, refetching, and optimistic updates.

```typescript
// hooks/useWorkshopTemplates.ts
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { workshopTemplateApi } from '../services/api/workshopTemplateApi';

export const useWorkshopTemplates = (page: number, size: number) => {
  return useQuery({
    queryKey: ['workshopTemplates', 'my', page, size],
    queryFn: () => workshopTemplateApi.getMyTemplates(page, size, 'createdAt', 'desc')
  });
};

export const useCreateTemplate = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: workshopTemplateApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['workshopTemplates'] });
    }
  });
};

export const useRegisterTemplate = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => workshopTemplateApi.register(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['workshopTemplates'] });
    }
  });
};
```

---

## ✅ Form Validation

### Validation Rules

```typescript
// utils/validation.ts
import * as yup from 'yup'; // or Zod

export const workshopTemplateSchema = yup.object({
  name: yup.string()
    .required('Workshop name is required')
    .max(255, 'Name must not exceed 255 characters'),
  
  shortDescription: yup.string()
    .max(500, 'Short description must not exceed 500 characters')
    .optional(),
  
  fullDescription: yup.string().optional(),
  
  estimatedDuration: yup.number()
    .required('Duration is required')
    .positive('Duration must be positive')
    .integer('Duration must be a whole number'),
  
  defaultPrice: yup.number()
    .required('Price is required')
    .positive('Price must be greater than 0')
    .min(0.01, 'Price must be at least $0.01'),
  
  minParticipants: yup.number()
    .required('Minimum participants is required')
    .positive('Must be at least 1')
    .integer('Must be a whole number'),
  
  maxParticipants: yup.number()
    .required('Maximum participants is required')
    .positive('Must be at least 1')
    .integer('Must be a whole number')
    .test('max-greater-than-min', 'Max must be >= Min', function(value) {
      return value >= this.parent.minParticipants;
    }),
  
  imageUrls: yup.array()
    .of(yup.string().url('Must be a valid URL'))
    .min(1, 'At least one image is required')
    .required('Images are required'),
  
  thumbnailIndex: yup.number()
    .min(0, 'Invalid thumbnail index')
    .required('Thumbnail selection is required'),
  
  tagIds: yup.array()
    .of(yup.string().uuid('Invalid tag ID'))
    .min(1, 'At least one tag is required')
    .required('Tags are required')
});
```

### Using with React Hook Form

```typescript
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';

const TemplateForm = () => {
  const { register, handleSubmit, formState: { errors } } = useForm({
    resolver: yupResolver(workshopTemplateSchema)
  });

  const onSubmit = (data) => {
    // Handle form submission
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      {/* Form fields */}
    </form>
  );
};
```

---

## 🔄 User Flows

### Flow 1: Create and Submit Template

```
1. Vendor clicks "Create New Template" button
   ↓
2. Fill out form (all required fields)
   ↓
3. Add at least 1 image, select thumbnail
   ↓
4. Select at least 1 tag/category
   ↓
5. Click "Save as Draft"
   → API: POST /api/workshops/templates
   → Status: DRAFT
   ↓
6. Redirected to template list or edit page
   ↓
7. Vendor reviews template
   ↓
8. Click "Submit for Approval"
   → Show confirmation dialog:
     "Once submitted, you cannot edit until approval. Proceed?"
   → User confirms
   ↓
9. API: POST /api/workshops/templates/{id}/register
   → Status: PENDING
   ↓
10. Show success message:
    "Template submitted successfully. Please wait for admin approval."
```

### Flow 2: Handle Rejection

```
1. Admin rejects template with reason
   → Status: REJECTED
   ↓
2. Vendor views template list
   → Sees REJECTED badge (red)
   ↓
3. Clicks "View" or "Edit"
   ↓
4. Sees rejection alert at top:
   "⚠️ Your template was rejected
   Reason: [Admin's reason text]
   Please address the issues and resubmit."
   ↓
5. Vendor clicks "Edit"
   ↓
6. Makes changes to address issues
   ↓
7. Clicks "Save Changes"
   → API: PUT /api/workshops/templates/{id}
   → Status: Still REJECTED
   ↓
8. Clicks "Submit for Approval" again
   → API: POST /api/workshops/templates/{id}/register
   → Status: PENDING
   ↓
9. Admin reviews again → Approves or Rejects
```

### Flow 3: Admin Approval Process

```
1. Admin navigates to pending templates
   → API: GET /api/workshops/templates (Admin only)
   → Filter by status=PENDING
   ↓
2. Admin clicks template to review
   ↓
3. Reviews all details:
   - Name, descriptions
   - Images (quality, appropriateness)
   - Pricing
   - Tags/categories
   - Duration
   ↓
4a. If acceptable:
    → Click "Approve"
    → API: POST /api/workshops/templates/{id}/approve
    → Status: ACTIVE
    → Vendor can now create workshop sessions
    
4b. If not acceptable:
    → Click "Reject"
    → Modal opens for rejection reason
    → Admin types reason (e.g., "Images are low quality")
    → Click "Submit Rejection"
    → API: POST /api/workshops/templates/{id}/reject
    → Status: REJECTED
    → Vendor receives notification
```

---

## 📌 Important Business Rules

### 1. Status-Based Permissions

| Status | Can Edit? | Can Delete? | Can Submit? | Notes |
|--------|-----------|-------------|-------------|-------|
| DRAFT | ✅ Yes | ✅ Yes | ✅ Yes | Full control |
| PENDING | ❌ No | ✅ Yes* | ❌ No | Locked for editing, can withdraw |
| ACTIVE | ❌ No | ❌ No | ❌ No | Published, read-only |
| REJECTED | ✅ Yes | ✅ Yes | ✅ Yes (after edit) | Can fix and resubmit |

*Deleting PENDING = withdrawing submission

### 2. Validation Requirements

**Before Creating:**
- Vendor must be verified (backend checks this)
- All required fields must be filled
- At least 1 image
- At least 1 tag

**Before Submitting for Approval:**
- Template must be in DRAFT or REJECTED status
- All required fields complete
- System validates completeness

**Before Updating:**
- Template must be in DRAFT or REJECTED
- Vendor must be the owner

**Before Deleting:**
- Template must NOT be ACTIVE
- Vendor must be the owner

### 3. Data Constraints

- **Name:** Max 255 characters
- **Short Description:** Max 500 characters
- **Price:** Must be > 0, support decimals (e.g., 49.99)
- **Duration:** Positive integer (minutes)
- **Participants:** minParticipants ≤ maxParticipants
- **Images:** At least 1, thumbnailIndex must be valid
- **Tags:** At least 1 tag required

### 4. Pagination Defaults

- **Default page:** 1 (0-indexed in backend, so send `page=0`)
- **Default size:** 10
- **Default sort:** createdAt, descending (newest first)

---

## 🎨 UI/UX Recommendations

### Status Indicators
Use consistent color coding:
- **DRAFT:** Gray/Neutral (#6B7280)
- **PENDING:** Yellow/Amber (#F59E0B)
- **ACTIVE:** Green (#10B981)
- **REJECTED:** Red (#EF4444)

### Confirmation Dialogs
Always confirm destructive actions:
- Delete template
- Submit for approval (locks editing)

### Empty States
- No templates yet: "Create your first workshop template"
- No results from filter: "No templates match your criteria"

### Loading States
- Show skeleton loaders while fetching
- Disable buttons during API calls
- Show loading spinner in action buttons

### Error Handling
- Display API errors clearly
- Network errors: "Connection failed. Please try again."
- Validation errors: Show inline under fields
- 403 Forbidden: "You don't have permission to perform this action"
- 404 Not Found: "Template not found"

### Success Feedback
- Toast notifications for actions:
  - "Template created successfully"
  - "Template updated"
  - "Template submitted for approval"
  - "Template deleted"

### Responsive Design
- Mobile: Stack form fields vertically
- Tablet: 2-column grid for templates
- Desktop: 3-4 column grid for templates

---

## 🚀 Implementation Steps

### Phase 1: Setup (Day 1)
1. Set up API service layer
2. Define TypeScript types/interfaces
3. Set up React Query or state management
4. Create folder structure

### Phase 2: Core Components (Day 2-3)
1. Build WTagSelector component
2. Build ImageUploader component
3. Build TemplateStatusBadge component
4. Build RejectionAlert component
5. Build WorkshopTemplateCard component

### Phase 3: Forms (Day 4-5)
1. Create TemplateForm component (shared for create/edit)
2. Set up form validation with React Hook Form + Yup
3. Test form submission
4. Add error handling

### Phase 4: Pages (Day 6-8)
1. Build Template List page (/my)
2. Build Create Template page
3. Build Edit Template page
4. Build View Template page
5. Add pagination
6. Add filtering

### Phase 5: Approval Flow (Day 9-10)
1. Add submit for approval confirmation
2. Build rejection display
3. Add status timeline component
4. Test full workflow: Create → Submit → Reject → Edit → Resubmit

### Phase 6: Polish (Day 11-12)
1. Add loading states
2. Add error handling
3. Add success feedback (toasts)
4. Add empty states
5. Responsive design
6. Testing

---

## 🧪 Testing Checklist

### Create Template
- [ ] All required fields validated
- [ ] Can add multiple images
- [ ] Can select thumbnail
- [ ] Can select multiple tags
- [ ] Min <= Max participants validated
- [ ] Price > 0 validated
- [ ] Duration > 0 validated
- [ ] Creates with DRAFT status
- [ ] Redirects after creation

### Edit Template
- [ ] Loads existing data correctly
- [ ] Can only edit DRAFT/REJECTED
- [ ] Shows rejection reason if REJECTED
- [ ] Saves changes correctly
- [ ] PENDING/ACTIVE templates read-only

### Submit for Approval
- [ ] Shows confirmation dialog
- [ ] Changes status to PENDING
- [ ] Template becomes read-only after submission
- [ ] Shows success message

### Delete Template
- [ ] Shows confirmation dialog
- [ ] Cannot delete ACTIVE templates
- [ ] Can delete DRAFT/PENDING/REJECTED
- [ ] Removes from list after deletion

### List View
- [ ] Shows all vendor's templates
- [ ] Pagination works correctly
- [ ] Status filter works
- [ ] Search works
- [ ] Sort works
- [ ] Action buttons show based on status

### Status-Based UI
- [ ] DRAFT: Shows Edit, Delete, Submit buttons
- [ ] PENDING: Shows View only (locked)
- [ ] ACTIVE: Shows View only
- [ ] REJECTED: Shows Edit, Delete, View Reason

---

## 📝 Sample API Calls

### Example 1: Create Template
```http
POST /api/workshops/templates
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

{
  "name": "Beginner Yoga Workshop",
  "shortDescription": "Learn basic yoga poses and breathing techniques",
  "fullDescription": "This 90-minute workshop is perfect for beginners...",
  "estimatedDuration": 90,
  "defaultPrice": 49.99,
  "minParticipants": 5,
  "maxParticipants": 20,
  "imageUrls": [
    "https://example.com/images/yoga1.jpg",
    "https://example.com/images/yoga2.jpg"
  ],
  "thumbnailIndex": 0,
  "tagIds": [
    "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "b2c3d4e5-f6a7-8901-bcde-f12345678901"
  ]
}
```

### Example 2: Get My Templates (Paginated)
```http
GET /api/workshops/templates/my?page=0&size=10&sortBy=createdAt&sortDir=desc
Authorization: Bearer {JWT_TOKEN}
```

### Example 3: Submit for Approval
```http
POST /api/workshops/templates/{id}/register
Authorization: Bearer {JWT_TOKEN}
```

### Example 4: Reject Template (Admin)
```http
POST /api/workshops/templates/{id}/reject
Authorization: Bearer {ADMIN_JWT_TOKEN}
Content-Type: application/json

{
  "rejectReason": "The images provided are low quality. Please upload higher resolution images showing the workshop activities clearly."
}
```

---

## 🔗 Additional Resources

### Pagination Implementation
- Backend uses 0-based page indexing
- Frontend might use 1-based for display (convert when calling API)
- Example: User sees "Page 1" → Send `page=0` to API

### Date Formatting
```typescript
// utils/formatters.ts
export const formatDate = (isoDate: string) => {
  return new Date(isoDate).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  });
};

// "2026-02-11T10:30:00Z" → "Feb 11, 2026"
```

### Price Formatting
```typescript
export const formatPrice = (price: number) => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD'
  }).format(price);
};

// 49.99 → "$49.99"
```

### Duration Formatting
```typescript
export const formatDuration = (minutes: number) => {
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  
  if (hours === 0) return `${mins} min`;
  if (mins === 0) return `${hours} hr`;
  return `${hours}h ${mins}m`;
};

// 90 → "1h 30m"
// 60 → "1 hr"
// 45 → "45 min"
```

---

## ✨ Summary

This guide provides a complete blueprint for implementing the Workshop Template CRUD functionality in your frontend. The key points to remember:

1. **Status workflow is critical:** DRAFT → PENDING → ACTIVE/REJECTED
2. **Permissions are status-based:** Only edit DRAFT/REJECTED
3. **Validation is important:** Enforce all backend constraints in frontend too
4. **User feedback is essential:** Confirmations, success messages, error handling
5. **Pagination:** Handle page conversion (0-based vs 1-based)

Build components incrementally, test each piece thoroughly, and ensure the user experience flows smoothly through the approval workflow.

Good luck with your implementation! 🚀
