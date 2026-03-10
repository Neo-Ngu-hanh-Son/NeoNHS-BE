# Admin History Audio API Documentation

## Overview

This API manages **Point History Audios** — audio narrations attached to a **Point** (a location/attraction stop). Each history audio contains a Cloudinary audio file URL, the transcript text, word-level timing data (for synchronized playback highlights), and metadata about how the audio was produced (AI-generated vs. manually uploaded).

A Point can have **multiple** history audios (e.g., different languages or versions).

## Base URL

```
/api/admin/points/{pointId}/history-audios
```

> **Note:** `{pointId}` is the UUID of the parent Point. All endpoints are scoped under a specific Point.

## Authentication & Authorization

- **Required**: Admin JWT Token
- **Role**: `ADMIN`
- **Header**: `Authorization: Bearer <admin-jwt-token>`

---

## Data Structures

### Request Body: `CreatePointHistoryAudio`

Used for both **Create** and **Update** operations.

```json
{
  "audioUrl": "https://res.cloudinary.com/.../audio.mp3",
  "historyText": "The text content of the narration...",
  "words": [
    { "text": "The", "start": 0.0, "end": 0.15 },
    { "text": "text", "start": 0.15, "end": 0.42 },
    { "text": "content", "start": 0.42, "end": 0.88 }
  ],
  "metadata": {
    "mode": "generate",
    "modelId": "eleven_multilingual_v2",
    "voiceId": "21m00Tcm4TlvDq8ikWAM",
    "language": "en"
  }
}
```

#### Field Reference

| Field         | Type            | Required | Description                                            |
| ------------- | --------------- | -------- | ------------------------------------------------------ |
| `audioUrl`    | `string`        | Yes      | Cloudinary URL of the uploaded/generated audio file    |
| `historyText` | `string`        | Yes      | The full transcript / narration text                   |
| `words`       | `WordTiming[]`  | No       | Array of word-level timing objects for synced playback |
| `metadata`    | `AudioMetadata` | No       | Audio generation metadata (can be `null` for uploads)  |

#### `WordTiming` Object

| Field   | Type     | Description           |
| ------- | -------- | --------------------- |
| `text`  | `string` | The word              |
| `start` | `number` | Start time in seconds |
| `end`   | `number` | End time in seconds   |

#### `AudioMetadata` Object

| Field      | Type             | Description                                                            |
| ---------- | ---------------- | ---------------------------------------------------------------------- |
| `mode`     | `string`         | `"generate"` (AI-generated) or `"upload"` (manually uploaded)          |
| `modelId`  | `string \| null` | AI model ID (e.g., ElevenLabs model). `null` when `mode` is `"upload"` |
| `voiceId`  | `string \| null` | AI voice ID. `null` when `mode` is `"upload"`                          |
| `language` | `string`         | Language code (e.g., `"en"`, `"vi"`)                                   |

### Response Body: `PointHistoryAudioResponse`

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "pointId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "audioUrl": "https://res.cloudinary.com/.../audio.mp3",
  "historyText": "The text content of the narration...",
  "words": [
    { "text": "The", "start": 0.0, "end": 0.15 },
    { "text": "text", "start": 0.15, "end": 0.42 }
  ],
  "metadata": {
    "mode": "generate",
    "modelId": "eleven_multilingual_v2",
    "voiceId": "21m00Tcm4TlvDq8ikWAM",
    "language": "en"
  },
  "createdAt": "2026-03-05T21:30:00",
  "updatedAt": "2026-03-05T21:30:00"
}
```

| Field         | Type                    | Description                                         |
| ------------- | ----------------------- | --------------------------------------------------- |
| `id`          | `string (UUID)`         | Unique identifier of the history audio              |
| `pointId`     | `string (UUID)`         | The parent Point's ID                               |
| `audioUrl`    | `string`                | Cloudinary URL                                      |
| `historyText` | `string`                | Full transcript text                                |
| `words`       | `WordTiming[]`          | Deserialized word timing array (empty `[]` if none) |
| `metadata`    | `AudioMetadata`         | Unflattened metadata object                         |
| `createdAt`   | `string (ISO datetime)` | Creation timestamp                                  |
| `updatedAt`   | `string (ISO datetime)` | Last update timestamp                               |

---

## API Endpoints

### 1. Create History Audio

**POST** `/api/admin/points/{pointId}/history-audios`

Creates a new history audio entry for the given Point.

**Path Parameters:**

- `pointId` — UUID of the parent Point

**Request Body:** `CreatePointHistoryAudio` (see above)

**Example Request:**

```http
POST /api/admin/points/7c9e6679-7425-40de-944b-e07fc1f90ae7/history-audios
Content-Type: application/json
Authorization: Bearer <token>

{
  "audioUrl": "https://res.cloudinary.com/demo/audio_narration.mp3",
  "historyText": "Welcome to the ancient pagoda...",
  "words": [
    { "text": "Welcome", "start": 0.0, "end": 0.35 },
    { "text": "to", "start": 0.35, "end": 0.45 },
    { "text": "the", "start": 0.45, "end": 0.55 },
    { "text": "ancient", "start": 0.55, "end": 0.95 },
    { "text": "pagoda", "start": 0.95, "end": 1.40 }
  ],
  "metadata": {
    "mode": "generate",
    "modelId": "eleven_multilingual_v2",
    "voiceId": "21m00Tcm4TlvDq8ikWAM",
    "language": "en"
  }
}
```

**Success Response (201 Created):**

```json
{
  "status": 201,
  "success": true,
  "message": "History audio created successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "pointId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "audioUrl": "https://res.cloudinary.com/demo/audio_narration.mp3",
    "historyText": "Welcome to the ancient pagoda...",
    "words": [
      { "text": "Welcome", "start": 0.0, "end": 0.35 },
      { "text": "to", "start": 0.35, "end": 0.45 }
    ],
    "metadata": {
      "mode": "generate",
      "modelId": "eleven_multilingual_v2",
      "voiceId": "21m00Tcm4TlvDq8ikWAM",
      "language": "en"
    },
    "createdAt": "2026-03-05T21:30:00",
    "updatedAt": "2026-03-05T21:30:00"
  },
  "timestamp": "2026-03-05T21:30:00"
}
```

---

### 2. Update History Audio

**PUT** `/api/admin/points/{pointId}/history-audios/{id}`

Updates an existing history audio entry.

**Path Parameters:**

- `pointId` — UUID of the parent Point
- `id` — UUID of the history audio to update

**Request Body:** `CreatePointHistoryAudio` (same structure as Create)

**Example Request:**

```http
PUT /api/admin/points/7c9e6679-7425-40de-944b-e07fc1f90ae7/history-audios/550e8400-e29b-41d4-a716-446655440000
Content-Type: application/json
Authorization: Bearer <token>

{
  "audioUrl": "https://res.cloudinary.com/demo/updated_audio.mp3",
  "historyText": "Updated narration text...",
  "words": [],
  "metadata": {
    "mode": "upload",
    "modelId": null,
    "voiceId": null,
    "language": "vi"
  }
}
```

**Success Response (200 OK):**

```json
{
  "status": 200,
  "success": true,
  "message": "History audio updated successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "pointId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "audioUrl": "https://res.cloudinary.com/demo/updated_audio.mp3",
    "historyText": "Updated narration text...",
    "words": [],
    "metadata": {
      "mode": "upload",
      "modelId": null,
      "voiceId": null,
      "language": "vi"
    },
    "createdAt": "2026-03-05T21:30:00",
    "updatedAt": "2026-03-05T22:00:00"
  },
  "timestamp": "2026-03-05T22:00:00"
}
```

---

### 3. Get History Audio by ID

**GET** `/api/admin/points/{pointId}/history-audios/{id}`

Retrieves a single history audio entry by its ID.

**Path Parameters:**

- `pointId` — UUID of the parent Point
- `id` — UUID of the history audio

**Example Request:**

```http
GET /api/admin/points/7c9e6679-7425-40de-944b-e07fc1f90ae7/history-audios/550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer <token>
```

**Success Response (200 OK):**

```json
{
  "status": 200,
  "success": true,
  "message": "Success",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "pointId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "audioUrl": "https://res.cloudinary.com/demo/audio_narration.mp3",
    "historyText": "Welcome to the ancient pagoda...",
    "words": [ ... ],
    "metadata": { ... },
    "createdAt": "2026-03-05T21:30:00",
    "updatedAt": "2026-03-05T21:30:00"
  },
  "timestamp": "2026-03-05T21:30:00"
}
```

---

### 4. Get All History Audios by Point

**GET** `/api/admin/points/{pointId}/history-audios`

Retrieves **all** history audio entries belonging to a specific Point.

**Path Parameters:**

- `pointId` — UUID of the parent Point

**Example Request:**

```http
GET /api/admin/points/7c9e6679-7425-40de-944b-e07fc1f90ae7/history-audios
Authorization: Bearer <token>
```

**Success Response (200 OK):**

```json
{
  "status": 200,
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "pointId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
      "audioUrl": "https://res.cloudinary.com/demo/audio_en.mp3",
      "historyText": "English narration...",
      "words": [ ... ],
      "metadata": {
        "mode": "generate",
        "modelId": "eleven_multilingual_v2",
        "voiceId": "21m00Tcm4TlvDq8ikWAM",
        "language": "en"
      },
      "createdAt": "2026-03-05T21:30:00",
      "updatedAt": "2026-03-05T21:30:00"
    },
    {
      "id": "661f9511-f30c-52e5-b827-557766551111",
      "pointId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
      "audioUrl": "https://res.cloudinary.com/demo/audio_vi.mp3",
      "historyText": "Bản tường thuật tiếng Việt...",
      "words": [ ... ],
      "metadata": {
        "mode": "upload",
        "modelId": null,
        "voiceId": null,
        "language": "vi"
      },
      "createdAt": "2026-03-05T21:45:00",
      "updatedAt": "2026-03-05T21:45:00"
    }
  ],
  "timestamp": "2026-03-05T21:50:00"
}
```

---

### 5. Delete History Audio

**DELETE** `/api/admin/points/{pointId}/history-audios/{id}`

Permanently deletes a history audio entry (hard delete, not soft delete).

**Path Parameters:**

- `pointId` — UUID of the parent Point
- `id` — UUID of the history audio to delete

**Example Request:**

```http
DELETE /api/admin/points/7c9e6679-7425-40de-944b-e07fc1f90ae7/history-audios/550e8400-e29b-41d4-a716-446655440000
Authorization: Bearer <token>
```

**Success Response (200 OK):**

```json
{
  "status": 200,
  "success": true,
  "message": "History audio deleted successfully",
  "data": null,
  "timestamp": "2026-03-05T22:00:00"
}
```

---

## Error Responses

### 404 Not Found — Point does not exist

```json
{
  "status": 404,
  "success": false,
  "message": "Point not found with id: '7c9e6679-7425-40de-944b-e07fc1f90ae7'",
  "data": null,
  "timestamp": "2026-03-05T22:00:00"
}
```

### 404 Not Found — History Audio does not exist

```json
{
  "status": 404,
  "success": false,
  "message": "PointHistoryAudio not found with id: '550e8400-e29b-41d4-a716-446655440000'",
  "data": null,
  "timestamp": "2026-03-05T22:00:00"
}
```

### 401 Unauthorized

```json
{
  "status": 401,
  "success": false,
  "message": "Unauthorized",
  "data": null
}
```

### 403 Forbidden

```json
{
  "status": 403,
  "success": false,
  "message": "Access Denied - Admin role required",
  "data": null
}
```

### 500 Internal Server Error — JSON Processing Failure

```json
{
  "status": 500,
  "success": false,
  "message": "Failed to serialize word timings",
  "data": null
}
```

---

## Endpoint Summary Table

| Method   | Path                                              | Description                      | Response `data` Type          |
| -------- | ------------------------------------------------- | -------------------------------- | ----------------------------- |
| `POST`   | `/api/admin/points/{pointId}/history-audios`      | Create a new history audio       | `PointHistoryAudioResponse`   |
| `PUT`    | `/api/admin/points/{pointId}/history-audios/{id}` | Update an existing history audio | `PointHistoryAudioResponse`   |
| `GET`    | `/api/admin/points/{pointId}/history-audios/{id}` | Get a single history audio       | `PointHistoryAudioResponse`   |
| `GET`    | `/api/admin/points/{pointId}/history-audios`      | List all audios for a Point      | `PointHistoryAudioResponse[]` |
| `DELETE` | `/api/admin/points/{pointId}/history-audios/{id}` | Delete a history audio           | `null`                        |

---

## Frontend Implementation Guide

This section provides step-by-step instructions for integrating with the History Audio API, following the existing FE codebase conventions.

### Step 1: Define TypeScript Types

Create `src/types/historyAudio.ts`:

```typescript
// ─── Word Timing ─────────────────────────────────────────────
export interface WordTiming {
  text: string;
  start: number;
  end: number;
}

// ─── Audio Metadata ──────────────────────────────────────────
export interface AudioMetadata {
  mode: "generate" | "upload";
  modelId: string | null;
  voiceId: string | null;
  language: string;
}

// ─── Request ─────────────────────────────────────────────────
export interface CreateHistoryAudioRequest {
  audioUrl: string;
  historyText: string;
  words: WordTiming[];
  metadata: AudioMetadata;
}

// ─── Response ────────────────────────────────────────────────
export interface HistoryAudioResponse {
  id: string;
  pointId: string;
  audioUrl: string;
  historyText: string;
  words: WordTiming[];
  metadata: AudioMetadata;
  createdAt: string;
  updatedAt: string;
}
```

Then re-export from `src/types/index.ts`:

```typescript
export * from "./historyAudio";
```

### Step 2: Create the API Service

Create `src/services/api/historyAudioService.ts`:

```typescript
import apiClient from "./apiClient";
import type { CreateHistoryAudioRequest, HistoryAudioResponse } from "@/types/historyAudio";
import type { ApiResponse } from "@/types";

const BASE = (pointId: string) => `/admin/points/${pointId}/history-audios`;

export const historyAudioService = {
  /** Create a new history audio for a point */
  create: (pointId: string, data: CreateHistoryAudioRequest) =>
    apiClient.post<ApiResponse<HistoryAudioResponse>>(BASE(pointId), data),

  /** Update an existing history audio */
  update: (pointId: string, id: string, data: CreateHistoryAudioRequest) =>
    apiClient.put<ApiResponse<HistoryAudioResponse>>(`${BASE(pointId)}/${id}`, data),

  /** Get a single history audio by ID */
  getById: (pointId: string, id: string) =>
    apiClient.get<ApiResponse<HistoryAudioResponse>>(`${BASE(pointId)}/${id}`),

  /** Get all history audios for a point */
  getAllByPoint: (pointId: string) =>
    apiClient.get<ApiResponse<HistoryAudioResponse[]>>(BASE(pointId)),

  /** Delete a history audio */
  delete: (pointId: string, id: string) =>
    apiClient.delete<ApiResponse<null>>(`${BASE(pointId)}/${id}`),
};
```

> **Note:** The `apiClient` already prepends `/api` via the Vite dev proxy, so the service paths start with `/admin/...` (without `/api`).

### Step 3: Create a Custom Hook

Create `src/hooks/historyAudio/useHistoryAudios.ts`:

```typescript
import { useState, useEffect, useCallback } from "react";
import { historyAudioService } from "@/services/api/historyAudioService";
import type { HistoryAudioResponse, CreateHistoryAudioRequest } from "@/types/historyAudio";
import { getApiErrorMessage } from "@/utils/getApiErrorMessage";

export function useHistoryAudios(pointId: string) {
  const [audios, setAudios] = useState<HistoryAudioResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // ─── Fetch all audios for the point ──────────────────────
  const fetchAudios = useCallback(async () => {
    if (!pointId) return;
    setLoading(true);
    setError(null);
    try {
      const res = await historyAudioService.getAllByPoint(pointId);
      setAudios(res.data.data);
    } catch (err) {
      setError(getApiErrorMessage(err, "Failed to load history audios"));
    } finally {
      setLoading(false);
    }
  }, [pointId]);

  useEffect(() => {
    fetchAudios();
  }, [fetchAudios]);

  // ─── Create ──────────────────────────────────────────────
  const createAudio = async (data: CreateHistoryAudioRequest) => {
    const res = await historyAudioService.create(pointId, data);
    await fetchAudios(); // refresh list
    return res.data.data;
  };

  // ─── Update ──────────────────────────────────────────────
  const updateAudio = async (id: string, data: CreateHistoryAudioRequest) => {
    const res = await historyAudioService.update(pointId, id, data);
    await fetchAudios();
    return res.data.data;
  };

  // ─── Delete ──────────────────────────────────────────────
  const deleteAudio = async (id: string) => {
    await historyAudioService.delete(pointId, id);
    await fetchAudios();
  };

  return {
    audios,
    loading,
    error,
    refetch: fetchAudios,
    createAudio,
    updateAudio,
    deleteAudio,
  };
}
```

### Step 4: Zod Validation Schema

For form validation with React Hook Form + Zod:

```typescript
import { z } from "zod";

const wordTimingSchema = z.object({
  text: z.string(),
  start: z.number().min(0),
  end: z.number().min(0),
});

const audioMetadataSchema = z.object({
  mode: z.enum(["generate", "upload"]),
  modelId: z.string().nullable(),
  voiceId: z.string().nullable(),
  language: z.string().min(1, "Language is required"),
});

export const historyAudioFormSchema = z.object({
  audioUrl: z.string().url("Must be a valid URL"),
  historyText: z.string().min(1, "Narration text is required"),
  words: z.array(wordTimingSchema).default([]),
  metadata: audioMetadataSchema,
});

export type HistoryAudioFormValues = z.infer<typeof historyAudioFormSchema>;
```

### Step 5: Build the UI Component

The component should handle **two modes**:

| Mode         | `metadata.mode` | `modelId` / `voiceId`     | Typical Flow                                                                                      |
| ------------ | --------------- | ------------------------- | ------------------------------------------------------------------------------------------------- |
| **Generate** | `"generate"`    | Populated from AI service | Admin enters text → calls AI TTS API → receives audio URL + word timings → saves via this API     |
| **Upload**   | `"upload"`      | `null`                    | Admin uploads an audio file to Cloudinary → optionally provides word timings → saves via this API |

#### Suggested Component Structure

```
src/pages/admin/destinations/components/
  └── historyAudio/
      ├── HistoryAudioPanel.tsx      ← Main panel (list + create/edit)
      ├── HistoryAudioList.tsx       ← Audio item cards
      ├── HistoryAudioForm.tsx       ← Create/Edit form (modal or inline)
      ├── HistoryAudioPlayer.tsx     ← Audio player with word highlight
      └── HistoryAudioDeleteDialog.tsx
```

#### Example: Integrating into the Destinations/Point Detail page

```tsx
import { useHistoryAudios } from "@/hooks/historyAudio/useHistoryAudios";

function PointDetailPage({ pointId }: { pointId: string }) {
  const { audios, loading, createAudio, updateAudio, deleteAudio } = useHistoryAudios(pointId);

  // ... render audio list, forms, players
}
```

### Step 6: File Organization Summary

Following the established FE conventions:

```
src/
├── types/
│   └── historyAudio.ts              ← Types/interfaces
├── services/api/
│   └── historyAudioService.ts       ← API service layer
├── hooks/historyAudio/
│   └── useHistoryAudios.ts          ← Data-fetching hook
└── pages/admin/destinations/
    └── components/historyAudio/
        ├── HistoryAudioPanel.tsx
        ├── HistoryAudioList.tsx
        ├── HistoryAudioForm.tsx
        ├── HistoryAudioPlayer.tsx
        └── HistoryAudioDeleteDialog.tsx
```

---

## Important Notes

1. **`pointId` in the URL**: The `pointId` is set from the URL path, **not** from the request body. The backend controller automatically injects it via `request.setPointId(pointId)`.

2. **`words` field behavior**: The `words` array is stored as a JSON string column in the database. The backend serializes on save and deserializes on read automatically. The frontend should always send and receive it as a normal array.

3. **`metadata` nullable fields**: When `mode` is `"upload"`, `modelId` and `voiceId` should be sent as `null`. The backend handles this gracefully.

4. **Hard delete**: The DELETE endpoint performs a **permanent delete** (not soft delete). The audio record is removed from the database entirely.

5. **No pagination**: The "Get All by Point" endpoint returns a flat array, not paginated. This is intentional since a Point typically has only a few audio entries (one per language).

6. **Cloudinary upload**: Audio files should be uploaded to Cloudinary **before** calling this API. The `audioUrl` field expects a fully-resolved Cloudinary URL. Use the existing `uploadVideoToCloudinary` utility or a similar audio upload function.

---

## Backend Implementation Files

| Layer                 | File                                                | Purpose                                                           |
| --------------------- | --------------------------------------------------- | ----------------------------------------------------------------- |
| **Entity**            | `entity/PointHistoryAudio.java`                     | JPA entity with `words` as JSON column                            |
| **Request DTO**       | `dto/request/point/CreatePointHistoryAudio.java`    | Request with nested `metadata` + `words`                          |
| **Response DTO**      | `dto/response/point/PointHistoryAudioResponse.java` | Unflattened response with metadata object                         |
| **Supporting DTOs**   | `dto/request/point/WordTimingRequest.java`          | Word timing data                                                  |
|                       | `dto/request/point/AudioMetadataRequest.java`       | Audio metadata (mode, model, voice, lang)                         |
| **Repository**        | `repository/PointHistoryAudioRepository.java`       | JPA repo with `findByPointId`                                     |
| **Service Interface** | `service/PointHistoryAudioService.java`             | CRUD contract                                                     |
| **Service Impl**      | `service/impl/PointHistoryAudioServiceImpl.java`    | JSON serialization, metadata flattening                           |
| **Controller**        | `controller/admin/AdminHistoryAudioController.java` | REST endpoints under `/api/admin/points/{pointId}/history-audios` |
