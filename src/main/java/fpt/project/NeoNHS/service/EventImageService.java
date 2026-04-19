package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.response.event.EventImageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface EventImageService {

    List<EventImageResponse> getImagesByEventId(UUID eventId);

    List<EventImageResponse> uploadImages(UUID eventId, MultipartFile[] files);

    void deleteImage(UUID eventId, UUID imageId);

    void deleteImages(UUID eventId, List<UUID> imageIds);

    EventImageResponse setThumbnail(UUID eventId, UUID imageId);
}
