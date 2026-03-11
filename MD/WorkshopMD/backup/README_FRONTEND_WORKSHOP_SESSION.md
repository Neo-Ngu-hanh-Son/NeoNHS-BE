# Frontend Implementation Guide: Workshop Session Calendar & Management

## Overview
This guide explains how to build the frontend UI for Workshop Session management with an **interactive calendar view** for vendors. Unlike templates (which are blueprints), sessions are actual scheduled workshops that users can book.

**Key Concept:**
- **Template** = Workshop blueprint (yoga, painting, cooking, etc.)
- **Session** = Scheduled instance of a template (e.g., "Yoga on Feb 20, 2026 at 10:00 AM")

---

## 📋 Table of Contents
1. [API Endpoints Reference](#api-endpoints-reference)
2. [Data Models & Types](#data-models--types)
3. [Folder Structure](#folder-structure)
4. [Calendar View Architecture](#calendar-view-architecture)
5. [Components to Build](#components-to-build)
6. [Pages to Build](#pages-to-build)
7. [Calendar Implementation Guide](#calendar-implementation-guide)
8. [API Service Layer](#api-service-layer)
9. [State Management](#state-management)
10. [User Flows](#user-flows)

---

## 🔌 API Endpoints Reference

### Workshop Session Endpoints

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/api/workshops/sessions` | VENDOR | Create new session from ACTIVE template |
| GET | `/api/workshops/sessions/{id}` | PUBLIC | Get single session details |
| GET | `/api/workshops/sessions` | PUBLIC | Get all upcoming sessions (paginated) |
| GET | `/api/workshops/sessions/my` | VENDOR | Get vendor's own sessions (paginated) |
| GET | `/api/workshops/sessions/template/{templateId}` | PUBLIC | Get sessions for specific template |
| GET | `/api/workshops/sessions/filter` | PUBLIC | Advanced search/filter |
| PUT | `/api/workshops/sessions/{id}` | VENDOR | Update session (SCHEDULED only) |
| DELETE | `/api/workshops/sessions/{id}` | VENDOR | Delete session (no enrollments) |
| POST | `/api/workshops/sessions/{id}/cancel` | VENDOR | Cancel session (soft delete) |

---

## 📦 Data Models & Types

### TypeScript Interfaces

```typescript
// Session Status
enum SessionStatus {
  SCHEDULED = "SCHEDULED",   // Future session, bookable
  ONGOING = "ONGOING",       // Currently happening
  COMPLETED = "COMPLETED",   // Past session
  CANCELLED = "CANCELLED"    // Cancelled by vendor
}

// Workshop Session Response
interface WorkshopSessionResponse {
  // Session-specific info
  id: string; // UUID
  startTime: string; // ISO DateTime "2026-02-20T10:00:00"
  endTime: string; // ISO DateTime "2026-02-20T11:30:00"
  price: number; // Can differ from template default
  maxParticipants: number;
  currentEnrollments: number;
  availableSlots: number; // maxParticipants - currentEnrollments
  status: SessionStatus;
  createdAt: string;
  updatedAt: string;
  
  // Template details (embedded)
  workshopTemplate: {
    id: string;
    name: string;
    shortDescription: string;
    fullDescription: string;
    estimatedDuration: number;
    minParticipants: number;
    averageRating: number | null;
    totalReview: number;
    images: WorkshopImageResponse[];
    tags: WTagResponse[];
  };
  
  // Vendor details
  vendor: {
    id: string;
    name: string;
    email: string;
    avatar?: string;
  };
}

// Create Session Request
interface CreateWorkshopSessionRequest {
  workshopTemplateId: string; // UUID, must be ACTIVE template
  startTime: string; // ISO DateTime, must be in future
  endTime: string; // ISO DateTime, must be after startTime
  price?: number; // Optional, defaults to template's defaultPrice
  maxParticipants?: number; // Optional, defaults to template's maxParticipants
}

// Update Session Request
interface UpdateWorkshopSessionRequest {
  startTime?: string;
  endTime?: string;
  price?: number;
  maxParticipants?: number;
}

// Calendar Event (for display)
interface CalendarEvent {
  id: string;
  title: string; // Workshop name
  start: Date;
  end: Date;
  status: SessionStatus;
  availableSlots: number;
  currentEnrollments: number;
  maxParticipants: number;
  price: number;
  thumbnailUrl: string;
  session: WorkshopSessionResponse; // Full data
}

// Filter Options
interface SessionFilterOptions {
  keyword?: string;
  vendorId?: string;
  tagId?: string;
  status?: SessionStatus;
  startDate?: string; // ISO DateTime
  endDate?: string; // ISO DateTime
  minPrice?: number;
  maxPrice?: number;
  availableOnly?: boolean;
}
```

---

## 📁 Folder Structure

```
src/
├── services/
│   ├── api/
│   │   ├── workshopSessionApi.ts      # Session API calls
│   │   └── workshopTemplateApi.ts     # Template API (for dropdown)
│   └── calendar/
│       ├── calendarUtils.ts           # Date calculations, event formatting
│       └── sessionColorScheme.ts      # Status-based colors
│
├── types/
│   ├── workshop.types.ts              # Session types
│   └── calendar.types.ts              # Calendar-specific types
│
├── components/
│   ├── workshop-sessions/
│   │   ├── SessionCalendar/
│   │   │   ├── index.tsx              # Main calendar component
│   │   │   ├── DayView.tsx            # Day view
│   │   │   ├── WeekView.tsx           # Week view
│   │   │   ├── MonthView.tsx          # Month view
│   │   │   ├── EventCard.tsx          # Session event card
│   │   │   └── CalendarControls.tsx   # View switcher, date navigation
│   │   │
│   │   ├── SessionModal/
│   │   │   ├── CreateSessionModal.tsx # Create new session
│   │   │   ├── EditSessionModal.tsx   # Edit existing session
│   │   │   └── ViewSessionModal.tsx   # View session details
│   │   │
│   │   ├── SessionCard.tsx            # Session card for list view
│   │   ├── SessionStatusBadge.tsx     # Status indicator
│   │   ├── SessionTimeline.tsx        # Visual timeline with bookings
│   │   └── TemplateSelector.tsx       # Dropdown to select template
│   │
│   ├── common/
│   │   ├── DateTimePicker.tsx         # DateTime input component
│   │   ├── TimeRangePicker.tsx        # Start/End time picker
│   │   └── ConfirmDialog.tsx          # Confirmation modal
│   │
│   └── forms/
│       └── SessionForm.tsx            # Shared form for create/edit
│
├── pages/
│   ├── vendor/
│   │   └── workshop-sessions/
│   │       ├── index.tsx              # Calendar view (main page)
│   │       ├── list.tsx               # List view (alternative)
│   │       └── [id]/
│   │           └── details.tsx        # Session details page
│   │
│   └── public/
│       └── workshops/
│           ├── index.tsx              # Browse all sessions (marketplace)
│           └── [id]/
│               └── details.tsx        # Session details (for booking)
│
├── hooks/
│   ├── useWorkshopSessions.ts         # Session CRUD operations
│   ├── useSessionCalendar.ts          # Calendar state management
│   └── useCalendarNavigation.ts       # Date navigation logic
│
└── utils/
    ├── dateHelpers.ts                 # Date formatting, calculations
    └── sessionValidation.ts           # Form validation
```

---

## 📅 Calendar View Architecture

### Why Calendar View for Vendors?

Vendors need to:
1. **See all their scheduled sessions at a glance**
2. **Quickly identify busy vs. free time slots**
3. **Avoid double-booking or scheduling conflicts**
4. **Manage sessions efficiently** (create, edit, cancel)

### Calendar Views to Implement

#### 1. **Month View** (Default)
- Shows all sessions for the month
- Each day cell displays session count
- Color-coded by status (green, yellow, red)
- Click day → Opens day view or create session modal

**Visual:**
```
┌─────────────────────────────────────────────────────────┐
│  February 2026               [Today] [Week] [Month]     │
├─────────┬─────────┬─────────┬─────────┬─────────┬───────┤
│   Sun   │   Mon   │   Tue   │   Wed   │   Thu   │  Fri  │
├─────────┼─────────┼─────────┼─────────┼─────────┼───────┤
│    9    │   10    │   11    │   12    │   13 🟢 │  14   │
│         │         │         │         │ 2 events│       │
├─────────┼─────────┼─────────┼─────────┼─────────┼───────┤
│   16    │   17 🟡 │   18    │   19    │   20 🟢 │  21   │
│         │ 1 event │         │         │ 3 events│       │
└─────────┴─────────┴─────────┴─────────┴─────────┴───────┘
```

#### 2. **Week View**
- Shows 7 days with time slots (hourly grid)
- Sessions displayed as blocks in time slots
- Easy to see schedule density
- Good for detailed weekly planning

**Visual:**
```
┌─────────────────────────────────────────────────────────┐
│  Feb 10 - Feb 16, 2026           [Day] [Week] [Month]   │
├──────┬──────┬──────┬──────┬──────┬──────┬──────┬────────┤
│ Time │ Mon  │ Tue  │ Wed  │ Thu  │ Fri  │ Sat  │  Sun   │
├──────┼──────┼──────┼──────┼──────┼──────┼──────┼────────┤
│ 9 AM │      │      │      │ Yoga │      │      │        │
│      │      │      │      │ 9-10 │      │      │        │
├──────┼──────┼──────┼──────┼──────┼──────┼──────┼────────┤
│10 AM │      │      │Cooking│     │      │Paint │        │
│      │      │      │10-12 │      │      │10-11 │        │
├──────┼──────┼──────┼──────┼──────┼──────┼──────┼────────┤
│ 2 PM │      │      │      │      │Dance │      │        │
│      │      │      │      │      │ 2-4  │      │        │
└──────┴──────┴──────┴──────┴──────┴──────┴──────┴────────┘
```

#### 3. **Day View** (Detailed)
- Shows all sessions for a single day
- Timeline view with hourly breakdown
- Shows available slots vs. booked slots
- Quick actions: Edit, Cancel, View Details

**Visual:**
```
┌─────────────────────────────────────────────────────────┐
│  Thursday, February 13, 2026     [Day] [Week] [Month]   │
├──────────┬──────────────────────────────────────────────┤
│  8:00 AM │                                              │
├──────────┼──────────────────────────────────────────────┤
│  9:00 AM │ ┌─────────────────────────────────────────┐ │
│          │ │ 🟢 Yoga for Beginners                   │ │
│          │ │ 9:00 AM - 10:30 AM                      │ │
│          │ │ 12/20 slots booked | $49.99             │ │
│          │ │ [View] [Edit] [Cancel]                  │ │
│          │ └─────────────────────────────────────────┘ │
├──────────┼──────────────────────────────────────────────┤
│ 10:00 AM │                                              │
├──────────┼──────────────────────────────────────────────┤
│ 11:00 AM │                                              │
├──────────┼──────────────────────────────────────────────┤
│ 12:00 PM │ ┌─────────────────────────────────────────┐ │
│          │ │ 🟡 Painting Workshop (PENDING)          │ │
│          │ │ 12:00 PM - 2:00 PM                      │ │
│          │ │ 5/15 slots booked | $75.00              │ │
│          │ └─────────────────────────────────────────┘ │
└──────────┴──────────────────────────────────────────────┘
```

### Status-Based Color Coding

```typescript
// sessionColorScheme.ts
export const SESSION_COLORS = {
  SCHEDULED: {
    bg: '#10B981',      // Green - Active, bookable
    border: '#059669',
    text: '#FFFFFF',
    light: '#D1FAE5'
  },
  ONGOING: {
    bg: '#3B82F6',      // Blue - Currently happening
    border: '#2563EB',
    text: '#FFFFFF',
    light: '#DBEAFE'
  },
  COMPLETED: {
    bg: '#6B7280',      // Gray - Past session
    border: '#4B5563',
    text: '#FFFFFF',
    light: '#E5E7EB'
  },
  CANCELLED: {
    bg: '#EF4444',      // Red - Cancelled
    border: '#DC2626',
    text: '#FFFFFF',
    light: '#FEE2E2'
  }
};

export const getSessionColor = (status: SessionStatus) => {
  return SESSION_COLORS[status];
};
```

---

## 🧩 Components to Build

### 1. SessionCalendar Component
**Purpose:** Main calendar container with view switching

**Props:**
```typescript
interface SessionCalendarProps {
  sessions: WorkshopSessionResponse[];
  onSessionClick: (session: WorkshopSessionResponse) => void;
  onDateClick: (date: Date) => void; // For creating new session
  onSessionEdit: (sessionId: string) => void;
  onSessionCancel: (sessionId: string) => void;
  loading?: boolean;
  defaultView?: 'month' | 'week' | 'day';
}
```

**Features:**
- View switcher: Month / Week / Day
- Date navigation: Previous, Next, Today
- Displays sessions as colored events
- Click empty slot → Create new session
- Click existing event → View/Edit session
- Legend showing status colors
- Session count badges on month view

**Recommended Library:**
- **FullCalendar** (https://fullcalendar.io/) - Most popular, highly customizable
- **React Big Calendar** (https://github.com/jquense/react-big-calendar) - Simpler, good for basic needs
- **Custom Implementation** - Full control but more work

---

### 2. MonthView Component
**Purpose:** Display month grid with session indicators

**Features:**
- 7-column grid (Sun-Sat)
- Each cell shows date number
- Session count badge per day
- Color indicators for status
- Click date → Open day detail or create modal
- Highlight today's date
- Navigate between months

**Event Display:**
```
┌──────────────┐
│    13        │ ← Date
│  🟢 🟢 🟡   │ ← Status dots (up to 3, then "+2")
│  2 sessions │ ← Count
└──────────────┘
```

---

### 3. WeekView Component
**Purpose:** Display 7-day schedule with time slots

**Features:**
- Time grid from 6 AM to 11 PM (configurable)
- 7 columns for days of week
- Sessions as blocks spanning time slots
- Truncated session name in block
- Click block → Open session detail
- Scroll to current time (if today is visible)
- Drag to resize (optional, advanced)

**Session Block:**
```
┌──────────────────┐
│ Yoga Workshop    │
│ 9:00 - 10:30 AM  │
│ 12/20 booked     │
└──────────────────┘
```

---

### 4. DayView Component
**Purpose:** Detailed view of a single day's schedule

**Features:**
- Hourly timeline (vertical)
- Full session details per slot
- Available slots indicator
- Quick actions: View, Edit, Cancel
- Empty slots are clickable (create session)
- Show conflicts or overlaps
- Display session thumbnail image

---

### 5. CreateSessionModal Component
**Purpose:** Modal to create a new session

**Form Fields:**
```typescript
interface SessionFormData {
  workshopTemplateId: string; // Dropdown of vendor's ACTIVE templates
  startTime: Date;           // DateTime picker
  endTime: Date;             // DateTime picker (auto-calculated from duration)
  price?: number;            // Optional, defaults to template price
  maxParticipants?: number;  // Optional, defaults to template max
}
```

**Features:**
- Template selector dropdown (only ACTIVE templates)
- When template selected → Auto-fill:
  - Duration (calculate endTime)
  - Default price
  - Max participants
- DateTime pickers for start/end
- Validation:
  - Start time must be in future
  - End time must be after start
  - No overlaps with existing sessions (optional check)
- Show template preview (name, image, description)
- Submit → POST `/api/workshops/sessions`

**User Flow:**
1. Vendor clicks empty calendar slot or "New Session" button
2. Modal opens with date pre-filled (if clicked a date)
3. Select template from dropdown
4. Adjust time, price, max participants if needed
5. Click "Create Session"
6. Session appears on calendar immediately

---

### 6. EditSessionModal Component
**Purpose:** Edit an existing SCHEDULED session

**Features:**
- Same form as create, but pre-filled with existing data
- Only allow editing SCHEDULED sessions
- Show current enrollments (cannot reduce max below current)
- Validate changes (start/end time, participant limit)
- Submit → PUT `/api/workshops/sessions/{id}`

**Restrictions:**
- Cannot change template (locked)
- Cannot reduce maxParticipants below currentEnrollments
- Cannot edit ONGOING, COMPLETED, or CANCELLED sessions

---

### 7. ViewSessionModal Component
**Purpose:** View session details (read-only)

**Display:**
- Workshop name and image gallery
- Date and time (formatted nicely)
- Duration
- Price
- Venue/Location (if applicable, future feature)
- Current bookings: "12 out of 20 slots booked"
- Progress bar showing booking percentage
- List of enrolled participants (admin/vendor view)
- Template details (description, tags, ratings)
- Status badge
- Actions based on status:
  - SCHEDULED: [Edit] [Cancel Session]
  - ONGOING: [View Only]
  - COMPLETED: [View Only]
  - CANCELLED: [View Only]

---

### 8. SessionCard Component
**Purpose:** Display session in list view (alternative to calendar)

**Display:**
- Thumbnail image
- Workshop name
- Date and time (formatted)
- Duration
- Price
- Status badge
- Available slots: "8 slots left" or "Fully booked"
- Tags
- Action buttons

**Layout:**
```
┌────────────────────────────────────────────────┐
│ ┌─────┐  Yoga for Beginners         🟢 SCHEDULED│
│ │ IMG │  Feb 13, 2026 • 9:00 - 10:30 AM         │
│ └─────┘  $49.99 • 90 min • 12/20 booked        │
│          #Wellness #Yoga                        │
│          [Edit] [Cancel] [View Details]         │
└────────────────────────────────────────────────┘
```

---

### 9. TemplateSelector Component
**Purpose:** Dropdown to select workshop template when creating session

**Props:**
```typescript
interface TemplateSelectorProps {
  selectedTemplateId: string | null;
  onChange: (templateId: string) => void;
  onTemplateSelected?: (template: WorkshopTemplateResponse) => void;
  error?: string;
}
```

**Features:**
- Fetch vendor's ACTIVE templates
- Display as searchable dropdown/combobox
- Show template name, thumbnail, price
- Filter: Only show ACTIVE status templates
- Empty state: "No active templates. Create one first."
- On selection → Emit full template object (for auto-fill)

**API Call:**
```typescript
GET /api/workshops/templates/my?status=ACTIVE
Response: WorkshopTemplateResponse[]
```

---

### 10. SessionStatusBadge Component
**Purpose:** Visual indicator of session status

**Props:**
```typescript
interface SessionStatusBadgeProps {
  status: SessionStatus;
  size?: 'sm' | 'md' | 'lg';
}
```

**Status Display:**
- SCHEDULED: Green badge "Scheduled"
- ONGOING: Blue badge "In Progress"
- COMPLETED: Gray badge "Completed"
- CANCELLED: Red badge "Cancelled"

---

## 📄 Pages to Build

### 1. Vendor Session Calendar Page
**Path:** `/vendor/workshop-sessions`

**Main Features:**
- **Calendar View (Default):**
  - FullCalendar integration
  - Month/Week/Day view toggle
  - Color-coded sessions by status
  - Click empty slot → Create session modal
  - Click event → View/Edit session modal
  
- **Top Bar:**
  - View switcher: [Month] [Week] [Day] [List]
  - Date navigator: [<] [Today] [>]
  - "New Session" button
  - Filter dropdown: All Statuses / Scheduled / Ongoing / Completed / Cancelled
  
- **Side Panel (Optional):**
  - Mini calendar (month overview)
  - Quick stats:
    - Total sessions this month
    - Upcoming sessions (next 7 days)
    - Total bookings
  - Legend (status colors)

**API Calls:**
```typescript
// Fetch vendor's sessions for calendar
GET /api/workshops/sessions/my?page=0&size=100&sortBy=startTime&sortDir=asc

// Optionally filter by date range
GET /api/workshops/sessions/filter?vendorId={vendorId}&startDate={startOfMonth}&endDate={endOfMonth}
```

**State Management:**
```typescript
const [currentView, setCurrentView] = useState<'month' | 'week' | 'day'>('month');
const [currentDate, setCurrentDate] = useState(new Date());
const [sessions, setSessions] = useState<WorkshopSessionResponse[]>([]);
const [selectedSession, setSelectedSession] = useState<WorkshopSessionResponse | null>(null);
const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
const [isEditModalOpen, setIsEditModalOpen] = useState(false);
```

---

### 2. Vendor Session List Page (Alternative View)
**Path:** `/vendor/workshop-sessions/list`

**Features:**
- Traditional list/table view (alternative to calendar)
- Pagination (10, 25, 50 per page)
- Sort by: Date, Price, Status, Bookings
- Filter:
  - By status
  - By date range
  - By template
- Search by workshop name
- Actions per row: Edit, Cancel, View Details

**Use Case:**
- Some users prefer list over calendar
- Better for viewing detailed info (price, bookings, etc.)
- Easier to export or print

---

### 3. Session Details Page
**Path:** `/vendor/workshop-sessions/{id}/details`

**Sections:**

#### A. Session Overview
- Workshop name (large heading)
- Date and time (prominent)
- Status badge
- Thumbnail image

#### B. Session Info Card
- Duration
- Price
- Current bookings: "12 / 20 enrolled"
- Available slots: "8 slots left"
- Progress bar (booking percentage)

#### C. Template Details (Collapsible)
- Full description
- Tags
- Average rating
- Image gallery

#### D. Enrollment List (Vendor Only)
- Table of enrolled participants:
  - Name
  - Email
  - Booking date
  - Payment status

#### E. Actions
- [Edit Session] (if SCHEDULED)
- [Cancel Session] (if SCHEDULED)
- [Back to Calendar]

**API Call:**
```typescript
GET /api/workshops/sessions/{id}
Response: WorkshopSessionResponse
```

---

### 4. Public Workshop Marketplace Page
**Path:** `/workshops` (Public)

**Features:**
- Browse all SCHEDULED sessions from all vendors
- Calendar view OR grid view
- Advanced filters:
  - By date range
  - By category/tag
  - By price range
  - By vendor
  - Available slots only
- Search by keyword
- Sort by: Date, Price, Rating
- Click session → View details page
- "Book Now" button (leads to booking flow - out of scope here)

**API Call:**
```typescript
GET /api/workshops/sessions?page=0&size=20&sortBy=startTime&sortDir=asc
GET /api/workshops/sessions/filter?status=SCHEDULED&availableOnly=true
```

---

## 🗓️ Calendar Implementation Guide

### Option A: Using FullCalendar (Recommended)

**Installation:**
```bash
npm install @fullcalendar/react @fullcalendar/core @fullcalendar/daygrid @fullcalendar/timegrid @fullcalendar/interaction
```

**Basic Setup:**
```typescript
import FullCalendar from '@fullcalendar/react';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';

const SessionCalendar = ({ sessions }: { sessions: WorkshopSessionResponse[] }) => {
  // Convert sessions to FullCalendar events
  const events = sessions.map(session => ({
    id: session.id,
    title: session.workshopTemplate.name,
    start: session.startTime,
    end: session.endTime,
    backgroundColor: getSessionColor(session.status).bg,
    borderColor: getSessionColor(session.status).border,
    extendedProps: {
      session: session // Store full session data
    }
  }));

  const handleEventClick = (info: any) => {
    const session = info.event.extendedProps.session;
    // Open view/edit modal
    openSessionModal(session);
  };

  const handleDateClick = (info: any) => {
    // Open create modal with pre-filled date
    openCreateModal(info.date);
  };

  return (
    <FullCalendar
      plugins={[dayGridPlugin, timeGridPlugin, interactionPlugin]}
      initialView="dayGridMonth"
      headerToolbar={{
        left: 'prev,next today',
        center: 'title',
        right: 'dayGridMonth,timeGridWeek,timeGridDay'
      }}
      events={events}
      eventClick={handleEventClick}
      dateClick={handleDateClick}
      height="auto"
      slotMinTime="06:00:00"
      slotMaxTime="23:00:00"
      allDaySlot={false}
      nowIndicator={true}
      editable={false} // Set true for drag-and-drop (advanced)
      selectable={true}
    />
  );
};
```

**Event Rendering Customization:**
```typescript
const renderEventContent = (eventInfo: any) => {
  const session = eventInfo.event.extendedProps.session;
  const availableSlots = session.availableSlots;
  
  return (
    <div className="fc-event-content">
      <div className="fc-event-time">{eventInfo.timeText}</div>
      <div className="fc-event-title">{eventInfo.event.title}</div>
      <div className="fc-event-slots">
        {availableSlots > 0 ? `${availableSlots} slots` : 'Full'}
      </div>
    </div>
  );
};

// Add to FullCalendar props:
<FullCalendar
  // ...other props
  eventContent={renderEventContent}
/>
```

---

### Option B: Using React Big Calendar

**Installation:**
```bash
npm install react-big-calendar moment
```

**Basic Setup:**
```typescript
import { Calendar, momentLocalizer } from 'react-big-calendar';
import moment from 'moment';
import 'react-big-calendar/lib/css/react-big-calendar.css';

const localizer = momentLocalizer(moment);

const SessionCalendar = ({ sessions }: { sessions: WorkshopSessionResponse[] }) => {
  const events = sessions.map(session => ({
    id: session.id,
    title: session.workshopTemplate.name,
    start: new Date(session.startTime),
    end: new Date(session.endTime),
    resource: session
  }));

  const eventStyleGetter = (event: any) => {
    const session = event.resource;
    const colors = getSessionColor(session.status);
    
    return {
      style: {
        backgroundColor: colors.bg,
        borderColor: colors.border,
        color: colors.text
      }
    };
  };

  return (
    <Calendar
      localizer={localizer}
      events={events}
      startAccessor="start"
      endAccessor="end"
      style={{ height: 600 }}
      eventPropGetter={eventStyleGetter}
      onSelectEvent={(event) => openSessionModal(event.resource)}
      onSelectSlot={(slotInfo) => openCreateModal(slotInfo.start)}
      selectable
      views={['month', 'week', 'day']}
      defaultView="month"
    />
  );
};
```

---

### Option C: Custom Calendar (Advanced)

If you want full control, build a custom calendar using:
- Date manipulation: **date-fns** or **dayjs**
- Grid layout: CSS Grid or Flexbox
- Event positioning: Calculate based on time slots

**Pros:**
- Full customization
- No library limitations

**Cons:**
- More development time
- Need to handle edge cases (overlapping events, timezone, DST, etc.)

---

## 🔧 API Service Layer

### workshopSessionApi.ts

```typescript
import axios from 'axios';
import { 
  CreateWorkshopSessionRequest, 
  UpdateWorkshopSessionRequest, 
  WorkshopSessionResponse, 
  PageResponse, 
  ApiResponse,
  SessionFilterOptions
} from '../types';

const API_BASE = '/api/workshops/sessions';

export const workshopSessionApi = {
  // Create session
  create: async (data: CreateWorkshopSessionRequest) => {
    const response = await axios.post<ApiResponse<WorkshopSessionResponse>>(API_BASE, data);
    return response.data.data;
  },

  // Get single session
  getById: async (id: string) => {
    const response = await axios.get<ApiResponse<WorkshopSessionResponse>>(`${API_BASE}/${id}`);
    return response.data.data;
  },

  // Get vendor's sessions (paginated)
  getMySessions: async (page: number, size: number, sortBy: string, sortDir: string) => {
    const response = await axios.get<ApiResponse<PageResponse<WorkshopSessionResponse>>>(
      `${API_BASE}/my`,
      { params: { page, size, sortBy, sortDir } }
    );
    return response.data.data;
  },

  // Get all upcoming sessions (public)
  getUpcoming: async (page: number, size: number, sortBy: string, sortDir: string) => {
    const response = await axios.get<ApiResponse<PageResponse<WorkshopSessionResponse>>>(
      API_BASE,
      { params: { page, size, sortBy, sortDir } }
    );
    return response.data.data;
  },

  // Get sessions by template
  getByTemplate: async (templateId: string, page: number, size: number) => {
    const response = await axios.get<ApiResponse<PageResponse<WorkshopSessionResponse>>>(
      `${API_BASE}/template/${templateId}`,
      { params: { page, size, sortBy: 'startTime', sortDir: 'asc' } }
    );
    return response.data.data;
  },

  // Update session
  update: async (id: string, data: UpdateWorkshopSessionRequest) => {
    const response = await axios.put<ApiResponse<WorkshopSessionResponse>>(`${API_BASE}/${id}`, data);
    return response.data.data;
  },

  // Delete session
  delete: async (id: string) => {
    await axios.delete(`${API_BASE}/${id}`);
  },

  // Cancel session
  cancel: async (id: string) => {
    const response = await axios.post<ApiResponse<WorkshopSessionResponse>>(`${API_BASE}/${id}/cancel`);
    return response.data.data;
  },

  // Search/Filter sessions
  filter: async (filters: SessionFilterOptions, page: number, size: number, sortBy: string, sortDir: string) => {
    const response = await axios.get<ApiResponse<PageResponse<WorkshopSessionResponse>>>(
      `${API_BASE}/filter`,
      { params: { ...filters, page, size, sortBy, sortDir } }
    );
    return response.data.data;
  },

  // Get sessions for a date range (useful for calendar)
  getByDateRange: async (startDate: string, endDate: string, vendorId?: string) => {
    const filters: SessionFilterOptions = {
      startDate,
      endDate,
      vendorId
    };
    const response = await axios.get<ApiResponse<PageResponse<WorkshopSessionResponse>>>(
      `${API_BASE}/filter`,
      { params: { ...filters, page: 0, size: 1000 } } // Get all for calendar
    );
    return response.data.data.content;
  }
};
```

---

## 🎯 State Management

### Using React Query (Recommended)

```typescript
// hooks/useWorkshopSessions.ts
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { workshopSessionApi } from '../services/api/workshopSessionApi';

// Fetch vendor's sessions
export const useMySessions = (page: number, size: number) => {
  return useQuery({
    queryKey: ['workshopSessions', 'my', page, size],
    queryFn: () => workshopSessionApi.getMySessions(page, size, 'startTime', 'asc')
  });
};

// Fetch sessions for calendar (date range)
export const useCalendarSessions = (startDate: string, endDate: string, vendorId?: string) => {
  return useQuery({
    queryKey: ['workshopSessions', 'calendar', startDate, endDate, vendorId],
    queryFn: () => workshopSessionApi.getByDateRange(startDate, endDate, vendorId),
    staleTime: 1000 * 60 * 5 // 5 minutes
  });
};

// Create session
export const useCreateSession = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: workshopSessionApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['workshopSessions'] });
    }
  });
};

// Update session
export const useUpdateSession = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: UpdateWorkshopSessionRequest }) =>
      workshopSessionApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['workshopSessions'] });
    }
  });
};

// Cancel session
export const useCancelSession = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => workshopSessionApi.cancel(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['workshopSessions'] });
    }
  });
};

// Delete session
export const useDeleteSession = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => workshopSessionApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['workshopSessions'] });
    }
  });
};
```

### Custom Hook for Calendar Navigation

```typescript
// hooks/useCalendarNavigation.ts
import { useState } from 'react';
import { startOfMonth, endOfMonth, addMonths, subMonths, format } from 'date-fns';

export const useCalendarNavigation = () => {
  const [currentDate, setCurrentDate] = useState(new Date());
  const [view, setView] = useState<'month' | 'week' | 'day'>('month');

  const goToToday = () => {
    setCurrentDate(new Date());
  };

  const goToNext = () => {
    if (view === 'month') {
      setCurrentDate(addMonths(currentDate, 1));
    } else if (view === 'week') {
      setCurrentDate(new Date(currentDate.getTime() + 7 * 24 * 60 * 60 * 1000));
    } else {
      setCurrentDate(new Date(currentDate.getTime() + 24 * 60 * 60 * 1000));
    }
  };

  const goToPrevious = () => {
    if (view === 'month') {
      setCurrentDate(subMonths(currentDate, 1));
    } else if (view === 'week') {
      setCurrentDate(new Date(currentDate.getTime() - 7 * 24 * 60 * 60 * 1000));
    } else {
      setCurrentDate(new Date(currentDate.getTime() - 24 * 60 * 60 * 1000));
    }
  };

  const getDateRange = () => {
    const start = startOfMonth(currentDate);
    const end = endOfMonth(currentDate);
    return {
      startDate: format(start, "yyyy-MM-dd'T'00:00:00"),
      endDate: format(end, "yyyy-MM-dd'T'23:59:59")
    };
  };

  return {
    currentDate,
    view,
    setView,
    goToToday,
    goToNext,
    goToPrevious,
    getDateRange
  };
};
```

---

## 🔄 User Flows

### Flow 1: Create a New Session (Calendar View)

```
1. Vendor is on calendar page (month view)
   ↓
2. Vendor clicks an empty date cell (e.g., Feb 20)
   → CreateSessionModal opens with date pre-filled to Feb 20
   ↓
3. Vendor selects workshop template from dropdown
   → Only ACTIVE templates shown
   → "Yoga for Beginners" selected
   ↓
4. System auto-fills:
   → End time (based on 90-min duration)
   → Default price: $49.99
   → Max participants: 20
   ↓
5. Vendor adjusts:
   → Start time: 10:00 AM → 9:00 AM
   → Price: $49.99 → $45.00 (discount)
   ↓
6. Vendor clicks "Create Session"
   → API: POST /api/workshops/sessions
   → Validation passes
   ↓
7. Modal closes, success toast: "Session created successfully"
   ↓
8. Calendar refreshes, new green event appears on Feb 20 at 9:00 AM
   → Shows "Yoga for Beginners"
   → Status: SCHEDULED
```

### Flow 2: Edit an Existing Session

```
1. Vendor clicks on a green event in the calendar
   → "Yoga for Beginners" on Feb 20, 9:00 AM
   ↓
2. ViewSessionModal opens showing:
   → Workshop details
   → Current bookings: "12/20 enrolled"
   → [Edit] [Cancel] buttons
   ↓
3. Vendor clicks [Edit]
   → EditSessionModal opens with pre-filled data
   ↓
4. Vendor changes:
   → Max participants: 20 → 25 (increase capacity)
   ↓
5. Vendor tries to change:
   → Max participants: 20 → 10 (reduce capacity)
   → ❌ Validation error: "Cannot reduce capacity below current enrollments (12)"
   ↓
6. Vendor keeps max at 25, clicks "Save Changes"
   → API: PUT /api/workshops/sessions/{id}
   ↓
7. Modal closes, success toast: "Session updated"
   ↓
8. Calendar event remains, but now shows "13 slots available"
```

### Flow 3: Cancel a Session

```
1. Vendor clicks on a scheduled session in calendar
   ↓
2. ViewSessionModal opens
   → Shows "8/20 enrolled"
   ↓
3. Vendor clicks [Cancel Session]
   → ConfirmDialog opens:
     "⚠️ Cancel this session?
     8 participants are enrolled. They will be notified and refunded.
     This action cannot be undone."
     [Cancel] [Confirm Cancellation]
   ↓
4. Vendor clicks [Confirm Cancellation]
   → API: POST /api/workshops/sessions/{id}/cancel
   → Status changes: SCHEDULED → CANCELLED
   ↓
5. Modal closes, success toast: "Session cancelled"
   ↓
6. Calendar event color changes from green to red
   → Shows "CANCELLED" badge
   → No longer bookable
   → Remains visible for record-keeping
```

### Flow 4: Delete a Session (No Enrollments)

```
1. Vendor creates a session but no one books it
   ↓
2. Vendor decides to remove it completely
   ↓
3. Vendor clicks the session in calendar
   ↓
4. ViewSessionModal shows [Edit] [Delete] [Cancel]
   → Delete is available because currentEnrollments = 0
   ↓
5. Vendor clicks [Delete]
   → ConfirmDialog: "Delete this session? This action cannot be undone."
   ↓
6. Vendor confirms
   → API: DELETE /api/workshops/sessions/{id}
   ↓
7. Session is permanently removed from database
   ↓
8. Calendar refreshes, event disappears
```

### Flow 5: View Calendar by Week

```
1. Vendor is on month view, sees several sessions
   ↓
2. Vendor clicks [Week] view toggle
   ↓
3. Calendar switches to week view (7 columns, hourly rows)
   → Current week shown: Feb 10-16
   ↓
4. Vendor sees:
   → Monday 2 PM: "Painting Workshop" (green block spanning 2 hours)
   → Thursday 9 AM: "Yoga" (green block spanning 1.5 hours)
   → Saturday 10 AM: "Cooking Class" (green block spanning 2 hours)
   ↓
5. Vendor identifies empty slot: Wednesday 3 PM
   → Clicks empty slot
   → CreateSessionModal opens with date/time pre-filled
   ↓
6. Vendor creates new session for Wednesday 3 PM
   ↓
7. New session appears immediately in the week view
```

---

## 📊 Calendar Data Loading Strategy

### Approach 1: Fetch All Sessions (Small Scale)

**When to use:** If vendor has < 100 sessions
**Pros:** Simple, no refetching when changing dates
**Cons:** Initial load might be slow

```typescript
const { data: sessions, isLoading } = useMySessions(0, 1000); // Get all

// Filter in client-side based on current view
const visibleSessions = sessions?.content.filter(session => {
  const sessionDate = new Date(session.startTime);
  return sessionDate >= startOfMonth(currentDate) && sessionDate <= endOfMonth(currentDate);
});
```

### Approach 2: Fetch Date Range (Scalable)

**When to use:** If vendor has > 100 sessions
**Pros:** Efficient, only loads visible data
**Cons:** Need to refetch when changing months

```typescript
const { startDate, endDate } = getDateRange(); // Based on current view
const { data: sessions, isLoading } = useCalendarSessions(startDate, endDate, vendorId);

// Re-fetches when user navigates to different month
```

### Approach 3: Infinite Scroll (Advanced)

**When to use:** Very large datasets, list view
**Pros:** Best performance
**Cons:** Complex implementation

---

## ✅ Validation Rules

### Create Session Validation

```typescript
// sessionValidation.ts
import * as yup from 'yup';

export const createSessionSchema = yup.object({
  workshopTemplateId: yup.string()
    .required('Workshop template is required')
    .uuid('Invalid template ID'),
  
  startTime: yup.date()
    .required('Start time is required')
    .min(new Date(), 'Start time must be in the future')
    .typeError('Invalid date format'),
  
  endTime: yup.date()
    .required('End time is required')
    .min(yup.ref('startTime'), 'End time must be after start time')
    .typeError('Invalid date format'),
  
  price: yup.number()
    .positive('Price must be positive')
    .min(0.01, 'Price must be at least $0.01')
    .optional(),
  
  maxParticipants: yup.number()
    .positive('Max participants must be positive')
    .integer('Must be a whole number')
    .optional()
});

export const updateSessionSchema = yup.object({
  startTime: yup.date()
    .min(new Date(), 'Start time must be in the future')
    .typeError('Invalid date format')
    .optional(),
  
  endTime: yup.date()
    .min(yup.ref('startTime'), 'End time must be after start time')
    .typeError('Invalid date format')
    .optional(),
  
  price: yup.number()
    .positive('Price must be positive')
    .optional(),
  
  maxParticipants: yup.number()
    .positive('Max participants must be positive')
    .integer('Must be a whole number')
    .test('not-below-current', 'Cannot reduce below current enrollments', function(value) {
      const currentEnrollments = this.options.context?.currentEnrollments || 0;
      return !value || value >= currentEnrollments;
    })
    .optional()
});
```

---

## 🎨 UI/UX Recommendations

### Calendar Interactions

1. **Hover Effects:**
   - Hover over event → Show tooltip with quick info (time, slots, price)
   - Hover over empty slot → Show "Click to create session"

2. **Click Behaviors:**
   - Click event → Open view/edit modal
   - Double-click event → Quick edit (advanced)
   - Click empty slot → Open create modal with date pre-filled

3. **Visual Cues:**
   - Current time indicator (red line in day/week view)
   - Today's date highlighted with border
   - Past dates grayed out slightly
   - Fully booked sessions: Add "FULL" badge or opacity

4. **Responsive Behavior:**
   - Mobile: Default to day view or list view
   - Tablet: Week view works well
   - Desktop: All views available

5. **Loading States:**
   - Skeleton calendar while fetching
   - Shimmer effect on event cards

6. **Empty States:**
   - No sessions this month: "No sessions scheduled. Click a date to create one."
   - No templates available: "Create a workshop template first before scheduling sessions."

### Status-Based UI Rules

| Status | Color | Actions Available | Notes |
|--------|-------|-------------------|-------|
| SCHEDULED | Green 🟢 | Edit, Cancel, Delete* | *Delete only if no enrollments |
| ONGOING | Blue 🔵 | View only | Currently in progress |
| COMPLETED | Gray ⚪ | View only | Historical record |
| CANCELLED | Red 🔴 | View only | Show cancellation notice |

### Confirmation Dialogs

Always confirm:
- **Cancel session** (if enrollments > 0)
- **Delete session**
- **Reduce max participants** (if below current enrollments)

### Success Feedback

Use toast notifications:
- ✅ "Session created successfully"
- ✅ "Session updated"
- ✅ "Session cancelled"
- ✅ "Session deleted"

---

## 🚀 Implementation Roadmap

### Week 1: Foundation
- [ ] Set up API service layer
- [ ] Define TypeScript types
- [ ] Set up React Query
- [ ] Create basic page structure

### Week 2: Calendar Core
- [ ] Install and configure FullCalendar
- [ ] Implement data fetching for calendar
- [ ] Display sessions as events
- [ ] Add view switching (Month/Week/Day)
- [ ] Add date navigation controls

### Week 3: Modals & Forms
- [ ] Build CreateSessionModal with form
- [ ] Build EditSessionModal
- [ ] Build ViewSessionModal
- [ ] Implement form validation
- [ ] Add TemplateSelector component

### Week 4: Actions & Polish
- [ ] Implement create session flow
- [ ] Implement edit session flow
- [ ] Implement cancel/delete flows
- [ ] Add confirmation dialogs
- [ ] Add success/error feedback

### Week 5: Advanced Features
- [ ] Add filtering (by status, template)
- [ ] Add search functionality
- [ ] Build list view (alternative)
- [ ] Add mini calendar sidebar
- [ ] Add session statistics panel

### Week 6: Testing & Refinement
- [ ] Test all user flows
- [ ] Mobile responsive design
- [ ] Accessibility improvements
- [ ] Performance optimization
- [ ] Bug fixes

---

## 🧪 Testing Checklist

### Create Session
- [ ] Can select template from dropdown
- [ ] Date/time pre-filled if clicked from calendar
- [ ] Auto-fills price and max participants from template
- [ ] Validates start time in future
- [ ] Validates end time after start time
- [ ] Creates with SCHEDULED status
- [ ] Appears on calendar immediately after creation

### Edit Session
- [ ] Can only edit SCHEDULED sessions
- [ ] Cannot reduce max participants below current enrollments
- [ ] Cannot edit ONGOING/COMPLETED/CANCELLED sessions
- [ ] Updates reflect immediately on calendar

### Cancel Session
- [ ] Shows confirmation dialog
- [ ] Changes status to CANCELLED
- [ ] Event changes to red on calendar
- [ ] Cannot cancel already cancelled sessions

### Delete Session
- [ ] Only available if no enrollments
- [ ] Shows confirmation dialog
- [ ] Permanently removes from calendar
- [ ] Cannot delete if enrollments exist

### Calendar Navigation
- [ ] Previous/Next buttons work correctly
- [ ] Today button jumps to current date
- [ ] View switcher changes layout correctly
- [ ] Events display in all views

### Calendar Views
- [ ] Month view shows all sessions
- [ ] Week view shows hourly schedule
- [ ] Day view shows detailed timeline
- [ ] Events are color-coded by status

### Responsive Design
- [ ] Mobile: List or day view by default
- [ ] Tablet: Week view works well
- [ ] Desktop: All views accessible
- [ ] Modals are mobile-friendly

---

## 📝 Calendar Library Comparison

| Feature | FullCalendar | React Big Calendar | Custom Build |
|---------|--------------|-------------------|--------------|
| **Ease of Use** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐ |
| **Customization** | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Documentation** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | N/A |
| **Bundle Size** | Medium (~200KB) | Small (~80KB) | Minimal |
| **Features** | Very Rich | Good | As needed |
| **React Support** | Excellent | Native | Full control |
| **Cost** | Free + Paid | Free | Free |
| **Best For** | Production apps | Simple calendars | Unique designs |

**Recommendation:** Start with **FullCalendar** for feature-rich, professional calendar experience.

---

## 🔗 Additional Resources

### Date Formatting Helper

```typescript
// utils/dateHelpers.ts
import { format, formatDistance, isPast, isFuture, isToday } from 'date-fns';

export const formatSessionDate = (isoDate: string) => {
  const date = new Date(isoDate);
  return format(date, 'MMM dd, yyyy');
};

export const formatSessionTime = (isoDate: string) => {
  const date = new Date(isoDate);
  return format(date, 'h:mm a');
};

export const formatSessionDateTime = (isoDate: string) => {
  const date = new Date(isoDate);
  return format(date, 'MMM dd, yyyy • h:mm a');
};

export const formatDuration = (startTime: string, endTime: string) => {
  const start = new Date(startTime);
  const end = new Date(endTime);
  const diffMs = end.getTime() - start.getTime();
  const minutes = Math.floor(diffMs / 60000);
  
  if (minutes < 60) return `${minutes} min`;
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  return mins === 0 ? `${hours} hr` : `${hours}h ${mins}m`;
};

export const getRelativeTime = (isoDate: string) => {
  const date = new Date(isoDate);
  if (isToday(date)) return 'Today';
  return formatDistance(date, new Date(), { addSuffix: true });
};

export const isSessionInFuture = (startTime: string) => {
  return isFuture(new Date(startTime));
};

export const isSessionInPast = (endTime: string) => {
  return isPast(new Date(endTime));
};
```

### Session Status Helper

```typescript
// utils/sessionHelpers.ts
import { SessionStatus } from '../types';

export const canEditSession = (status: SessionStatus): boolean => {
  return status === SessionStatus.SCHEDULED;
};

export const canDeleteSession = (status: SessionStatus, enrollments: number): boolean => {
  return status === SessionStatus.SCHEDULED && enrollments === 0;
};

export const canCancelSession = (status: SessionStatus): boolean => {
  return status === SessionStatus.SCHEDULED;
};

export const getStatusText = (status: SessionStatus): string => {
  const statusMap = {
    [SessionStatus.SCHEDULED]: 'Scheduled',
    [SessionStatus.ONGOING]: 'In Progress',
    [SessionStatus.COMPLETED]: 'Completed',
    [SessionStatus.CANCELLED]: 'Cancelled'
  };
  return statusMap[status];
};

export const getAvailabilityText = (available: number, max: number): string => {
  if (available === 0) return 'Fully Booked';
  if (available === max) return 'All slots available';
  return `${available} of ${max} slots available`;
};
```

---

## ✨ Summary

This guide provides a complete blueprint for building a **calendar-based workshop session management system** for vendors. Key highlights:

1. **Calendar View is Central:** Vendors manage sessions visually through an interactive calendar
2. **Three View Modes:** Month (overview), Week (planning), Day (detailed)
3. **Status-Based Actions:** SCHEDULED sessions are editable, others are view-only
4. **Booking Awareness:** Show available slots, prevent overbooking
5. **Template Integration:** Sessions are created from approved templates
6. **User-Friendly:** Click-to-create, drag-to-reschedule (optional), color-coded status

The calendar interface makes it intuitive for vendors to:
- See their schedule at a glance
- Avoid double-booking
- Quickly create sessions for open time slots
- Monitor session bookings and availability

Good luck building an amazing workshop session calendar! 🎉📅
