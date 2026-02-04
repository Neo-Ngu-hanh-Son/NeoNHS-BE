package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.request.attraction.AttractionRequest;
import fpt.project.NeoNHS.dto.response.attraction.AttractionResponse;
import fpt.project.NeoNHS.entity.Attraction;
import fpt.project.NeoNHS.repository.AttractionRepository;
import fpt.project.NeoNHS.service.AttractionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttractionServiceImpl implements AttractionService {

    private final AttractionRepository attractionRepository;

    @Override
    public AttractionResponse createAttraction(AttractionRequest request) {
        if (request.getCloseHour().isBefore(request.getOpenHour()))
            throw new IllegalArgumentException("Close hour must be after open hour");

        Attraction attraction = Attraction.builder()
                .name(request.getName()).description(request.getDescription())
                .address(request.getAddress()).latitude(request.getLatitude())
                .longitude(request.getLongitude()).status(request.getStatus())
                .thumbnailUrl(request.getThumbnailUrl()).mapImageUrl(request.getMapImageUrl())
                .openHour(request.getOpenHour()).closeHour(request.getCloseHour())
                .isActive(true).build();

        return mapToResponse(attractionRepository.save(attraction));
    }

    @Override
    public List<AttractionResponse> getAllAttractions() {
        return attractionRepository.findAll()
                .stream()
                .filter(Attraction::getIsActive)
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public AttractionResponse getAttractionById(UUID id) {
        Attraction attraction = attractionRepository.findById(id)
                .filter(Attraction::getIsActive)
                .orElseThrow(() -> new RuntimeException("Attraction not found"));
        return mapToResponse(attraction);
    }

    @Override
    @Transactional
    public AttractionResponse updateAttraction(UUID id, AttractionRequest request) {
        Attraction attraction = attractionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attraction not found with id: " + id));

        if (request.getName() != null) attraction.setName(request.getName());
        if (request.getDescription() != null) attraction.setDescription(request.getDescription());
        if (request.getMapImageUrl() != null) attraction.setMapImageUrl(request.getMapImageUrl());
        if (request.getAddress() != null) attraction.setAddress(request.getAddress());
        if (request.getLatitude() != null) attraction.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) attraction.setLongitude(request.getLongitude());
        if (request.getStatus() != null) attraction.setStatus(request.getStatus());
        if (request.getThumbnailUrl() != null) attraction.setThumbnailUrl(request.getThumbnailUrl());
        if (request.getOpenHour() != null) attraction.setOpenHour(request.getOpenHour());
        if (request.getCloseHour() != null) attraction.setCloseHour(request.getCloseHour());

        if (attraction.getOpenHour() != null && attraction.getCloseHour() != null) {
            if (attraction.getCloseHour().isBefore(attraction.getOpenHour())) {
                throw new IllegalArgumentException("Close hour must be after open hour!");
            }
        }

        return mapToResponse(attractionRepository.save(attraction));
    }

    @Override
    @Transactional
    public void deleteAttraction(UUID id) {
        Attraction attraction = attractionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attraction not found with id: " + id));

        //not really delete just set is active = false
        attraction.setIsActive(false);
        attractionRepository.save(attraction);
    }

    private AttractionResponse mapToResponse(Attraction entity) {
        return AttractionResponse.builder()
                .id(entity.getId()).name(entity.getName()).description(entity.getDescription())
                .address(entity.getAddress()).latitude(entity.getLatitude()).longitude(entity.getLongitude())
                .status(entity.getStatus()).thumbnailUrl(entity.getThumbnailUrl())
                .openHour(entity.getOpenHour()).closeHour(entity.getCloseHour())
                .build();
    }
}
