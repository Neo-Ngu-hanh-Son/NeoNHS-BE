package fpt.project.NeoNHS.controller.blogs;

import fpt.project.NeoNHS.controller.BlogController;
import fpt.project.NeoNHS.dto.response.blog.BlogResponse;
import fpt.project.NeoNHS.enums.BlogStatus;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.exception.UnauthorizedException;
import fpt.project.NeoNHS.security.CustomUserDetailsService;
import fpt.project.NeoNHS.security.JwtTokenProvider;
import fpt.project.NeoNHS.service.BlogService;
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
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BlogController.class)
@AutoConfigureMockMvc(addFilters = false)
class BlogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BlogService blogService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private BlogResponse blogResponse;

    @BeforeEach
    void setUp() {
        blogResponse = BlogResponse.builder()
                .id(UUID.randomUUID())
                .title("History of Old Capital")
                .slug("history-of-old-capital")
                .summary("A short summary")
                .contentJSON("{}")
                .contentHTML("<p>content</p>")
                .isFeatured(false)
                .status(BlogStatus.PUBLISHED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Get All Blogs Tests")
    class GetAllBlogsTests {

        // ----- UTCID01: Normal — defaults only (200 OK) -----
        @Test
        @DisplayName("UTCID01 - Default request -> 200 OK")
        void utcid01_defaultRequest_shouldReturn200() throws Exception {
            Page<BlogResponse> page = new PageImpl<>(List.of(blogResponse));
            Mockito.when(blogService.getActiveBlogs(any(), any(), any(), any(Pageable.class), eq(false), any()))
                    .thenReturn(page);

            mockMvc.perform(get("/api/blogs"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("Blogs retrieved successfully"))
                    .andExpect(jsonPath("$.data.content.length()").value(1));

            Mockito.verify(blogService).getActiveBlogs(
                    eq(null),
                    eq(null),
                    eq(null),
                    argThat(pageable -> pageable.getPageNumber() == 0
                            && pageable.getPageSize() == 10
                            && pageable.getSort().getOrderFor("createdAt") != null
                            && pageable.getSort().getOrderFor("createdAt").getDirection() == Sort.Direction.DESC),
                    eq(false),
                    eq(null)
            );
        }

        // ----- UTCID02: Normal — full filter request (200 OK) -----
        @Test
        @DisplayName("UTCID02 - Full filters and ASC sort -> 200 OK")
        void utcid02_fullFilters_shouldReturn200() throws Exception {
            Page<BlogResponse> page = new PageImpl<>(List.of(blogResponse));
            Mockito.when(blogService.getActiveBlogs(any(), any(), any(), any(Pageable.class), eq(true), any()))
                    .thenReturn(page);

            mockMvc.perform(get("/api/blogs")
                            .param("page", "1")
                            .param("size", "5")
                            .param("search", "heritage")
                            .param("status", "PUBLISHED")
                            .param("tags", "history", "culture")
                            .param("sortBy", "title")
                            .param("sortDir", "ASC")
                            .param("isFeatured", "true")
                            .param("categorySlug", "culture"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Blogs retrieved successfully"))
                    .andExpect(jsonPath("$.data.content.length()").value(1));

            Mockito.verify(blogService).getActiveBlogs(
                    eq("heritage"),
                    eq(BlogStatus.PUBLISHED),
                    eq(List.of("history", "culture")),
                    argThat(pageable -> pageable.getPageNumber() == 1
                            && pageable.getPageSize() == 5
                            && pageable.getSort().getOrderFor("title") != null
                            && pageable.getSort().getOrderFor("title").getDirection() == Sort.Direction.ASC),
                    eq(true),
                    eq("culture")
            );
        }

        // ----- UTCID03: Boundary — empty result page (200 OK) -----
        @Test
        @DisplayName("UTCID03 - Empty result page -> 200 OK")
        void utcid03_emptyResult_shouldReturn200() throws Exception {
            Page<BlogResponse> page = new PageImpl<>(List.of());
            Mockito.when(blogService.getActiveBlogs(eq("no-data"), any(), any(), any(Pageable.class), eq(false), any()))
                    .thenReturn(page);

            mockMvc.perform(get("/api/blogs")
                            .param("search", "no-data"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content.length()").value(0));
        }

        // ----- UTCID04: Abnormal — infra failure from service (500 Internal Server Error) -----
        @Test
        @DisplayName("UTCID04 - DB failure from service -> 500 Internal Server Error")
        void utcid04_serviceDbFailure_shouldReturn500() throws Exception {
            Mockito.when(blogService.getActiveBlogs(any(), any(), any(), any(Pageable.class), eq(false), any()))
                    .thenThrow(new DataAccessResourceFailureException("Database unreachable"));

            mockMvc.perform(get("/api/blogs"))
                    .andDo(print())
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
        }
    }

    @Nested
    @DisplayName("Get Blog By ID Tests")
    class GetBlogByIdTests {

        // ----- UTCID01: Normal — valid blog id (200 OK) -----
        @Test
        @DisplayName("UTCID01 - Valid id -> 200 OK")
        void utcid01_validId_shouldReturn200() throws Exception {
            UUID id = UUID.randomUUID();
            Mockito.when(blogService.getBlogById(id)).thenReturn(blogResponse);

            mockMvc.perform(get("/api/blogs/{id}", id))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("Blog retrieved successfully"))
                    .andExpect(jsonPath("$.data.id").value(blogResponse.getId().toString()));

            Mockito.verify(blogService).getBlogById(id);
        }

        // ----- UTCID02: Abnormal business flow — blog not found (404 Not Found) -----
        @Test
        @DisplayName("UTCID02 - Blog not found -> 404 Not Found")
        void utcid02_blogNotFound_shouldReturn404() throws Exception {
            UUID id = UUID.randomUUID();
            Mockito.when(blogService.getBlogById(id))
                    .thenThrow(new ResourceNotFoundException("Blog not found"));

            mockMvc.perform(get("/api/blogs/{id}", id))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Blog not found"));
        }

        // ----- UTCID03: Abnormal business flow — archived/draft not accessible by non-admin (401) -----
        @Test
        @DisplayName("UTCID03 - Unauthorized when blog is restricted -> 401 Unauthorized")
        void utcid03_unauthorizedRestrictedBlog_shouldReturn401() throws Exception {
            UUID id = UUID.randomUUID();
            Mockito.when(blogService.getBlogById(id))
                    .thenThrow(new UnauthorizedException("You are not allowed to view this blog"));

            mockMvc.perform(get("/api/blogs/{id}", id))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("You are not allowed to view this blog"));
        }

        // ----- UTCID04: Abnormal infrastructure flow — DB failure (500) -----
        @Test
        @DisplayName("UTCID04 - DB failure -> 500 Internal Server Error")
        void utcid04_dbFailure_shouldReturn500() throws Exception {
            UUID id = UUID.randomUUID();
            Mockito.when(blogService.getBlogById(id))
                    .thenThrow(new DataAccessResourceFailureException("Database down"));

            mockMvc.perform(get("/api/blogs/{id}", id))
                    .andDo(print())
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
        }
    }

    @Nested
    @DisplayName("Increment View Count Tests")
    class IncrementViewCountTests {

        // ----- UTCID01: Normal — increment view count success (200 OK) -----
        @Test
        @DisplayName("UTCID01 - Increment view count -> 200 OK")
        void utcid01_incrementViewCount_shouldReturn200() throws Exception {
            UUID id = UUID.randomUUID();
            Mockito.doNothing().when(blogService).incrementViewCount(id);

            mockMvc.perform(post("/api/blogs/{id}/view", id))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("Success"))
                    .andExpect(jsonPath("$.data").value("Ok"));

            Mockito.verify(blogService).incrementViewCount(id);
        }

        // ----- UTCID02: Abnormal — service failure when incrementing (500) -----
        @Test
        @DisplayName("UTCID02 - Increment view count DB failure -> 500 Internal Server Error")
        void utcid02_incrementViewCountDbFailure_shouldReturn500() throws Exception {
            UUID id = UUID.randomUUID();
            Mockito.doThrow(new DataAccessResourceFailureException("Redis unavailable"))
                    .when(blogService).incrementViewCount(id);

            mockMvc.perform(post("/api/blogs/{id}/view", id))
                    .andDo(print())
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
        }
    }
}


