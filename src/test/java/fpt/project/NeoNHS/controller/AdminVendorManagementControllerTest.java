package fpt.project.NeoNHS.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fpt.project.NeoNHS.dto.request.workshop.ApproveWorkshopTemplateRequest;
import fpt.project.NeoNHS.dto.request.workshop.RejectWorkshopTemplateRequest;
import fpt.project.NeoNHS.dto.response.workshop.WorkshopTemplateResponse;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.exception.UnauthorizedException;
import fpt.project.NeoNHS.security.CustomUserDetailsService;
import fpt.project.NeoNHS.security.JwtTokenProvider;
import fpt.project.NeoNHS.service.AdminVendorManagementService;
import fpt.project.NeoNHS.service.WorkshopTemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminVendorManagementController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminVendorManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminVendorManagementService adminVendorManagementService;

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
    private final String adminEmail = "admin@example.com";

    @BeforeEach
    void setUp() {
        templateId = UUID.randomUUID();
        mockResponse = new WorkshopTemplateResponse();
        mockPrincipal = () -> adminEmail;
    }

    @Nested
    @DisplayName("POST /api/admin/vendors/workshop-templates/{id}/approve")
    class ApproveWorkshopTemplateTests {

        @Test
        @DisplayName("UTCID01 - Valid approve - Should return 200")
        @WithMockUser(roles = "ADMIN")
        void utcid01_validApprove_shouldReturn200() throws Exception {
            ApproveWorkshopTemplateRequest request = new ApproveWorkshopTemplateRequest();
            request.setAdminNote("Looks good");

            Mockito.when(workshopTemplateService.approveWorkshopTemplate(eq(adminEmail), eq(templateId), eq("Looks good")))
                    .thenReturn(mockResponse);

            mockMvc.perform(post("/api/admin/vendors/workshop-templates/{id}/approve", templateId)
                            .principal(mockPrincipal)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Workshop template approved successfully"));
        }

        @Test
        @DisplayName("UTCID02 - User not authenticated (missing principal) - Should return 401")
        void utcid02_unauthenticated_shouldReturn401() throws Exception {
            ApproveWorkshopTemplateRequest request = new ApproveWorkshopTemplateRequest();

            mockMvc.perform(post("/api/admin/vendors/workshop-templates/{id}/approve", templateId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("UTCID03 - Template not found - Should return 404")
        @WithMockUser(roles = "ADMIN")
        void utcid03_templateNotFound_shouldReturn404() throws Exception {
            ApproveWorkshopTemplateRequest request = new ApproveWorkshopTemplateRequest();

            Mockito.when(workshopTemplateService.approveWorkshopTemplate(eq(adminEmail), eq(templateId), any()))
                    .thenThrow(new ResourceNotFoundException("Workshop template", "id", templateId));

            mockMvc.perform(post("/api/admin/vendors/workshop-templates/{id}/approve", templateId)
                            .principal(mockPrincipal)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Workshop template not found with id: '" + templateId + "'"));
        }

        @Test
        @DisplayName("UTCID04 - Unauthorized access (not an admin) - Should return 403")
        @WithMockUser(roles = "VENDOR")
        void utcid04_unauthorizedAccess_shouldReturn403() throws Exception {
            ApproveWorkshopTemplateRequest request = new ApproveWorkshopTemplateRequest();

            Mockito.when(workshopTemplateService.approveWorkshopTemplate(eq(adminEmail), eq(templateId), any()))
                    .thenThrow(new UnauthorizedException("You do not have permission to access this resource"));

            mockMvc.perform(post("/api/admin/vendors/workshop-templates/{id}/approve", templateId)
                            .principal(mockPrincipal)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("You do not have permission to access this resource"));
        }

        @Test
        @DisplayName("UTCID05 - Template not in PENDING status - Should return 400")
        @WithMockUser(roles = "ADMIN")
        void utcid05_templateNotPending_shouldReturn400() throws Exception {
            ApproveWorkshopTemplateRequest request = new ApproveWorkshopTemplateRequest();

            Mockito.when(workshopTemplateService.approveWorkshopTemplate(eq(adminEmail), eq(templateId), any()))
                    .thenThrow(new IllegalArgumentException("Only PENDING templates can be approved"));

            mockMvc.perform(post("/api/admin/vendors/workshop-templates/{id}/approve", templateId)
                            .principal(mockPrincipal)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Only PENDING templates can be approved"));
        }

        @Test
        @DisplayName("UTCID06 - Invalid UUID format - Should return 400")
        @WithMockUser(roles = "ADMIN")
        void utcid06_invalidUuidFormat_shouldReturn400() throws Exception {
            ApproveWorkshopTemplateRequest request = new ApproveWorkshopTemplateRequest();

            mockMvc.perform(post("/api/admin/vendors/workshop-templates/{id}/approve", "invalid-uuid")
                            .principal(mockPrincipal)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
            // GlobalExceptionHandler handles the MethodArgumentTypeMismatchException
        }

        @Test
        @DisplayName("UTCID07 - Missing body (valid since request is optional) - Should return 200")
        @WithMockUser(roles = "ADMIN")
        void utcid07_missingBody_shouldReturn200() throws Exception {
            Mockito.when(workshopTemplateService.approveWorkshopTemplate(eq(adminEmail), eq(templateId), eq(null)))
                    .thenReturn(mockResponse);

            mockMvc.perform(post("/api/admin/vendors/workshop-templates/{id}/approve", templateId)
                            .principal(mockPrincipal)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Workshop template approved successfully"));
        }
    }

    @Nested
    @DisplayName("POST /api/admin/vendors/workshop-templates/{id}/reject")
    class RejectWorkshopTemplateTests {

        @Test
        @DisplayName("UTCID01 - Valid reject - Should return 200")
        @WithMockUser(roles = "ADMIN")
        void utcid01_validReject_shouldReturn200() throws Exception {
            RejectWorkshopTemplateRequest request = new RejectWorkshopTemplateRequest();
            request.setAdminNote("Missing images");

            Mockito.when(workshopTemplateService.rejectWorkshopTemplate(eq(adminEmail), eq(templateId), eq("Missing images")))
                    .thenReturn(mockResponse);

            mockMvc.perform(post("/api/admin/vendors/workshop-templates/{id}/reject", templateId)
                            .principal(mockPrincipal)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Workshop template rejected"));
        }

        @Test
        @DisplayName("UTCID02 - User not authenticated (missing principal) - Should return 401")
        void utcid02_unauthenticated_shouldReturn401() throws Exception {
            RejectWorkshopTemplateRequest request = new RejectWorkshopTemplateRequest();
            request.setAdminNote("Missing images");

            mockMvc.perform(post("/api/admin/vendors/workshop-templates/{id}/reject", templateId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("UTCID03 - Missing reject reason (adminNote) - Should return 400")
        @WithMockUser(roles = "ADMIN")
        void utcid03_missingReason_shouldReturn400() throws Exception {
            Mockito.when(workshopTemplateService.rejectWorkshopTemplate(any(), any(), any()))
                    .thenThrow(new IllegalArgumentException("Reject reason is required"));

            mockMvc.perform(post("/api/admin/vendors/workshop-templates/{id}/reject", templateId)
                            .principal(mockPrincipal)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("UTCID04 - Blank reject reason - Should return 400")
        @WithMockUser(roles = "ADMIN")
        void utcid04_blankReason_shouldReturn400() throws Exception {
            RejectWorkshopTemplateRequest request = new RejectWorkshopTemplateRequest();
            request.setAdminNote("   "); // blank

            Mockito.when(workshopTemplateService.rejectWorkshopTemplate(any(), any(), any()))
                    .thenThrow(new IllegalArgumentException("Reject reason is required"));

            mockMvc.perform(post("/api/admin/vendors/workshop-templates/{id}/reject", templateId)
                            .principal(mockPrincipal)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("UTCID05 - Template not in PENDING status - Should return 400")
        @WithMockUser(roles = "ADMIN")
        void utcid05_templateNotPending_shouldReturn400() throws Exception {
            RejectWorkshopTemplateRequest request = new RejectWorkshopTemplateRequest();
            request.setAdminNote("Missing images");

            Mockito.when(workshopTemplateService.rejectWorkshopTemplate(eq(adminEmail), eq(templateId), eq("Missing images")))
                    .thenThrow(new IllegalArgumentException("Only PENDING templates can be rejected"));

            mockMvc.perform(post("/api/admin/vendors/workshop-templates/{id}/reject", templateId)
                            .principal(mockPrincipal)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Only PENDING templates can be rejected"));
        }

        @Test
        @DisplayName("UTCID06 - Template not found - Should return 404")
        @WithMockUser(roles = "ADMIN")
        void utcid06_templateNotFound_shouldReturn404() throws Exception {
            RejectWorkshopTemplateRequest request = new RejectWorkshopTemplateRequest();
            request.setAdminNote("Missing images");

            Mockito.when(workshopTemplateService.rejectWorkshopTemplate(eq(adminEmail), eq(templateId), eq("Missing images")))
                    .thenThrow(new ResourceNotFoundException("Workshop template", "id", templateId));

            mockMvc.perform(post("/api/admin/vendors/workshop-templates/{id}/reject", templateId)
                            .principal(mockPrincipal)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Workshop template not found with id: '" + templateId + "'"));
        }

        @Test
        @DisplayName("UTCID07 - Invalid UUID format - Should return 400")
        @WithMockUser(roles = "ADMIN")
        void utcid07_invalidUuidFormat_shouldReturn400() throws Exception {
            RejectWorkshopTemplateRequest request = new RejectWorkshopTemplateRequest();
            request.setAdminNote("Missing images");

            mockMvc.perform(post("/api/admin/vendors/workshop-templates/{id}/reject", "invalid-uuid")
                            .principal(mockPrincipal)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}
