//package fpt.project.NeoNHS.controller;
//
//import fpt.project.NeoNHS.dto.response.point.MapPointResponse;
//import fpt.project.NeoNHS.enums.PointType;
//import fpt.project.NeoNHS.security.CustomUserDetailsService;
//import fpt.project.NeoNHS.security.JwtTokenProvider;
//import fpt.project.NeoNHS.service.PanoramaService;
//import fpt.project.NeoNHS.service.PointService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
//import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
//import org.springframework.dao.DataAccessResourceFailureException;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.NoSuchElementException;
//import java.util.UUID;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(PointController.class)
//@AutoConfigureMockMvc(addFilters = false)
//class PointControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockitoBean
//    private PointService pointService;
//
//    @MockitoBean
//    private PanoramaService panoramaService;
//
//    @MockitoBean
//    private JwtTokenProvider jwtTokenProvider;
//
//    @MockitoBean
//    private CustomUserDetailsService customUserDetailsService;
//
//    private MapPointResponse mapPoint1;
//    private MapPointResponse mapPoint2;
//
//    @BeforeEach
//    void setUp() {
//        mapPoint1 = buildMapPoint("Main Pagoda", PointType.PAGODA, 10.7626, 106.6602);
//        mapPoint2 = buildMapPoint("Workshop Spot", PointType.WORKSHOP, 10.7700, 106.6800);
//    }
//
//    private MapPointResponse buildMapPoint(String name, PointType type, Double latitude, Double longitude) {
//        return MapPointResponse.builder()
//                .id(UUID.randomUUID())
//                .name(name)
//                .description("Point for map view")
//                .thumbnailUrl("https://example.com/thumb.jpg")
//                .latitude(latitude)
//                .longitude(longitude)
//                .type(type)
//                .startTime(LocalDateTime.now().plusDays(1))
//                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
//                .maxParticipants(30)
//                .currentEnrolled(5)
//                .workshopOrganizerName("Organizer A")
//                .build();
//    }
//
//    // ==================== GET /api/points/map tests ====================
//
//    // ----- UTCID01: Normal — mixed map points list from points/events/workshops (200 OK) -----
//    @Test
//    @DisplayName("UTCID01 - Mixed map points list -> 200 OK")
//    void utcid01_mixedMapPoints_shouldReturn200() throws Exception {
//        MapPointResponse eventPoint = buildMapPoint("Festival Area", PointType.EVENT, 10.7811, 106.7000);
//        Mockito.when(pointService.getAllPointsOnMap()).thenReturn(List.of(mapPoint1, eventPoint, mapPoint2));
//
//        mockMvc.perform(get("/api/points/map"))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.status").value(200))
//                .andExpect(jsonPath("$.message").value("Success"))
//                .andExpect(jsonPath("$.data.length()").value(3))
//                .andExpect(jsonPath("$.data[0].type").value("PAGODA"))
//                .andExpect(jsonPath("$.data[1].type").value("EVENT"))
//                .andExpect(jsonPath("$.data[2].type").value("WORKSHOP"));
//
//        Mockito.verify(pointService).getAllPointsOnMap();
//    }
//
//    // ----- UTCID02: Boundary — empty list (200 OK) -----
//    @Test
//    @DisplayName("UTCID02 - Empty map points list -> 200 OK")
//    void utcid02_emptyList_shouldReturn200() throws Exception {
//        Mockito.when(pointService.getAllPointsOnMap()).thenReturn(List.of());
//
//        mockMvc.perform(get("/api/points/map"))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.data.length()").value(0));
//    }
//
//    // ----- UTCID03: Boundary — single point only (200 OK) -----
//    @Test
//    @DisplayName("UTCID03 - Single map point -> 200 OK")
//    void utcid03_singleItem_shouldReturn200() throws Exception {
//        Mockito.when(pointService.getAllPointsOnMap()).thenReturn(List.of(mapPoint1));
//
//        mockMvc.perform(get("/api/points/map"))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data.length()").value(1))
//                .andExpect(jsonPath("$.data[0].name").value("Main Pagoda"));
//    }
//
//    // ----- UTCID04: Boundary — large result list (200 OK) -----
//    @Test
//    @DisplayName("UTCID04 - Large result list -> 200 OK")
//    void utcid04_largeList_shouldReturn200() throws Exception {
//        List<MapPointResponse> manyPoints = new ArrayList<>();
//        for (int i = 0; i < 25; i++) {
//            manyPoints.add(buildMapPoint("Point-" + i, PointType.GENERAL, 10.0 + i, 106.0 + i));
//        }
//        Mockito.when(pointService.getAllPointsOnMap()).thenReturn(manyPoints);
//
//        mockMvc.perform(get("/api/points/map"))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.data.length()").value(25));
//    }
//
//    // ----- UTCID05: Boundary — optional fields missing from mapped object (200 OK) -----
//    @Test
//    @DisplayName("UTCID05 - Null optional fields -> 200 OK")
//    void utcid05_nullOptionalFields_shouldReturn200() throws Exception {
//        MapPointResponse minimal = MapPointResponse.builder()
//                .id(UUID.randomUUID())
//                .name("Minimal Point")
//                .type(PointType.GENERAL)
//                .latitude(10.0)
//                .longitude(106.0)
//                .build();
//        Mockito.when(pointService.getAllPointsOnMap()).thenReturn(List.of(minimal));
//
//        mockMvc.perform(get("/api/points/map"))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data[0].name").value("Minimal Point"));
//    }
//
//    // ----- UTCID06: Abnormal — repository/data source connectivity issue (500) -----
//    @Test
//    @DisplayName("UTCID06 - Data access failure -> 500 Internal Server Error")
//    void utcid06_dataAccessFailure_shouldReturn500() throws Exception {
//        Mockito.when(pointService.getAllPointsOnMap())
//                .thenThrow(new DataAccessResourceFailureException("Database unavailable"));
//
//        mockMvc.perform(get("/api/points/map"))
//                .andDo(print())
//                .andExpect(status().isInternalServerError())
//                .andExpect(jsonPath("$.success").value(false))
//                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
//    }
//
//    // ----- UTCID07: Abnormal — generic runtime from query/specification layer (500) -----
//    @Test
//    @DisplayName("UTCID07 - Runtime exception in service flow -> 500 Internal Server Error")
//    void utcid07_runtimeException_shouldReturn500() throws Exception {
//        Mockito.when(pointService.getAllPointsOnMap())
//                .thenThrow(new RuntimeException("Point repository query failed"));
//
//        mockMvc.perform(get("/api/points/map"))
//                .andDo(print())
//                .andExpect(status().isInternalServerError())
//                .andExpect(jsonPath("$.success").value(false));
//    }
//
//    // ----- UTCID08: Abnormal — null relation while mapping point/checkins/history audios (500) -----
//    @Test
//    @DisplayName("UTCID08 - NullPointerException in mapping flow -> 500 Internal Server Error")
//    void utcid08_nullPointerMappingFlow_shouldReturn500() throws Exception {
//        Mockito.when(pointService.getAllPointsOnMap())
//                .thenThrow(new NullPointerException("Point latitude is null"));
//
//        mockMvc.perform(get("/api/points/map"))
//                .andDo(print())
//                .andExpect(status().isInternalServerError())
//                .andExpect(jsonPath("$.success").value(false));
//    }
//
//    // ----- UTCID09: Abnormal — workshop mapping uses first() on empty collection (500) -----
//    @Test
//    @DisplayName("UTCID09 - NoSuchElementException in workshop mapping -> 500 Internal Server Error")
//    void utcid09_noSuchElementWorkshopFlow_shouldReturn500() throws Exception {
//        Mockito.when(pointService.getAllPointsOnMap())
//                .thenThrow(new NoSuchElementException("No workshop sessions available"));
//
//        mockMvc.perform(get("/api/points/map"))
//                .andDo(print())
//                .andExpect(status().isInternalServerError())
//                .andExpect(jsonPath("$.success").value(false));
//    }
//
//    // ----- UTCID10: Abnormal — auth/helper context issue in silent current-user lookup (500) -----
//    @Test
//    @DisplayName("UTCID10 - IllegalStateException in auth helper flow -> 500 Internal Server Error")
//    void utcid10_authContextIssue_shouldReturn500() throws Exception {
//        Mockito.when(pointService.getAllPointsOnMap())
//                .thenThrow(new IllegalStateException("Security context is corrupted"));
//
//        mockMvc.perform(get("/api/points/map"))
//                .andDo(print())
//                .andExpect(status().isInternalServerError())
//                .andExpect(jsonPath("$.success").value(false));
//    }
//
//    // ----- UTCID11: Normal — event/workshop time fields are returned (200 OK) -----
//    @Test
//    @DisplayName("UTCID11 - startTime and endTime are serialized -> 200 OK")
//    void utcid11_timeFieldsSerialized_shouldReturn200() throws Exception {
//        MapPointResponse eventPoint = buildMapPoint("Time Point", PointType.EVENT, 10.75, 106.67);
//        Mockito.when(pointService.getAllPointsOnMap()).thenReturn(List.of(eventPoint));
//
//        mockMvc.perform(get("/api/points/map"))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.data[0].startTime").exists())
//                .andExpect(jsonPath("$.data[0].endTime").exists());
//    }
//
//    // ----- UTCID12: Boundary — duplicate map points are preserved in response (200 OK) -----
//    @Test
//    @DisplayName("UTCID12 - Duplicate map points remain in list -> 200 OK")
//    void utcid12_duplicatePointsPreserved_shouldReturn200() throws Exception {
//        Mockito.when(pointService.getAllPointsOnMap()).thenReturn(List.of(mapPoint1, mapPoint1));
//
//        mockMvc.perform(get("/api/points/map"))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data.length()").value(2))
//                .andExpect(jsonPath("$.data[0].id").value(mapPoint1.getId().toString()))
//                .andExpect(jsonPath("$.data[1].id").value(mapPoint1.getId().toString()));
//    }
//
//    // ----- UTCID13: Boundary — list containing null entry is handled (200 OK) -----
//    @Test
//    @DisplayName("UTCID13 - List with null item -> 200 OK")
//    void utcid13_listWithNullItem_shouldReturn200() throws Exception {
//        Mockito.when(pointService.getAllPointsOnMap()).thenReturn(Arrays.asList(mapPoint1, null));
//
//        mockMvc.perform(get("/api/points/map"))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.data.length()").value(2));
//    }
//
//    // ----- UTCID14: Abnormal — unsupported operation from service (500) -----
//    @Test
//    @DisplayName("UTCID14 - UnsupportedOperationException -> 500 Internal Server Error")
//    void utcid14_unsupportedOperation_shouldReturn500() throws Exception {
//        Mockito.when(pointService.getAllPointsOnMap())
//                .thenThrow(new UnsupportedOperationException("Map source not supported"));
//
//        mockMvc.perform(get("/api/points/map"))
//                .andDo(print())
//                .andExpect(status().isInternalServerError())
//                .andExpect(jsonPath("$.success").value(false))
//                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
//    }
//
//    // ----- UTCID15: Abnormal — concurrent modification in service aggregation flow (500) -----
//    @Test
//    @DisplayName("UTCID15 - ConcurrentModificationException -> 500 Internal Server Error")
//    void utcid15_concurrentModification_shouldReturn500() throws Exception {
//        Mockito.when(pointService.getAllPointsOnMap())
//                .thenThrow(new java.util.ConcurrentModificationException("Points list changed during mapping"));
//
//        mockMvc.perform(get("/api/points/map"))
//                .andDo(print())
//                .andExpect(status().isInternalServerError())
//                .andExpect(jsonPath("$.success").value(false));
//    }
//}
//
//
//
