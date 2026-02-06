package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.attraction.AttractionRequest;
import fpt.project.NeoNHS.dto.response.attraction.AttractionResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface AttractionService {
    AttractionResponse createAttraction(AttractionRequest request);

    List<AttractionResponse> getAllAttractions();

    AttractionResponse getAttractionById(UUID id);

    AttractionResponse updateAttraction(UUID id, AttractionRequest request);

    void deleteAttraction(UUID id);

    Page<AttractionResponse> getAllAttractionsWithPagination(int page, int size, String sortBy, String sortDir, String search);
}
