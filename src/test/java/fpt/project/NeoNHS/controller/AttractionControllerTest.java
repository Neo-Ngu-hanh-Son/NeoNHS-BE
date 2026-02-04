package fpt.project.NeoNHS.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fpt.project.NeoNHS.dto.response.AttractionResponse;
import fpt.project.NeoNHS.dto.response.PagedResponse;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.service.AttractionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AttractionController.
 * Uses @WebMvcTest to test only the web layer with mocked dependencies.
 */
@WebMvcTest(AttractionController.class)
@DisplayName("AttractionController Unit Tests")
@AutoConfigureMockMvc(addFilters = false)
class AttractionControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private AttractionService attractionService;

        private static final String BASE_URL = "/api/public/attractions";

        private AttractionResponse sampleAttraction;
        private UUID sampleId;

        @BeforeEach
        void setUp() {
                sampleId = UUID.randomUUID();
                sampleAttraction = AttractionResponse.builder()
                                .id(sampleId)
                                .name("Thủy Sơn (Water Mountain)")
                                .description("The largest and most famous of the five Marble Mountains.")
                                .address("Hòa Hải, Ngũ Hành Sơn, Đà Nẵng, Vietnam")
                                .latitude(new BigDecimal("16.0034"))
                                .longitude(new BigDecimal("108.2636"))
                                .status("OPEN")
                                .thumbnailUrl("https://example.com/thuyson.jpg")
                                .openHour(LocalTime.of(7, 0))
                                .closeHour(LocalTime.of(17, 30))
                                .pointCount(5)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();
        }

        @Nested
        @DisplayName("GET /api/public/attractions")
        class GetAllAttractions {
                @Test
                @DisplayName("Should return paginated attractions with default parameters")
                void getAllAttractions_WithDefaultParams_ReturnsPagedResponse() throws Exception {
                        // Given
                        PagedResponse<AttractionResponse> pagedResponse = PagedResponse.<AttractionResponse>builder()
                                        .content(List.of(sampleAttraction))
                                        .page(0)
                                        .size(10)
                                        .totalElements(1)
                                        .totalPages(1)
                                        .first(true)
                                        .last(true)
                                        .empty(false)
                                        .build();

                        given(attractionService.getAllActiveAttractions(null, 0, 10, "name", "asc"))
                                        .willReturn(pagedResponse);

                        // When
                        ResultActions result = mockMvc.perform(get(BASE_URL)
                                        .with(csrf()));

                        // Then
                        result.andDo(print())
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.success", is(true)))
                                        .andExpect(jsonPath("$.status", is(200)))
                                        .andExpect(jsonPath("$.message", is("Attractions retrieved successfully")))
                                        .andExpect(jsonPath("$.data.content", hasSize(1)))
                                        .andExpect(jsonPath("$.data.content[0].name", is("Thủy Sơn (Water Mountain)")))
                                        .andExpect(jsonPath("$.data.page", is(0)))
                                        .andExpect(jsonPath("$.data.size", is(10)))
                                        .andExpect(jsonPath("$.data.totalElements", is(1)))
                                        .andExpect(jsonPath("$.data.first", is(true)))
                                        .andExpect(jsonPath("$.data.last", is(true)));

                        verify(attractionService, times(1)).getAllActiveAttractions(null, 0, 10, "name", "asc");
                }

                // @Test
                // @DisplayName("Should return paginated attractions with custom parameters")
                // void getAllAttractions_WithCustomParams_ReturnsFilteredPagedResponse() throws
                // Exception {
                // // Given
                // PagedResponse<AttractionResponse> pagedResponse =
                // PagedResponse.<AttractionResponse>builder()
                // .content(List.of(sampleAttraction))
                // .page(1)
                // .size(5)
                // .totalElements(6)
                // .totalPages(2)
                // .first(false)
                // .last(true)
                // .empty(false)
                // .build();

                // given(attractionService.getAllActiveAttractions("water", 1, 5, "createdAt",
                // "desc"))
                // .willReturn(pagedResponse);

                // // When
                // ResultActions result = mockMvc.perform(get(BASE_URL)
                // .param("keyword", "water")
                // .param("page", "1")
                // .param("size", "5")
                // .param("sortBy", "createdAt")
                // .param("sortDir", "desc")
                // .with(csrf()));

                // // Then
                // result.andDo(print())
                // .andExpect(status().isOk())
                // .andExpect(jsonPath("$.success", is(true)))
                // .andExpect(jsonPath("$.data.page", is(1)))
                // .andExpect(jsonPath("$.data.size", is(5)))
                // .andExpect(jsonPath("$.data.totalPages", is(2)))
                // .andExpect(jsonPath("$.data.first", is(false)))
                // .andExpect(jsonPath("$.data.last", is(true)));

                // verify(attractionService, times(1)).getAllActiveAttractions("water", 1, 5,
                // "createdAt", "desc");
                // }

                // @Test
                // @DisplayName("Should return empty list when no attractions found")
                // void getAllAttractions_WhenNoAttractions_ReturnsEmptyList() throws Exception
                // {
                // // Given
                // PagedResponse<AttractionResponse> emptyResponse =
                // PagedResponse.<AttractionResponse>builder()
                // .content(Collections.emptyList())
                // .page(0)
                // .size(10)
                // .totalElements(0)
                // .totalPages(0)
                // .first(true)
                // .last(true)
                // .empty(true)
                // .build();

                // given(attractionService.getAllActiveAttractions(any(), anyInt(), anyInt(),
                // anyString(), anyString()))
                // .willReturn(emptyResponse);

                // // When
                // ResultActions result = mockMvc.perform(get(BASE_URL)
                // .with(csrf()));

                // // Then
                // result.andDo(print())
                // .andExpect(status().isOk())
                // .andExpect(jsonPath("$.success", is(true)))
                // .andExpect(jsonPath("$.data.content", hasSize(0)))
                // .andExpect(jsonPath("$.data.empty", is(true)))
                // .andExpect(jsonPath("$.data.totalElements", is(0)));
                // }

                // @Test
                // @DisplayName("Should return attractions with keyword search")
                // void getAllAttractions_WithKeyword_ReturnsFilteredResults() throws Exception
                // {
                // // Given
                // AttractionResponse mocSon = AttractionResponse.builder()
                // .id(UUID.randomUUID())
                // .name("Mộc Sơn (Wood Mountain)")
                // .description("Wood element mountain")
                // .status("OPEN")
                // .build();

                // PagedResponse<AttractionResponse> pagedResponse =
                // PagedResponse.<AttractionResponse>builder()
                // .content(List.of(mocSon))
                // .page(0)
                // .size(10)
                // .totalElements(1)
                // .totalPages(1)
                // .first(true)
                // .last(true)
                // .empty(false)
                // .build();

                // given(attractionService.getAllActiveAttractions(eq("Mộc"), anyInt(),
                // anyInt(), anyString(), anyString()))
                // .willReturn(pagedResponse);

                // // When
                // ResultActions result = mockMvc.perform(get(BASE_URL)
                // .param("keyword", "Mộc")
                // .with(csrf()));

                // // Then
                // result.andDo(print())
                // .andExpect(status().isOk())
                // .andExpect(jsonPath("$.data.content", hasSize(1)))
                // .andExpect(jsonPath("$.data.content[0].name", containsString("Mộc Sơn")));
                // }
                // }

                // @Nested
                // @DisplayName("GET /api/public/attractions/{id}")
                // class GetAttractionById {

                // @Test
                // @DisplayName("Should return attraction when found by ID")
                // void getAttractionById_WhenExists_ReturnsAttraction() throws Exception {
                // // Given
                // given(attractionService.getActiveAttractionById(sampleId))
                // .willReturn(sampleAttraction);

                // // When
                // ResultActions result = mockMvc.perform(get(BASE_URL + "/{id}", sampleId)
                // .with(csrf()));

                // // Then
                // result.andDo(print())
                // .andExpect(status().isOk())
                // .andExpect(jsonPath("$.success", is(true)))
                // .andExpect(jsonPath("$.status", is(200)))
                // .andExpect(jsonPath("$.message", is("Attraction retrieved successfully")))
                // .andExpect(jsonPath("$.data.id", is(sampleId.toString())))
                // .andExpect(jsonPath("$.data.name", is("Thủy Sơn (Water Mountain)")))
                // .andExpect(jsonPath("$.data.status", is("OPEN")))
                // .andExpect(jsonPath("$.data.pointCount", is(5)));

                // verify(attractionService, times(1)).getActiveAttractionById(sampleId);
                // }

                // @Test
                // @DisplayName("Should return 404 when attraction not found")
                // void getAttractionById_WhenNotExists_Returns404() throws Exception {
                // // Given
                // UUID nonExistentId = UUID.randomUUID();
                // given(attractionService.getActiveAttractionById(nonExistentId))
                // .willThrow(new ResourceNotFoundException("Attraction", "id",
                // nonExistentId.toString()));

                // // When
                // ResultActions result = mockMvc.perform(get(BASE_URL + "/{id}", nonExistentId)
                // .with(csrf()));

                // // Then
                // result.andDo(print())
                // .andExpect(status().isNotFound());

                // verify(attractionService, times(1)).getActiveAttractionById(nonExistentId);
                // }

                // @Test
                // @DisplayName("Should return 400 for invalid UUID format")
                // void getAttractionById_WithInvalidUUID_Returns400() throws Exception {
                // // When
                // ResultActions result = mockMvc.perform(get(BASE_URL + "/{id}",
                // "invalid-uuid")
                // .with(csrf()));

                // // Then
                // result.andDo(print())
                // .andExpect(status().isBadRequest());
                // }
                // }

                // @Nested
                // @DisplayName("GET /api/public/attractions/count")
                // class GetAttractionCount {

                // @Test
                // @DisplayName("Should return count of active attractions")
                // void getAttractionCount_ReturnsCount() throws Exception {
                // // Given
                // given(attractionService.countActiveAttractions()).willReturn(5L);

                // // When
                // ResultActions result = mockMvc.perform(get(BASE_URL + "/count")
                // .with(csrf()));

                // // Then
                // result.andDo(print())
                // .andExpect(status().isOk())
                // .andExpect(jsonPath("$.success", is(true)))
                // .andExpect(jsonPath("$.status", is(200)))
                // .andExpect(jsonPath("$.message", is("Attraction count retrieved
                // successfully")))
                // .andExpect(jsonPath("$.data", is(5)));

                // verify(attractionService, times(1)).countActiveAttractions();
                // }

                // @Test
                // @DisplayName("Should return zero when no active attractions")
                // void getAttractionCount_WhenNoAttractions_ReturnsZero() throws Exception {
                // // Given
                // given(attractionService.countActiveAttractions()).willReturn(0L);

                // // When
                // ResultActions result = mockMvc.perform(get(BASE_URL + "/count")
                // .with(csrf()));

                // // Then
                // result.andDo(print())
                // .andExpect(status().isOk())
                // .andExpect(jsonPath("$.data", is(0)));
                // }
                // }

                // @Nested
                // @DisplayName("Security Tests")
                // class SecurityTests {

                // @Test
                // @DisplayName("Public endpoints should be accessible without authentication")
                // void publicEndpoints_ShouldBeAccessibleWithoutAuth() throws Exception {
                // // Given
                // PagedResponse<AttractionResponse> pagedResponse =
                // PagedResponse.<AttractionResponse>builder()
                // .content(Collections.emptyList())
                // .page(0)
                // .size(10)
                // .totalElements(0)
                // .totalPages(0)
                // .first(true)
                // .last(true)
                // .empty(true)
                // .build();

                // given(attractionService.getAllActiveAttractions(any(), anyInt(), anyInt(),
                // anyString(), anyString()))
                // .willReturn(pagedResponse);
                // given(attractionService.countActiveAttractions()).willReturn(0L);

                // // When & Then - All public endpoints should be accessible
                // mockMvc.perform(get(BASE_URL).with(csrf()))
                // .andExpect(status().isOk());

                // mockMvc.perform(get(BASE_URL + "/count").with(csrf()))
                // .andExpect(status().isOk());
                // }
        }
}
