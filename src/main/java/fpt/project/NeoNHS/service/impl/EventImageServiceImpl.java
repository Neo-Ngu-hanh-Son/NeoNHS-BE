package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.response.event.EventImageResponse;
import fpt.project.NeoNHS.dto.response.upload.ImageUploadResponse;
import fpt.project.NeoNHS.entity.Event;
import fpt.project.NeoNHS.entity.EventImage;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.repository.EventImageRepository;
import fpt.project.NeoNHS.repository.EventRepository;
import fpt.project.NeoNHS.service.EventImageService;
import fpt.project.NeoNHS.service.ImageUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventImageServiceImpl implements EventImageService {

    private final EventImageRepository eventImageRepository;
    private final EventRepository eventRepository;
    private final ImageUploadService imageUploadService;

    @Override
    @Transactional(readOnly = true)
    public List<EventImageResponse> getImagesByEventId(UUID eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found with id: " + eventId);
        }
        return eventImageRepository.findByEventIdAndDeletedAtIsNull(eventId).stream()
                .map(EventImageResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public List<EventImageResponse> uploadImages(UUID eventId, MultipartFile[] files) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        List<EventImageResponse> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            ImageUploadResponse uploadRes = imageUploadService.uploadImage(file);

            EventImage eventImage = EventImage.builder()
                    .imageUrl(uploadRes.getMediaUrl())
                    .isThumbnail(false)
                    .event(event)
                    .build();

            EventImage saved = eventImageRepository.save(eventImage);
            responses.add(EventImageResponse.fromEntity(saved));
        }

        return responses;
    }

    @Override
    @Transactional
    public void deleteImage(UUID eventId, UUID imageId) {
        EventImage image = eventImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + imageId));

        validateImageBelongsToEvent(eventId, image);

        if (Boolean.TRUE.equals(image.getIsThumbnail())) {
            throw new BadRequestException("Cannot delete the current thumbnail image. Please set another image as thumbnail first.");
        }

        image.setDeletedAt(LocalDateTime.now());
        eventImageRepository.save(image);
    }

    @Override
    @Transactional
    public void deleteImages(UUID eventId, List<UUID> imageIds) {
        List<EventImage> images = eventImageRepository.findAllById(imageIds);
        
        for (EventImage image : images) {
            validateImageBelongsToEvent(eventId, image);

            if (Boolean.TRUE.equals(image.getIsThumbnail())) {
                throw new BadRequestException("Cannot delete image with ID " + image.getId() + " because it is the current thumbnail.");
            }
            image.setDeletedAt(LocalDateTime.now());
        }
        eventImageRepository.saveAll(images);
    }

    private void validateImageBelongsToEvent(UUID eventId, EventImage image) {
        if (!image.getEvent().getId().equals(eventId)) {
            throw new BadRequestException("Image " + image.getId() + " does not belong to the specified event");
        }
    }

    @Override
    @Transactional
    public EventImageResponse setThumbnail(UUID eventId, UUID imageId) {
        EventImage newThumbnail = eventImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found with id: " + imageId));

        if (!newThumbnail.getEvent().getId().equals(eventId)) {
            throw new BadRequestException("Image does not belong to the specified event");
        }

        // Unset old thumbnail
        Optional<EventImage> existingThumbnail = eventImageRepository.findByEventIdAndIsThumbnailTrue(eventId);
        if (existingThumbnail.isPresent()) {
            EventImage oldThumbnail = existingThumbnail.get();
            oldThumbnail.setIsThumbnail(false);
            eventImageRepository.save(oldThumbnail);
        }

        // Set new thumbnail
        newThumbnail.setIsThumbnail(true);
        EventImage saved = eventImageRepository.save(newThumbnail);
        return EventImageResponse.fromEntity(saved);
    }
}
