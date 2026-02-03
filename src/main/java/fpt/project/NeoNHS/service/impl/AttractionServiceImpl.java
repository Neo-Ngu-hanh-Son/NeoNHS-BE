package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.request.attraction.CreateAttractionRequest;
import fpt.project.NeoNHS.entity.Attraction;
import fpt.project.NeoNHS.entity.Point;
import fpt.project.NeoNHS.repository.AttractionRepository;
import fpt.project.NeoNHS.repository.PointRepository;
import fpt.project.NeoNHS.service.AttractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttractionServiceImpl implements AttractionService {

    private final AttractionRepository attractionRepository;
    private final PointRepository pointRepository;

    @Override
    public void createAttraction(CreateAttractionRequest request) {
        Attraction attraction = Attraction.builder()
                .name(request.getName())
                .description(request.getDescription())
                .mapImageUrl(request.getMapImageUrl())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .status(request.getStatus())
                .thumbnailUrl(request.getThumbnailUrl())
                .openHour(request.getOpenHour())
                .closeHour(request.getCloseHour())
                .isActive(true)
                .build();

        attractionRepository.save(attraction);
    }

    @Override
    public List<Attraction> getAllAttractions() {
        // Trả về tất cả các điểm đến đang hoạt động (isActive = true)
        return attractionRepository.findAll()
                .stream()
                .filter(Attraction::getIsActive)
                .toList();
    }

    @Override
    public List<Point> getPointsByAttraction(UUID attractionId) {
        // Kiểm tra xem Attraction có tồn tại không trước khi lấy Point (optional nhưng nên có)
        if (!attractionRepository.existsById(attractionId)) {
            throw new RuntimeException("Attraction not found with id: " + attractionId);
        }
        return pointRepository.findByAttractionIdOrderByOrderIndexAsc(attractionId);
    }
}
