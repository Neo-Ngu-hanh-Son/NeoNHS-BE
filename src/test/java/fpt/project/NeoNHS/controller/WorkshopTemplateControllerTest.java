package fpt.project.NeoNHS.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fpt.project.NeoNHS.dto.request.workshop.CreateWorkshopTemplateRequest;
import fpt.project.NeoNHS.dto.request.workshop.UpdateWorkshopTemplateRequest;
import fpt.project.NeoNHS.dto.response.workshop.WorkshopTemplateResponse;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.exception.UnauthorizedException;
import fpt.project.NeoNHS.security.CustomUserDetailsService;
import fpt.project.NeoNHS.security.JwtTokenProvider;
import fpt.project.NeoNHS.service.WorkshopTemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WorkshopTemplateController.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkshopTemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkshopTemplateService workshopTemplateService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UUID templateId;
    private WorkshopTemplateResponse mockResponse;
    private Principal mockPrincipal;
    private final String vendorEmail = "vendor@example.com";

    @BeforeEach
    void setUp() {
        templateId = UUID.randomUUID();
        mockResponse = new WorkshopTemplateResponse();
        mockPrincipal = () -> vendorEmail;
    }

    // ==================== Helper to build a valid request ====================

    private CreateWorkshopTemplateRequest buildValidRequest() {
        return CreateWorkshopTemplateRequest.builder()
                .name("Yoga Workshop")
                .shortDescription("A relaxing yoga session")
                .fullDescription("Full day yoga workshop for beginners")
                .estimatedDuration(120)
                .defaultPrice(new BigDecimal("99.99"))
                .minParticipants(5)
                .maxParticipants(20)
                .imageUrls(List.of("https://example.com/img1.jpg", "https://example.com/img2.jpg"))
                .thumbnailIndex(0)
                .tagIds(List.of(UUID.randomUUID(), UUID.randomUUID()))
                .build();
    }

    private UpdateWorkshopTemplateRequest buildValidUpdateRequest() {
        UpdateWorkshopTemplateRequest request = new UpdateWorkshopTemplateRequest();
        request.setName("Updated Yoga Workshop");
        request.setShortDescription("An updated relaxing yoga session");
        request.setFullDescription("Updated full day yoga workshop for beginners");
        request.setEstimatedDuration(150);
        request.setDefaultPrice(new BigDecimal("149.99"));
        request.setMinParticipants(5);
        request.setMaxParticipants(25);
        request.setImageUrls(List.of("https://example.com/img1_updated.jpg", "https://example.com/img2_updated.jpg"));
        request.setThumbnailIndex(1);
        request.setTagIds(List.of(UUID.randomUUID(), UUID.randomUUID()));
        return request;
    }

    // ==================================================================================
    //  CREATE WORKSHOP TEMPLATE — Test cases UTCID01 through UTCID10
    // ==================================================================================

    @Nested
    @DisplayName("Create Workshop Template Tests")
    class CreateWorkshopTemplateTests {

        // ----- UTCID01: Normal — success (201 Created) -----
        @Test
        @DisplayName("UTCID01 - Valid request from verified vendor → 201 Created")
        @WithMockUser(roles = "VENDOR")
        void utcid01_validRequest_shouldReturn201() throws Exception {
            CreateWorkshopTemplateRequest request = buildValidRequest();

            Mockito.when(workshopTemplateService.createWorkshopTemplate(
                    eq(vendorEmail), any(CreateWorkshopTemplateRequest.class)))
                    .thenReturn(mockResponse);

            mockMvc.perform(post("/api/workshops/templates")
                            .principal(mockPrincipal)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message").value("Workshop template created successfully"));
        }

        // ----- UTCID02: Abnormal — DB connection lost (500 Internal Server Error) -----
        @Test
        @DisplayName("UTCID02 - DB connection lost → 500 Internal Server Error")
        @WithMockUser(roles = "VENDOR")
        void utcid02_dbConnectionLost_shouldReturn500() throws Exception {
            CreateWorkshopTemplateRequest request = buildValidRequest();

            Mockito.when(workshopTemplateService.createWorkshopTemplate(
                    eq(vendorEmail), any(CreateWorkshopTemplateRequest.class)))
                    .thenThrow(new DataAccessResourceFailureException("Database connection error"));

            mockMvc.perform(post("/api/workshops/templates")
                            .principal(mockPrincipal)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false));
        }

        // ----- UTCID03: Abnormal — No vendor profile in DB (404 Not Found) -----
        @Test
        @DisplayName("UTCID03 - No vendor profile in DB → 404 Not Found")
        @WithMockUser(roles = "VENDOR")
        void utcid03_noVendorProfile_shouldReturn404() throws Exception {
            CreateWorkshopTemplateRequest request = buildValidRequest();

            Mockito.when(workshopTemplateService.createWorkshopTemplate(
                    eq(vendorEmail), any(CreateWorkshopTemplateRequest.class)))
                    .thenThrow(new ResourceNotFoundException("VendorProfile", "email", vendorEmail));

            mockMvc.perform(post("/api/workshops/templates")
                            .principal(mockPrincipal)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(
                            "VendorProfile not found with email: '" + vendorEmail + "'"));
        }

        // ----- UTCID04: Abnormal — Vendor not verified (400 Bad Request) -----
        @Test
        @DisplayName("UTCID04 - Vendor not verified → 400 Bad Request")
        @WithMockUser(roles = "VENDOR")
        void utcid04_vendorNotVerified_shouldReturn400() throws Exception {
            CreateWorkshopTemplateRequest request = buildValidRequest();

            Mockito.when(workshopTemplateService.createWorkshopTemplate(
                    eq(vendorEmail), any(CreateWorkshopTemplateRequest.class)))
                    .thenThrow(new BadRequestException(
                            "Only verified vendors can create workshop templates"));

            mockMvc.perform(post("/api/workshops/templates")
                            .principal(mockPrincipal)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(
                            "Only verified vendors can create workshop templates"));
        }

        // ----- UTCID05: Abnormal — Name is blank (400 Bad Request) -----
        @Test
        @DisplayName("UTCID05 - Name is blank → 400 Bad Request")
        @WithMockUser(roles = "VENDOR")
        void utcid05_nameIsBlank_shouldReturn400() throws Exception {
            CreateWorkshopTemplateRequest request = buildValidRequest();
            request.setName("");

            Mockito.when(workshopTemplateService.createWorkshopTemplate(
                    eq(vendorEmail), any(CreateWorkshopTemplateRequest.class)))
                    .thenThrow(new BadRequestException("Workshop name is required"));

            mockMvc.perform(post("/api/workshops/templates")
                            .principal(mockPrincipal)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Workshop name is required"));
        }

        // ----- UTCID06: Abnormal — One or more tag IDs not in DB (400 Bad Request) -----
        @Test
        @DisplayName("UTCID06 - Invalid tag IDs → 400 Bad Request")
        @WithMockUser(roles = "VENDOR")
        void utcid06_invalidTagIds_shouldReturn400() throws Exception {
            CreateWorkshopTemplateRequest request = buildValidRequest();

            Mockito.when(workshopTemplateService.createWorkshopTemplate(
                    eq(vendorEmail), any(CreateWorkshopTemplateRequest.class)))
                    .thenThrow(new BadRequestException("One or more tag IDs are invalid"));

            mockMvc.perform(post("/api/workshops/templates")
                            .principal(mockPrincipal)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(
                            "One or more tag IDs are invalid"));
        }

        // ----- UTCID07: Abnormal — minParticipants > maxParticipants (400 Bad Request) -----
        @Test
        @DisplayName("UTCID07 - minParticipants > maxParticipants → 400 Bad Request")
        @WithMockUser(roles = "VENDOR")
        void utcid07_minGreaterThanMax_shouldReturn400() throws Exception {
            CreateWorkshopTemplateRequest request = buildValidRequest();
            request.setMinParticipants(30);
            request.setMaxParticipants(10);

            Mockito.when(workshopTemplateService.createWorkshopTemplate(
                    eq(vendorEmail), any(CreateWorkshopTemplateRequest.class)))
                    .thenThrow(new BadRequestException(
                            "Minimum participants cannot be greater than maximum participants"));

            mockMvc.perform(post("/api/workshops/templates")
                            .principal(mockPrincipal)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(
                            "Minimum participants cannot be greater than maximum participants"));
        }

        // ----- UTCID08: Abnormal — thumbnailIndex >= imageUrls.size (400 Bad Request) -----
        @Test
        @DisplayName("UTCID08 - thumbnailIndex out of bounds → 400 Bad Request")
        @WithMockUser(roles = "VENDOR")
        void utcid08_thumbnailIndexOutOfBounds_shouldReturn400() throws Exception {
            CreateWorkshopTemplateRequest request = buildValidRequest();
            request.setImageUrls(List.of("https://example.com/img1.jpg"));
            request.setThumbnailIndex(5);

            Mockito.when(workshopTemplateService.createWorkshopTemplate(
                    eq(vendorEmail), any(CreateWorkshopTemplateRequest.class)))
                    .thenThrow(new BadRequestException("Thumbnail index is out of bounds"));

            mockMvc.perform(post("/api/workshops/templates")
                            .principal(mockPrincipal)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(
                            "Thumbnail index is out of bounds"));
        }

        // ----- UTCID09: Boundary — minParticipants == maxParticipants (201 Created) -----
        @Test
        @DisplayName("UTCID09 - minParticipants equals maxParticipants → 201 Created")
        @WithMockUser(roles = "VENDOR")
        void utcid09_minEqualsMax_shouldReturn201() throws Exception {
            CreateWorkshopTemplateRequest request = buildValidRequest();
            request.setMinParticipants(10);
            request.setMaxParticipants(10);

            Mockito.when(workshopTemplateService.createWorkshopTemplate(
                    eq(vendorEmail), any(CreateWorkshopTemplateRequest.class)))
                    .thenReturn(mockResponse);

            mockMvc.perform(post("/api/workshops/templates")
                            .principal(mockPrincipal)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message").value("Workshop template created successfully"));
        }

        // ----- UTCID10: Boundary — tagIds has exactly 1 item (201 Created) -----
        @Test
        @DisplayName("UTCID10 - tagIds with exactly 1 item (minimum) → 201 Created")
        @WithMockUser(roles = "VENDOR")
        void utcid10_singleTagId_shouldReturn201() throws Exception {
            CreateWorkshopTemplateRequest request = buildValidRequest();
            request.setTagIds(List.of(UUID.randomUUID()));

            Mockito.when(workshopTemplateService.createWorkshopTemplate(
                    eq(vendorEmail), any(CreateWorkshopTemplateRequest.class)))
                    .thenReturn(mockResponse);

            mockMvc.perform(post("/api/workshops/templates")
                            .principal(mockPrincipal)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message").value("Workshop template created successfully"));
        }
    }

    // ==================================================================================
    //  Existing API tests (GET, PUT, DELETE)
    // ==================================================================================

    @Nested
    @DisplayName("Get Workshop Template By ID Tests")
    class GetWorkshopTemplateByIdTests {

        // ----- UTCID01: Normal — exists (200 OK) -----
        @Test
        @DisplayName("UTCID01 - Valid template ID → 200 OK")
        void utcid01_validId_shouldReturn200() throws Exception {
            Mockito.when(workshopTemplateService.getWorkshopTemplateById(templateId))
                   .thenReturn(mockResponse);

            mockMvc.perform(get("/api/workshops/templates/{id}", templateId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Workshop template retrieved successfully"));
        }

        // ----- UTCID02: Abnormal — ID not found (404 Not Found) -----
        @Test
        @DisplayName("UTCID02 - Template ID not found in DB → 404 Not Found")
        void utcid02_idNotFound_shouldReturn404() throws Exception {
            Mockito.when(workshopTemplateService.getWorkshopTemplateById(templateId))
                   .thenThrow(new ResourceNotFoundException("WorkshopTemplate", "id", templateId.toString()));

            mockMvc.perform(get("/api/workshops/templates/{id}", templateId))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(
                            "WorkshopTemplate not found with id: '" + templateId + "'"));
        }

        // ----- UTCID03: Abnormal — DB connection lost (500 Internal Server Error) -----
        @Test
        @DisplayName("UTCID03 - DB connection lost → 500 Internal Server Error")
        void utcid03_dbConnectionLost_shouldReturn500() throws Exception {
            Mockito.when(workshopTemplateService.getWorkshopTemplateById(templateId))
                   .thenThrow(new DataAccessResourceFailureException("Database connection error"));

            mockMvc.perform(get("/api/workshops/templates/{id}", templateId))
                    .andDo(print())
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false));
        }

        // ----- UTCID04: Abnormal — Invalid UUID format (400 Bad Request) -----
        @Test
        @DisplayName("UTCID04 - Invalid UUID format → 400 Bad Request")
        void utcid04_invalidUuidFormat_shouldReturn400() throws Exception {
            mockMvc.perform(get("/api/workshops/templates/{id}", "invalid-uuid-format"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Get My Workshop Templates Tests")
    class GetMyWorkshopTemplatesTests {

        // ----- UTCID01: Normal — successful retrieval with elements (200 OK) -----
        @Test
        @DisplayName("UTCID01 - Valid request, returns templates → 200 OK")
        @WithMockUser(roles = "VENDOR")
        void utcid01_validRequest_shouldReturn200() throws Exception {
            Page<WorkshopTemplateResponse> mockPage = new PageImpl<>(List.of(mockResponse));

            Mockito.when(workshopTemplateService.getMyWorkshopTemplates(eq(vendorEmail), any(Pageable.class)))
                   .thenReturn(mockPage);

            mockMvc.perform(get("/api/workshops/templates/my")
                    .principal(mockPrincipal)
                    .param("page", "0")
                    .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Your workshop templates retrieved successfully"));
        }

        // ----- UTCID02: Normal — successful retrieval, empty list (200 OK) -----
        @Test
        @DisplayName("UTCID02 - Valid request, no templates found → 200 OK")
        @WithMockUser(roles = "VENDOR")
        void utcid02_emptyList_shouldReturn200() throws Exception {
            Page<WorkshopTemplateResponse> mockPage = new PageImpl<>(List.of());

            Mockito.when(workshopTemplateService.getMyWorkshopTemplates(eq(vendorEmail), any(Pageable.class)))
                   .thenReturn(mockPage);

            mockMvc.perform(get("/api/workshops/templates/my")
                    .principal(mockPrincipal)
                    .param("page", "0")
                    .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Your workshop templates retrieved successfully"));
        }

        // ----- UTCID03: Abnormal — Vendor profile not found (404 Not Found) -----
        @Test
        @DisplayName("UTCID03 - Vendor profile not found in DB → 404 Not Found")
        @WithMockUser(roles = "VENDOR")
        void utcid03_vendorNotFound_shouldReturn404() throws Exception {
            Mockito.when(workshopTemplateService.getMyWorkshopTemplates(eq(vendorEmail), any(Pageable.class)))
                   .thenThrow(new ResourceNotFoundException("VendorProfile", "email", vendorEmail));

            mockMvc.perform(get("/api/workshops/templates/my")
                    .principal(mockPrincipal)
                    .param("page", "0")
                    .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("VendorProfile not found with email: '" + vendorEmail + "'"));
        }

        // ----- UTCID04: Abnormal — DB connection lost (500 Internal Server Error) -----
        @Test
        @DisplayName("UTCID04 - DB connection lost → 500 Internal Server Error")
        @WithMockUser(roles = "VENDOR")
        void utcid04_dbConnectionLost_shouldReturn500() throws Exception {
            Mockito.when(workshopTemplateService.getMyWorkshopTemplates(eq(vendorEmail), any(Pageable.class)))
                   .thenThrow(new DataAccessResourceFailureException("Database connection error"));

            mockMvc.perform(get("/api/workshops/templates/my")
                    .principal(mockPrincipal)
                    .param("page", "0")
                    .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false));
        }

        // ----- UTCID05: Abnormal — Invalid pagination parameters (400 Bad Request) -----
        @Test
        @DisplayName("UTCID05 - Invalid page size format → 400 Bad Request")
        @WithMockUser(roles = "VENDOR")
        void utcid05_invalidPageSize_shouldReturn400() throws Exception {
            mockMvc.perform(get("/api/workshops/templates/my")
                    .principal(mockPrincipal)
                    .param("page", "0")
                    .param("size", "invalid-size"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void filterWorkshopTemplates_ShouldReturn200() throws Exception {
        List<WorkshopTemplateResponse> mockList = List.of(mockResponse);

        Mockito.when(workshopTemplateService.searchWorkshopTemplates(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
        )).thenReturn(mockList);

        mockMvc.perform(get("/api/workshops/templates/filter")
                .param("keyword", "yoga")
                .param("status", "ACTIVE")
                .param("minPrice", "50.00")
                .param("maxPrice", "100.00"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Workshop templates filtered successfully"));
    }

    @Nested
    @DisplayName("Update Workshop Template Tests")
    class UpdateWorkshopTemplateTests {

        // ----- UTCID01: Normal — success (200 OK) -----
        @Test
        @DisplayName("UTCID01 - Valid update request from verified vendor → 200 OK")
        @WithMockUser(roles = "VENDOR")
        void utcid01_validRequest_shouldReturn200() throws Exception {
            UpdateWorkshopTemplateRequest request = buildValidUpdateRequest();

            Mockito.when(workshopTemplateService.updateWorkshopTemplate(
                    eq(vendorEmail), eq(templateId), any(UpdateWorkshopTemplateRequest.class)))
                    .thenReturn(mockResponse);

            mockMvc.perform(put("/api/workshops/templates/{id}", templateId)
                            .principal(mockPrincipal)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Workshop template updated successfully"));
        }

        // ----- UTCID02: Abnormal — Template not found (404 Not Found) -----
        @Test
        @DisplayName("UTCID02 - Template ID not found → 404 Not Found")
        @WithMockUser(roles = "VENDOR")
        void utcid02_templateNotFound_shouldReturn404() throws Exception {
            UpdateWorkshopTemplateRequest request = buildValidUpdateRequest();

            Mockito.when(workshopTemplateService.updateWorkshopTemplate(
                    eq(vendorEmail), eq(templateId), any(UpdateWorkshopTemplateRequest.class)))
                    .thenThrow(new ResourceNotFoundException("WorkshopTemplate", "id", templateId.toString()));

            mockMvc.perform(put("/api/workshops/templates/{id}", templateId)
                            .principal(mockPrincipal)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("WorkshopTemplate not found with id: '" + templateId + "'"));
        }

        // ----- UTCID03: Abnormal — No vendor profile in DB (404 Not Found) -----
        @Test
        @DisplayName("UTCID03 - No vendor profile in DB → 404 Not Found")
        @WithMockUser(roles = "VENDOR")
        void utcid03_noVendorProfile_shouldReturn404() throws Exception {
            UpdateWorkshopTemplateRequest request = buildValidUpdateRequest();

            Mockito.when(workshopTemplateService.updateWorkshopTemplate(
                    eq(vendorEmail), eq(templateId), any(UpdateWorkshopTemplateRequest.class)))
                    .thenThrow(new ResourceNotFoundException("VendorProfile", "email", vendorEmail));

            mockMvc.perform(put("/api/workshops/templates/{id}", templateId)
                            .principal(mockPrincipal)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("VendorProfile not found with email: '" + vendorEmail + "'"));
        }

        // ----- UTCID04: Abnormal — DB connection lost (500 Internal Server Error) -----
        @Test
        @DisplayName("UTCID04 - DB connection lost → 500 Internal Server Error")
        @WithMockUser(roles = "VENDOR")
        void utcid04_dbConnectionLost_shouldReturn500() throws Exception {
            UpdateWorkshopTemplateRequest request = buildValidUpdateRequest();

            Mockito.when(workshopTemplateService.updateWorkshopTemplate(
                    eq(vendorEmail), eq(templateId), any(UpdateWorkshopTemplateRequest.class)))
                    .thenThrow(new DataAccessResourceFailureException("Database connection error"));

            mockMvc.perform(put("/api/workshops/templates/{id}", templateId)
                            .principal(mockPrincipal)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false));
        }

        // ----- UTCID05: Abnormal — Name is blank (400 Bad Request) -----
        @Test
        @DisplayName("UTCID05 - Name is blank → 400 Bad Request")
        @WithMockUser(roles = "VENDOR")
        void utcid05_nameIsBlank_shouldReturn400() throws Exception {
            UpdateWorkshopTemplateRequest request = buildValidUpdateRequest();
            request.setName("");

            Mockito.when(workshopTemplateService.updateWorkshopTemplate(
                    eq(vendorEmail), eq(templateId), any(UpdateWorkshopTemplateRequest.class)))
                    .thenThrow(new BadRequestException("Workshop name is required"));

            mockMvc.perform(put("/api/workshops/templates/{id}", templateId)
                            .principal(mockPrincipal)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Workshop name is required"));
        }

        // ----- UTCID06: Abnormal — One or more tag IDs not in DB (400 Bad Request) -----
        @Test
        @DisplayName("UTCID06 - Invalid tag IDs → 400 Bad Request")
        @WithMockUser(roles = "VENDOR")
        void utcid06_invalidTagIds_shouldReturn400() throws Exception {
            UpdateWorkshopTemplateRequest request = buildValidUpdateRequest();

            Mockito.when(workshopTemplateService.updateWorkshopTemplate(
                    eq(vendorEmail), eq(templateId), any(UpdateWorkshopTemplateRequest.class)))
                    .thenThrow(new BadRequestException("One or more tag IDs are invalid"));

            mockMvc.perform(put("/api/workshops/templates/{id}", templateId)
                            .principal(mockPrincipal)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("One or more tag IDs are invalid"));
        }

        // ----- UTCID07: Abnormal — minParticipants > maxParticipants (400 Bad Request) -----
        @Test
        @DisplayName("UTCID07 - minParticipants > maxParticipants → 400 Bad Request")
        @WithMockUser(roles = "VENDOR")
        void utcid07_minGreaterThanMax_shouldReturn400() throws Exception {
            UpdateWorkshopTemplateRequest request = buildValidUpdateRequest();
            request.setMinParticipants(30);
            request.setMaxParticipants(10);

            Mockito.when(workshopTemplateService.updateWorkshopTemplate(
                    eq(vendorEmail), eq(templateId), any(UpdateWorkshopTemplateRequest.class)))
                    .thenThrow(new BadRequestException("Minimum participants cannot be greater than maximum participants"));

            mockMvc.perform(put("/api/workshops/templates/{id}", templateId)
                            .principal(mockPrincipal)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Minimum participants cannot be greater than maximum participants"));
        }

        // ----- UTCID08: Abnormal — thumbnailIndex >= imageUrls.size (400 Bad Request) -----
        @Test
        @DisplayName("UTCID08 - thumbnailIndex out of bounds → 400 Bad Request")
        @WithMockUser(roles = "VENDOR")
        void utcid08_thumbnailIndexOutOfBounds_shouldReturn400() throws Exception {
            UpdateWorkshopTemplateRequest request = buildValidUpdateRequest();
            request.setImageUrls(List.of("https://example.com/img1.jpg"));
            request.setThumbnailIndex(5);

            Mockito.when(workshopTemplateService.updateWorkshopTemplate(
                    eq(vendorEmail), eq(templateId), any(UpdateWorkshopTemplateRequest.class)))
                    .thenThrow(new BadRequestException("Thumbnail index is out of bounds"));

            mockMvc.perform(put("/api/workshops/templates/{id}", templateId)
                            .principal(mockPrincipal)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Thumbnail index is out of bounds"));
        }

        // ----- UTCID09: Boundary — minParticipants == maxParticipants (200 OK) -----
        @Test
        @DisplayName("UTCID09 - minParticipants equals maxParticipants → 200 OK")
        @WithMockUser(roles = "VENDOR")
        void utcid09_minEqualsMax_shouldReturn200() throws Exception {
            UpdateWorkshopTemplateRequest request = buildValidUpdateRequest();
            request.setMinParticipants(15);
            request.setMaxParticipants(15);

            Mockito.when(workshopTemplateService.updateWorkshopTemplate(
                    eq(vendorEmail), eq(templateId), any(UpdateWorkshopTemplateRequest.class)))
                    .thenReturn(mockResponse);

            mockMvc.perform(put("/api/workshops/templates/{id}", templateId)
                            .principal(mockPrincipal)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Workshop template updated successfully"));
        }

        // ----- UTCID10: Boundary — tagIds has exactly 1 item (200 OK) -----
        @Test
        @DisplayName("UTCID10 - tagIds with exactly 1 item (minimum) → 200 OK")
        @WithMockUser(roles = "VENDOR")
        void utcid10_singleTagId_shouldReturn200() throws Exception {
            UpdateWorkshopTemplateRequest request = buildValidUpdateRequest();
            request.setTagIds(List.of(UUID.randomUUID()));

            Mockito.when(workshopTemplateService.updateWorkshopTemplate(
                    eq(vendorEmail), eq(templateId), any(UpdateWorkshopTemplateRequest.class)))
                    .thenReturn(mockResponse);

            mockMvc.perform(put("/api/workshops/templates/{id}", templateId)
                            .principal(mockPrincipal)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Workshop template updated successfully"));
        }

        // ----- UTCID11: Abnormal — Invalid UUID format (400 Bad Request) -----
        @Test
        @DisplayName("UTCID11 - Invalid UUID format in path → 400 Bad Request")
        @WithMockUser(roles = "VENDOR")
        void utcid11_invalidUuidFormat_shouldReturn400() throws Exception {
            UpdateWorkshopTemplateRequest request = buildValidUpdateRequest();

            mockMvc.perform(put("/api/workshops/templates/{id}", "invalid-uuid-format")
                            .principal(mockPrincipal)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        // ----- UTCID12: Abnormal — Unauthorized / Does not own template (401 Unauthorized) -----
        @Test
        @DisplayName("UTCID12 - Vendor does not own template → 401 Unauthorized")
        @WithMockUser(roles = "VENDOR")
        void utcid12_notOwner_shouldReturn401() throws Exception {
            UpdateWorkshopTemplateRequest request = buildValidUpdateRequest();

            Mockito.when(workshopTemplateService.updateWorkshopTemplate(
                    eq(vendorEmail), eq(templateId), any(UpdateWorkshopTemplateRequest.class)))
                    .thenThrow(new UnauthorizedException("You do not have permission to modify this template"));

            mockMvc.perform(put("/api/workshops/templates/{id}", templateId)
                            .principal(mockPrincipal)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("You do not have permission to modify this template"));
        }

        // ----- UTCID13: Abnormal — Vendor not verified (400 Bad Request) -----
        @Test
        @DisplayName("UTCID13 - Vendor not verified → 400 Bad Request")
        @WithMockUser(roles = "VENDOR")
        void utcid13_vendorNotVerified_shouldReturn400() throws Exception {
            UpdateWorkshopTemplateRequest request = buildValidUpdateRequest();

            Mockito.when(workshopTemplateService.updateWorkshopTemplate(
                    eq(vendorEmail), eq(templateId), any(UpdateWorkshopTemplateRequest.class)))
                    .thenThrow(new BadRequestException("Only verified vendors can update workshop templates"));

            mockMvc.perform(put("/api/workshops/templates/{id}", templateId)
                            .principal(mockPrincipal)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Only verified vendors can update workshop templates"));
        }

        // ----- UTCID14: Abnormal — Malformed JSON (400 Bad Request) -----
        @Test
        @DisplayName("UTCID14 - Malformed JSON payload → 400 Bad Request")
        @WithMockUser(roles = "VENDOR")
        void utcid14_malformedJson_shouldReturn400() throws Exception {
            mockMvc.perform(put("/api/workshops/templates/{id}", templateId)
                            .principal(mockPrincipal)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{ invalid_json: "))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
            // GlobalExceptionHandler handles HttpMessageNotReadableException
        }

        // ----- UTCID15: Abnormal — Invalid state to update (400 Bad Request) -----
        @Test
        @DisplayName("UTCID15 - Invalid state (e.g. already approved) → 400 Bad Request")
        @WithMockUser(roles = "VENDOR")
        void utcid15_invalidState_shouldReturn400() throws Exception {
            UpdateWorkshopTemplateRequest request = buildValidUpdateRequest();

            Mockito.when(workshopTemplateService.updateWorkshopTemplate(
                    eq(vendorEmail), eq(templateId), any(UpdateWorkshopTemplateRequest.class)))
                    .thenThrow(new IllegalArgumentException("Template cannot be updated because it is already approved"));

            mockMvc.perform(put("/api/workshops/templates/{id}", templateId)
                            .principal(mockPrincipal)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Template cannot be updated because it is already approved"));
        }
    }

    @Nested
    @DisplayName("POST /api/workshops/templates/{id}/toggle-publish")
    class TogglePublishWorkshopTemplateTests {

        @Test
        @DisplayName("UTCID01 - Valid toggle publish - Should return 200")
        @WithMockUser(roles = "VENDOR")
        void utcid01_validToggle_shouldReturn200() throws Exception {
            Mockito.when(workshopTemplateService.togglePublishWorkshopTemplate(vendorEmail, templateId)).thenReturn(mockResponse);

            mockMvc.perform(post("/api/workshops/templates/{id}/toggle-publish", templateId)
                            .principal(mockPrincipal))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Workshop template publish status toggled successfully"));
        }

        @Test
        @DisplayName("UTCID02 - User not authenticated - Should return 401")
        void utcid02_unauthenticated_shouldReturn401() throws Exception {
            mockMvc.perform(post("/api/workshops/templates/{id}/toggle-publish", templateId))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("UTCID03 - Template not found - Should return 404")
        @WithMockUser(roles = "VENDOR")
        void utcid03_templateNotFound_shouldReturn404() throws Exception {
            Mockito.when(workshopTemplateService.togglePublishWorkshopTemplate(vendorEmail, templateId))
                    .thenThrow(new ResourceNotFoundException("Workshop template", "id", templateId));

            mockMvc.perform(post("/api/workshops/templates/{id}/toggle-publish", templateId)
                            .principal(mockPrincipal))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Workshop template not found with id : '" + templateId + "'"));
        }

        @Test
        @DisplayName("UTCID04 - Unauthorized vendor - Should return 403")
        @WithMockUser(roles = "VENDOR")
        void utcid04_unauthorizedVendor_shouldReturn403() throws Exception {
            Mockito.when(workshopTemplateService.togglePublishWorkshopTemplate(vendorEmail, templateId))
                    .thenThrow(new UnauthorizedException("You do not have permission to access this template"));

            mockMvc.perform(post("/api/workshops/templates/{id}/toggle-publish", templateId)
                            .principal(mockPrincipal))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("You do not have permission to access this template"));
        }

        @Test
        @DisplayName("UTCID05 - Template not approved - Should return 400")
        @WithMockUser(roles = "VENDOR")
        void utcid05_templateNotApproved_shouldReturn400() throws Exception {
            Mockito.when(workshopTemplateService.togglePublishWorkshopTemplate(vendorEmail, templateId))
                    .thenThrow(new IllegalArgumentException("Template must be APPROVED before it can be published"));

            mockMvc.perform(post("/api/workshops/templates/{id}/toggle-publish", templateId)
                            .principal(mockPrincipal))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Template must be APPROVED before it can be published"));
        }
    }

    @Nested
    @DisplayName("POST /api/workshops/templates/{id}/register")
    class RegisterWorkshopTemplateTests {

        @Test
        @DisplayName("UTCID01 - Valid register - Should return 200")
        @WithMockUser(roles = "VENDOR")
        void utcid01_validRegister_shouldReturn200() throws Exception {
            Mockito.when(workshopTemplateService.registerWorkshopTemplate(vendorEmail, templateId)).thenReturn(mockResponse);

            mockMvc.perform(post("/api/workshops/templates/{id}/register", templateId)
                            .principal(mockPrincipal))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Template submitted successfully. Please wait for admin approval."));
        }

        @Test
        @DisplayName("UTCID02 - User not authenticated - Should return 401")
        void utcid02_unauthenticated_shouldReturn401() throws Exception {
            mockMvc.perform(post("/api/workshops/templates/{id}/register", templateId))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("UTCID03 - Template not found - Should return 404")
        @WithMockUser(roles = "VENDOR")
        void utcid03_templateNotFound_shouldReturn404() throws Exception {
            Mockito.when(workshopTemplateService.registerWorkshopTemplate(vendorEmail, templateId))
                    .thenThrow(new ResourceNotFoundException("Workshop template", "id", templateId));

            mockMvc.perform(post("/api/workshops/templates/{id}/register", templateId)
                            .principal(mockPrincipal))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Workshop template not found with id : '" + templateId + "'"));
        }

        @Test
        @DisplayName("UTCID04 - Unauthorized vendor - Should return 403")
        @WithMockUser(roles = "VENDOR")
        void utcid04_unauthorizedVendor_shouldReturn403() throws Exception {
            Mockito.when(workshopTemplateService.registerWorkshopTemplate(vendorEmail, templateId))
                    .thenThrow(new UnauthorizedException("You do not have permission to access this template"));

            mockMvc.perform(post("/api/workshops/templates/{id}/register", templateId)
                            .principal(mockPrincipal))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("You do not have permission to access this template"));
        }

        @Test
        @DisplayName("UTCID05 - Template already approved - Should return 400")
        @WithMockUser(roles = "VENDOR")
        void utcid05_templateAlreadyApproved_shouldReturn400() throws Exception {
            Mockito.when(workshopTemplateService.registerWorkshopTemplate(vendorEmail, templateId))
                    .thenThrow(new IllegalArgumentException("Template cannot be registered because it is already approved"));

            mockMvc.perform(post("/api/workshops/templates/{id}/register", templateId)
                            .principal(mockPrincipal))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Template cannot be registered because it is already approved"));
        }
    }

    @Test
    @WithMockUser(roles = "VENDOR")
    void deleteWorkshopTemplate_ShouldReturn200() throws Exception {
        Mockito.doNothing().when(workshopTemplateService).deleteWorkshopTemplate(vendorEmail, templateId);

        mockMvc.perform(delete("/api/workshops/templates/{id}", templateId)
                .principal(mockPrincipal))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Workshop template deleted successfully"));

        Mockito.verify(workshopTemplateService, Mockito.times(1)).deleteWorkshopTemplate(vendorEmail, templateId);
    }
}
