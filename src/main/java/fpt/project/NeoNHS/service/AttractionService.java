package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.response.AttractionResponse;
import fpt.project.NeoNHS.dto.response.PagedResponse;

import java.util.UUID;

/**
 * Service interface for Attraction operations
 */
public interface AttractionService {

    /**
     * Get all active attractions with pagination and optional search
     *
     * @param keyword  Optional search keyword for attraction name
     * @param page     Page number (0-indexed)
     * @param size     Page size
     * @param sortBy   Field to sort by
     * @param sortDir  Sort direction (asc/desc)
     * @return Paged response of attractions
     */
    PagedResponse<AttractionResponse> getAllActiveAttractions(
            String keyword, int page, int size, String sortBy, String sortDir);

    /**
     * Get a single active attraction by ID
     *
     * @param id Attraction UUID
     * @return Attraction response
     */
    AttractionResponse getActiveAttractionById(UUID id);

    /**
     * Count all active attractions
     *
     * @return Count of active attractions
     */
    long countActiveAttractions();
}

