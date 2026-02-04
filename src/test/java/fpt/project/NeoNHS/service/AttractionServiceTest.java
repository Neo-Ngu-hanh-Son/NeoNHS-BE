package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.response.AttractionResponse;
import fpt.project.NeoNHS.dto.response.PagedResponse;
import fpt.project.NeoNHS.entity.Attraction;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.repository.AttractionRepository;
import fpt.project.NeoNHS.service.impl.AttractionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AttractionServiceImpl.
 * Uses Mockito to mock repository dependencies.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AttractionService Unit Tests")
class AttractionServiceTest {

    @Mock
    private AttractionRepository attractionRepository;

    @InjectMocks
    private AttractionServiceImpl attractionService;

    private Attraction sampleAttraction;
    private UUID sampleId;

    @BeforeEach
    void setUp() {
        sampleId = UUID.randomUUID();
        sampleAttraction = Attraction.builder()
                .id(sampleId)
                .name("Thủy Sơn (Water Mountain)")
                .description("The largest and most famous of the five Marble Mountains.")
                .address("Hòa Hải, Ngũ Hành Sơn, Đà Nẵng, Vietnam")
                .latitude(new BigDecimal("16.0034"))
                .longitude(new BigDecimal("108.2636"))
                .status("OPEN")
                .thumbnailUrl("https://example.com/thuyson.jpg")
                .mapImageUrl("https://example.com/map.jpg")
                .openHour(LocalTime.of(7, 0))
                .closeHour(LocalTime.of(17, 30))
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .points(Collections.emptyList())
                .build();
    }

    @Nested
    @DisplayName("getAllActiveAttractions")
    class GetAllActiveAttractions {

        @Test
        @DisplayName("Should return paged attractions with default sorting")
        void getAllActiveAttractions_WithDefaults_ReturnsPagedResponse() {
            // Given
            Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
            Page<Attraction> attractionPage = new PageImpl<>(
                    List.of(sampleAttraction), pageable, 1);

            given(attractionRepository.findActiveAttractions(eq(null), any(Pageable.class)))
                    .willReturn(attractionPage);

            // When
            PagedResponse<AttractionResponse> result = attractionService
                    .getAllActiveAttractions(null, 0, 10, "name", "asc");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getPage()).isEqualTo(0);
            assertThat(result.getSize()).isEqualTo(10);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.isFirst()).isTrue();
            assertThat(result.isLast()).isTrue();
            assertThat(result.isEmpty()).isFalse();

            verify(attractionRepository, times(1)).findActiveAttractions(eq(null), any(Pageable.class));
        }

        @Test
        @DisplayName("Should return paged attractions with keyword search")
        void getAllActiveAttractions_WithKeyword_ReturnsFilteredResults() {
            // Given
            String keyword = "water";
            Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
            Page<Attraction> attractionPage = new PageImpl<>(
                    List.of(sampleAttraction), pageable, 1);

            given(attractionRepository.findActiveAttractions(eq(keyword), any(Pageable.class)))
                    .willReturn(attractionPage);

            // When
            PagedResponse<AttractionResponse> result = attractionService
                    .getAllActiveAttractions(keyword, 0, 10, "name", "asc");

            // Then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).contains("Water Mountain");

            verify(attractionRepository, times(1)).findActiveAttractions(eq(keyword), any(Pageable.class));
        }

        @Test
        @DisplayName("Should return paged attractions with descending sort")
        void getAllActiveAttractions_WithDescSort_ReturnsSortedResults() {
            // Given
            Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
            Page<Attraction> attractionPage = new PageImpl<>(
                    List.of(sampleAttraction), pageable, 1);

            given(attractionRepository.findActiveAttractions(any(), any(Pageable.class)))
                    .willReturn(attractionPage);

            // When
            PagedResponse<AttractionResponse> result = attractionService
                    .getAllActiveAttractions(null, 0, 10, "createdAt", "desc");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);

            verify(attractionRepository, times(1)).findActiveAttractions(any(), any(Pageable.class));
        }

        @Test
        @DisplayName("Should return empty paged response when no attractions found")
        void getAllActiveAttractions_WhenNoAttractions_ReturnsEmptyPage() {
            // Given
            Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
            Page<Attraction> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            given(attractionRepository.findActiveAttractions(any(), any(Pageable.class)))
                    .willReturn(emptyPage);

            // When
            PagedResponse<AttractionResponse> result = attractionService
                    .getAllActiveAttractions(null, 0, 10, "name", "asc");

            // Then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("Should handle pagination correctly")
        void getAllActiveAttractions_WithPagination_ReturnsCorrectPage() {
            // Given
            Attraction secondAttraction = Attraction.builder()
                    .id(UUID.randomUUID())
                    .name("Mộc Sơn (Wood Mountain)")
                    .isActive(true)
                    .points(Collections.emptyList())
                    .build();

            Pageable pageable = PageRequest.of(1, 1, Sort.by("name").ascending());
            Page<Attraction> secondPage = new PageImpl<>(
                    List.of(secondAttraction), pageable, 2);

            given(attractionRepository.findActiveAttractions(any(), any(Pageable.class)))
                    .willReturn(secondPage);

            // When
            PagedResponse<AttractionResponse> result = attractionService
                    .getAllActiveAttractions(null, 1, 1, "name", "asc");

            // Then
            assertThat(result.getPage()).isEqualTo(1);
            assertThat(result.getSize()).isEqualTo(1);
            assertThat(result.getTotalPages()).isEqualTo(2);
            assertThat(result.isFirst()).isFalse();
            assertThat(result.isLast()).isTrue();
        }

        @Test
        @DisplayName("Should correctly map entity fields to DTO")
        void getAllActiveAttractions_MapsFieldsCorrectly() {
            // Given
            Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
            Page<Attraction> attractionPage = new PageImpl<>(
                    List.of(sampleAttraction), pageable, 1);

            given(attractionRepository.findActiveAttractions(any(), any(Pageable.class)))
                    .willReturn(attractionPage);

            // When
            PagedResponse<AttractionResponse> result = attractionService
                    .getAllActiveAttractions(null, 0, 10, "name", "asc");

            // Then
            AttractionResponse dto = result.getContent().get(0);
            assertThat(dto.getId()).isEqualTo(sampleAttraction.getId());
            assertThat(dto.getName()).isEqualTo(sampleAttraction.getName());
            assertThat(dto.getDescription()).isEqualTo(sampleAttraction.getDescription());
            assertThat(dto.getAddress()).isEqualTo(sampleAttraction.getAddress());
            assertThat(dto.getLatitude()).isEqualByComparingTo(sampleAttraction.getLatitude());
            assertThat(dto.getLongitude()).isEqualByComparingTo(sampleAttraction.getLongitude());
            assertThat(dto.getStatus()).isEqualTo(sampleAttraction.getStatus());
            assertThat(dto.getThumbnailUrl()).isEqualTo(sampleAttraction.getThumbnailUrl());
            assertThat(dto.getMapImageUrl()).isEqualTo(sampleAttraction.getMapImageUrl());
            assertThat(dto.getOpenHour()).isEqualTo(sampleAttraction.getOpenHour());
            assertThat(dto.getCloseHour()).isEqualTo(sampleAttraction.getCloseHour());
            assertThat(dto.getPointCount()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("getActiveAttractionById")
    class GetActiveAttractionById {

        @Test
        @DisplayName("Should return attraction when found")
        void getActiveAttractionById_WhenExists_ReturnsAttraction() {
            // Given
            given(attractionRepository.findByIdAndIsActiveTrue(sampleId))
                    .willReturn(Optional.of(sampleAttraction));

            // When
            AttractionResponse result = attractionService.getActiveAttractionById(sampleId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(sampleId);
            assertThat(result.getName()).isEqualTo("Thủy Sơn (Water Mountain)");

            verify(attractionRepository, times(1)).findByIdAndIsActiveTrue(sampleId);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when not found")
        void getActiveAttractionById_WhenNotExists_ThrowsException() {
            // Given
            UUID nonExistentId = UUID.randomUUID();
            given(attractionRepository.findByIdAndIsActiveTrue(nonExistentId))
                    .willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> attractionService.getActiveAttractionById(nonExistentId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Attraction")
                    .hasMessageContaining("id")
                    .hasMessageContaining(nonExistentId.toString());

            verify(attractionRepository, times(1)).findByIdAndIsActiveTrue(nonExistentId);
        }

        @Test
        @DisplayName("Should correctly map all fields when found")
        void getActiveAttractionById_MapsAllFields() {
            // Given
            given(attractionRepository.findByIdAndIsActiveTrue(sampleId))
                    .willReturn(Optional.of(sampleAttraction));

            // When
            AttractionResponse result = attractionService.getActiveAttractionById(sampleId);

            // Then
            assertThat(result.getId()).isEqualTo(sampleAttraction.getId());
            assertThat(result.getName()).isEqualTo(sampleAttraction.getName());
            assertThat(result.getDescription()).isEqualTo(sampleAttraction.getDescription());
            assertThat(result.getAddress()).isEqualTo(sampleAttraction.getAddress());
            assertThat(result.getStatus()).isEqualTo(sampleAttraction.getStatus());
        }
    }

    @Nested
    @DisplayName("countActiveAttractions")
    class CountActiveAttractions {

        @Test
        @DisplayName("Should return count of active attractions")
        void countActiveAttractions_ReturnsCount() {
            // Given
            given(attractionRepository.countByIsActiveTrue()).willReturn(5L);

            // When
            long result = attractionService.countActiveAttractions();

            // Then
            assertThat(result).isEqualTo(5L);
            verify(attractionRepository, times(1)).countByIsActiveTrue();
        }

        @Test
        @DisplayName("Should return zero when no active attractions")
        void countActiveAttractions_WhenNoAttractions_ReturnsZero() {
            // Given
            given(attractionRepository.countByIsActiveTrue()).willReturn(0L);

            // When
            long result = attractionService.countActiveAttractions();

            // Then
            assertThat(result).isEqualTo(0L);
        }

        @Test
        @DisplayName("Should return large count correctly")
        void countActiveAttractions_WithLargeCount_ReturnsCorrectly() {
            // Given
            given(attractionRepository.countByIsActiveTrue()).willReturn(1000000L);

            // When
            long result = attractionService.countActiveAttractions();

            // Then
            assertThat(result).isEqualTo(1000000L);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle attraction with null points list")
        void mapToResponse_WithNullPoints_ReturnsZeroPointCount() {
            // Given
            Attraction attractionWithNullPoints = Attraction.builder()
                    .id(UUID.randomUUID())
                    .name("Test Mountain")
                    .isActive(true)
                    .points(null)
                    .build();

            Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
            Page<Attraction> attractionPage = new PageImpl<>(
                    List.of(attractionWithNullPoints), pageable, 1);

            given(attractionRepository.findActiveAttractions(any(), any(Pageable.class)))
                    .willReturn(attractionPage);

            // When
            PagedResponse<AttractionResponse> result = attractionService
                    .getAllActiveAttractions(null, 0, 10, "name", "asc");

            // Then
            assertThat(result.getContent().get(0).getPointCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle attraction with empty keyword")
        void getAllActiveAttractions_WithEmptyKeyword_TreatsAsNull() {
            // Given
            Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
            Page<Attraction> attractionPage = new PageImpl<>(
                    List.of(sampleAttraction), pageable, 1);

            given(attractionRepository.findActiveAttractions(eq(""), any(Pageable.class)))
                    .willReturn(attractionPage);

            // When
            PagedResponse<AttractionResponse> result = attractionService
                    .getAllActiveAttractions("", 0, 10, "name", "asc");

            // Then
            assertThat(result.getContent()).hasSize(1);
        }
    }
}
