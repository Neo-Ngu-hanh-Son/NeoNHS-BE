package fpt.project.NeoNHS.controller.audioguide;

import com.fasterxml.jackson.databind.ObjectMapper;
import fpt.project.NeoNHS.controller.HistoryAudioController;
import fpt.project.NeoNHS.dto.response.point.PointHistoryAudioResponse;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.exception.InvalidTokenException;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.exception.UnauthorizedException;
import fpt.project.NeoNHS.security.CustomUserDetailsService;
import fpt.project.NeoNHS.security.JwtTokenProvider;
import fpt.project.NeoNHS.service.PointHistoryAudioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HistoryAudioController.class)
@AutoConfigureMockMvc(addFilters = false)
class HistoryAudioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PointHistoryAudioService pointHistoryAudioService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @SuppressWarnings("unused")
    private final ObjectMapper objectMapper = new ObjectMapper();

    private UUID pointId;
    private UUID audioId;
    private UUID zeroUuid;
    private UUID maxUuid;
    private PointHistoryAudioResponse mockResponse;

    @BeforeEach
    void setUp() {
        pointId = UUID.randomUUID();
        audioId = UUID.randomUUID();
        zeroUuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
        maxUuid = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
        mockResponse = buildResponse(audioId, pointId);
    }

    private PointHistoryAudioResponse buildResponse(UUID id, UUID pId) {
        return PointHistoryAudioResponse.builder()
                .id(id)
                .pointId(pId)
                .audioUrl("https://cdn.example.com/audio/history-1.mp3")
                .historyText("Historic story")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Get History Audio By ID Tests")
    class GetHistoryAudioByIdTests {

        // ----- UTCID01: Normal — valid pointId + id (200 OK) -----
        @Test
        @DisplayName("UTCID01 - Valid pointId and id -> 200 OK")
        void utcid01_validPointIdAndId_shouldReturn200() throws Exception {
            Mockito.when(pointHistoryAudioService.getByPointIdAndId(pointId, audioId))
                    .thenReturn(mockResponse);

            mockMvc.perform(get("/api/points/{pointId}/history-audios/{id}", pointId, audioId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("Success"))
                    .andExpect(jsonPath("$.data.id").value(audioId.toString()));
        }

        // ----- UTCID02: Boundary — all-zero UUIDs (200 OK) -----
        @Test
        @DisplayName("UTCID02 - Boundary with all-zero UUIDs -> 200 OK")
        void utcid02_zeroUuids_shouldReturn200() throws Exception {
            PointHistoryAudioResponse boundaryResponse = buildResponse(zeroUuid, zeroUuid);
            Mockito.when(pointHistoryAudioService.getByPointIdAndId(zeroUuid, zeroUuid))
                    .thenReturn(boundaryResponse);

            mockMvc.perform(get("/api/points/{pointId}/history-audios/{id}", zeroUuid, zeroUuid))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.pointId").value(zeroUuid.toString()));
        }

        // ----- UTCID03: Abnormal — audio id not found (404 Not Found) -----
        @Test
        @DisplayName("UTCID03 - Audio id not found -> 404 Not Found")
        void utcid03_audioIdNotFound_shouldReturn404() throws Exception {
            Mockito.when(pointHistoryAudioService.getByPointIdAndId(pointId, audioId))
                    .thenThrow(new ResourceNotFoundException("PointHistoryAudio", "id", audioId));

            mockMvc.perform(get("/api/points/{pointId}/history-audios/{id}", pointId, audioId))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("PointHistoryAudio not found with id: '" + audioId + "'"));
        }

        // ----- UTCID04: Abnormal — point id not found (404 Not Found) -----
        @Test
        @DisplayName("UTCID04 - Point id not found -> 404 Not Found")
        void utcid04_pointIdNotFound_shouldReturn404() throws Exception {
            Mockito.when(pointHistoryAudioService.getByPointIdAndId(pointId, audioId))
                    .thenThrow(new ResourceNotFoundException("Point", "id", pointId));

            mockMvc.perform(get("/api/points/{pointId}/history-audios/{id}", pointId, audioId))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Point not found with id: '" + pointId + "'"));
        }

        // ----- UTCID05: Abnormal — invalid pointId format (500 Internal Server Error) -----
        @Test
        @DisplayName("UTCID05 - Invalid pointId format -> 500 Internal Server Error")
        void utcid05_invalidPointIdFormat_shouldReturn500() throws Exception {
            mockMvc.perform(get("/api/points/{pointId}/history-audios/{id}", "invalid-uuid", audioId))
                    .andDo(print())
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("An unexpected error occurred"));

            Mockito.verifyNoInteractions(pointHistoryAudioService);
        }

        // ----- UTCID06: Abnormal — invalid id format (500 Internal Server Error) -----
        @Test
        @DisplayName("UTCID06 - Invalid id format -> 500 Internal Server Error")
        void utcid06_invalidIdFormat_shouldReturn500() throws Exception {
            mockMvc.perform(get("/api/points/{pointId}/history-audios/{id}", pointId, "invalid-uuid"))
                    .andDo(print())
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("An unexpected error occurred"));

            Mockito.verifyNoInteractions(pointHistoryAudioService);
        }

        // ----- UTCID07: Abnormal — service throws IllegalArgumentException (400 Bad Request) -----
        @Test
        @DisplayName("UTCID07 - Illegal argument from service -> 400 Bad Request")
        void utcid07_illegalArgument_shouldReturn400() throws Exception {
            Mockito.when(pointHistoryAudioService.getByPointIdAndId(pointId, audioId))
                    .thenThrow(new IllegalArgumentException("Point and audio id mismatch"));

            mockMvc.perform(get("/api/points/{pointId}/history-audios/{id}", pointId, audioId))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Point and audio id mismatch"));
        }

        // ----- UTCID08: Abnormal — service throws BadRequestException (400 Bad Request) -----
        @Test
        @DisplayName("UTCID08 - Bad request from service -> 400 Bad Request")
        void utcid08_badRequestException_shouldReturn400() throws Exception {
            Mockito.when(pointHistoryAudioService.getByPointIdAndId(pointId, audioId))
                    .thenThrow(new BadRequestException("Audio is still processing"));

            mockMvc.perform(get("/api/points/{pointId}/history-audios/{id}", pointId, audioId))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Audio is still processing"));
        }

        // ----- UTCID09: Abnormal — service throws UnauthorizedException (401 Unauthorized) -----
        @Test
        @DisplayName("UTCID09 - Unauthorized exception -> 401 Unauthorized")
        void utcid09_unauthorized_shouldReturn401() throws Exception {
            Mockito.when(pointHistoryAudioService.getByPointIdAndId(pointId, audioId))
                    .thenThrow(new UnauthorizedException("You are not allowed to access this audio"));

            mockMvc.perform(get("/api/points/{pointId}/history-audios/{id}", pointId, audioId))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("You are not allowed to access this audio"));
        }

        // ----- UTCID10: Abnormal — service throws InvalidTokenException (401 Unauthorized) -----
        @Test
        @DisplayName("UTCID10 - Invalid token exception -> 401 Unauthorized")
        void utcid10_invalidToken_shouldReturn401() throws Exception {
            Mockito.when(pointHistoryAudioService.getByPointIdAndId(pointId, audioId))
                    .thenThrow(new InvalidTokenException("Token expired"));

            mockMvc.perform(get("/api/points/{pointId}/history-audios/{id}", pointId, audioId))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Token expired"));
        }

        // ----- UTCID11: Abnormal — DB connection failure (500 Internal Server Error) -----
        @Test
        @DisplayName("UTCID11 - DB connection failure -> 500 Internal Server Error")
        void utcid11_dbFailure_shouldReturn500() throws Exception {
            Mockito.when(pointHistoryAudioService.getByPointIdAndId(pointId, audioId))
                    .thenThrow(new DataAccessResourceFailureException("DB unavailable"));

            mockMvc.perform(get("/api/points/{pointId}/history-audios/{id}", pointId, audioId))
                    .andDo(print())
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
        }

        // ----- UTCID12: Abnormal — runtime exception (500 Internal Server Error) -----
        @Test
        @DisplayName("UTCID12 - Runtime exception -> 500 Internal Server Error")
        void utcid12_runtimeException_shouldReturn500() throws Exception {
            Mockito.when(pointHistoryAudioService.getByPointIdAndId(pointId, audioId))
                    .thenThrow(new RuntimeException("Unexpected runtime issue"));

            mockMvc.perform(get("/api/points/{pointId}/history-audios/{id}", pointId, audioId))
                    .andDo(print())
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false));
        }

        // ----- UTCID13: Abnormal — null pointer exception (500 Internal Server Error) -----
        @Test
        @DisplayName("UTCID13 - NullPointerException from service -> 500 Internal Server Error")
        void utcid13_nullPointer_shouldReturn500() throws Exception {
            Mockito.when(pointHistoryAudioService.getByPointIdAndId(pointId, audioId))
                    .thenThrow(new NullPointerException("Null content"));

            mockMvc.perform(get("/api/points/{pointId}/history-audios/{id}", pointId, audioId))
                    .andDo(print())
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false));
        }

        // ----- UTCID14: Abnormal — non-uuid text as path variable (500 Internal Server Error) -----
        @Test
        @DisplayName("UTCID14 - Non-UUID text path value -> 500 Internal Server Error")
        void utcid14_nonUuidText_shouldReturn500() throws Exception {
            mockMvc.perform(get("/api/points/{pointId}/history-audios/{id}", "point-123", "audio-123"))
                    .andDo(print())
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("An unexpected error occurred"));

            Mockito.verifyNoInteractions(pointHistoryAudioService);
        }

        // ----- UTCID15: Boundary — max UUID value (200 OK) -----
        @Test
        @DisplayName("UTCID15 - Boundary with max UUID value -> 200 OK")
        void utcid15_maxUuid_shouldReturn200() throws Exception {
            PointHistoryAudioResponse boundaryResponse = buildResponse(maxUuid, maxUuid);
            Mockito.when(pointHistoryAudioService.getByPointIdAndId(maxUuid, maxUuid))
                    .thenReturn(boundaryResponse);

            mockMvc.perform(get("/api/points/{pointId}/history-audios/{id}", maxUuid, maxUuid))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(maxUuid.toString()));
        }
    }

    @Nested
    @DisplayName("Get All History Audios By Point Tests")
    class GetAllHistoryAudiosByPointTests {

        // ----- UTCID01: Normal — point has history audios (200 OK) -----
        @Test
        @DisplayName("UTCID01 - Point has history audios -> 200 OK")
        void utcid01_pointHasHistoryAudios_shouldReturn200() throws Exception {
            List<PointHistoryAudioResponse> responses = List.of(
                    buildResponse(UUID.randomUUID(), pointId),
                    buildResponse(UUID.randomUUID(), pointId)
            );
            Mockito.when(pointHistoryAudioService.getAllByPointId(pointId))
                    .thenReturn(responses);

            mockMvc.perform(get("/api/points/{pointId}/history-audios", pointId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("Success"))
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        // ----- UTCID02: Boundary — empty list (200 OK) -----
        @Test
        @DisplayName("UTCID02 - Empty list boundary -> 200 OK")
        void utcid02_emptyList_shouldReturn200() throws Exception {
            Mockito.when(pointHistoryAudioService.getAllByPointId(pointId))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/points/{pointId}/history-audios", pointId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        // ----- UTCID03: Boundary — all-zero UUID for pointId (200 OK) -----
        @Test
        @DisplayName("UTCID03 - Boundary with zero UUID pointId -> 200 OK")
        void utcid03_zeroPointId_shouldReturn200() throws Exception {
            Mockito.when(pointHistoryAudioService.getAllByPointId(zeroUuid))
                    .thenReturn(List.of(buildResponse(UUID.randomUUID(), zeroUuid)));

            mockMvc.perform(get("/api/points/{pointId}/history-audios", zeroUuid))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.length()").value(1));
        }

        // ----- UTCID04: Abnormal — point not found (404 Not Found) -----
        @Test
        @DisplayName("UTCID04 - Point not found -> 404 Not Found")
        void utcid04_pointNotFound_shouldReturn404() throws Exception {
            Mockito.when(pointHistoryAudioService.getAllByPointId(pointId))
                    .thenThrow(new ResourceNotFoundException("Point", "id", pointId));

            mockMvc.perform(get("/api/points/{pointId}/history-audios", pointId))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Point not found with id: '" + pointId + "'"));
        }

        // ----- UTCID05: Abnormal — invalid pointId format (500 Internal Server Error) -----
        @Test
        @DisplayName("UTCID05 - Invalid pointId format -> 500 Internal Server Error")
        void utcid05_invalidPointIdFormat_shouldReturn500() throws Exception {
            mockMvc.perform(get("/api/points/{pointId}/history-audios", "invalid-point-id"))
                    .andDo(print())
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("An unexpected error occurred"));

            Mockito.verifyNoInteractions(pointHistoryAudioService);
        }

        // ----- UTCID06: Abnormal — service throws IllegalArgumentException (400 Bad Request) -----
        @Test
        @DisplayName("UTCID06 - Illegal argument from service -> 400 Bad Request")
        void utcid06_illegalArgument_shouldReturn400() throws Exception {
            Mockito.when(pointHistoryAudioService.getAllByPointId(pointId))
                    .thenThrow(new IllegalArgumentException("Invalid point state"));

            mockMvc.perform(get("/api/points/{pointId}/history-audios", pointId))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Invalid point state"));
        }

        // ----- UTCID07: Abnormal — service throws BadRequestException (400 Bad Request) -----
        @Test
        @DisplayName("UTCID07 - Bad request from service -> 400 Bad Request")
        void utcid07_badRequestException_shouldReturn400() throws Exception {
            Mockito.when(pointHistoryAudioService.getAllByPointId(pointId))
                    .thenThrow(new BadRequestException("Point is archived"));

            mockMvc.perform(get("/api/points/{pointId}/history-audios", pointId))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Point is archived"));
        }

        // ----- UTCID08: Abnormal — unauthorized access (401 Unauthorized) -----
        @Test
        @DisplayName("UTCID08 - Unauthorized exception -> 401 Unauthorized")
        void utcid08_unauthorized_shouldReturn401() throws Exception {
            Mockito.when(pointHistoryAudioService.getAllByPointId(pointId))
                    .thenThrow(new UnauthorizedException("Please login first"));

            mockMvc.perform(get("/api/points/{pointId}/history-audios", pointId))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Please login first"));
        }

        // ----- UTCID09: Abnormal — invalid token (401 Unauthorized) -----
        @Test
        @DisplayName("UTCID09 - Invalid token exception -> 401 Unauthorized")
        void utcid09_invalidToken_shouldReturn401() throws Exception {
            Mockito.when(pointHistoryAudioService.getAllByPointId(pointId))
                    .thenThrow(new InvalidTokenException("JWT is invalid"));

            mockMvc.perform(get("/api/points/{pointId}/history-audios", pointId))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("JWT is invalid"));
        }

        // ----- UTCID10: Abnormal — DB connection failure (500 Internal Server Error) -----
        @Test
        @DisplayName("UTCID10 - DB connection failure -> 500 Internal Server Error")
        void utcid10_dbFailure_shouldReturn500() throws Exception {
            Mockito.when(pointHistoryAudioService.getAllByPointId(pointId))
                    .thenThrow(new DataAccessResourceFailureException("Database down"));

            mockMvc.perform(get("/api/points/{pointId}/history-audios", pointId))
                    .andDo(print())
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false));
        }

        // ----- UTCID11: Abnormal — runtime exception (500 Internal Server Error) -----
        @Test
        @DisplayName("UTCID11 - Runtime exception -> 500 Internal Server Error")
        void utcid11_runtimeException_shouldReturn500() throws Exception {
            Mockito.when(pointHistoryAudioService.getAllByPointId(pointId))
                    .thenThrow(new RuntimeException("Unexpected list failure"));

            mockMvc.perform(get("/api/points/{pointId}/history-audios", pointId))
                    .andDo(print())
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false));
        }

        // ----- UTCID12: Abnormal — null pointer exception (500 Internal Server Error) -----
        @Test
        @DisplayName("UTCID12 - NullPointerException -> 500 Internal Server Error")
        void utcid12_nullPointer_shouldReturn500() throws Exception {
            Mockito.when(pointHistoryAudioService.getAllByPointId(pointId))
                    .thenThrow(new NullPointerException("Unexpected null"));

            mockMvc.perform(get("/api/points/{pointId}/history-audios", pointId))
                    .andDo(print())
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false));
        }

        // ----- UTCID13: Abnormal — unsupported operation (500 Internal Server Error) -----
        @Test
        @DisplayName("UTCID13 - Unsupported operation from service -> 500 Internal Server Error")
        void utcid13_unsupportedOperation_shouldReturn500() throws Exception {
            Mockito.when(pointHistoryAudioService.getAllByPointId(pointId))
                    .thenThrow(new UnsupportedOperationException("Read operation disabled"));

            mockMvc.perform(get("/api/points/{pointId}/history-audios", pointId))
                    .andDo(print())
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false));
        }

        // ----- UTCID14: Abnormal — malformed UUID without hyphens (500 Internal Server Error) -----
        @Test
        @DisplayName("UTCID14 - UUID without hyphens -> 500 Internal Server Error")
        void utcid14_uuidWithoutHyphens_shouldReturn500() throws Exception {
            mockMvc.perform(get("/api/points/{pointId}/history-audios", "123e4567e89b12d3a456426614174000"))
                    .andDo(print())
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("An unexpected error occurred"));

            Mockito.verifyNoInteractions(pointHistoryAudioService);
        }

        // ----- UTCID15: Abnormal — malformed UUID too short (500 Internal Server Error) -----
        @Test
        @DisplayName("UTCID15 - Too short UUID -> 500 Internal Server Error")
        void utcid15_uuidTooShort_shouldReturn500() throws Exception {
            mockMvc.perform(get("/api/points/{pointId}/history-audios", "1234"))
                    .andDo(print())
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message").value("An unexpected error occurred"));

            Mockito.verifyNoInteractions(pointHistoryAudioService);
        }
    }
}




