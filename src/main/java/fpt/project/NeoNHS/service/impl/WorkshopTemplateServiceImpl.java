package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.request.workshop.CreateWorkshopTemplateRequest;
import fpt.project.NeoNHS.dto.request.workshop.UpdateWorkshopTemplateRequest;

import fpt.project.NeoNHS.dto.response.workshop.WTagResponse;
import fpt.project.NeoNHS.dto.response.workshop.WorkshopImageResponse;
import fpt.project.NeoNHS.dto.response.workshop.WorkshopTemplateResponse;
import fpt.project.NeoNHS.entity.*;
import fpt.project.NeoNHS.enums.WorkshopStatus;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.repository.VendorProfileRepository;
import fpt.project.NeoNHS.repository.WTagRepository;
import fpt.project.NeoNHS.repository.WorkshopImageRepository;
import fpt.project.NeoNHS.repository.WorkshopTagRepository;
import fpt.project.NeoNHS.repository.WorkshopTemplateRepository;

import fpt.project.NeoNHS.service.WorkshopTemplateService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkshopTemplateServiceImpl implements WorkshopTemplateService {

    private final WorkshopTemplateRepository workshopTemplateRepository;
    private final VendorProfileRepository vendorProfileRepository;
    private final WTagRepository wTagRepository;
    private final WorkshopImageRepository workshopImageRepository;
    private final WorkshopTagRepository workshopTagRepository;

    // ==================== CREATE ====================

    @Override
    @Transactional
    public WorkshopTemplateResponse createWorkshopTemplate(String email, CreateWorkshopTemplateRequest request) {
        // 1. Find vendor profile by email
        VendorProfile vendor = vendorProfileRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("VendorProfile", "email", email));

        // 2. Check if vendor is verified
        if (!vendor.getIsVerified()) {
            throw new BadRequestException("Only verified vendors can create workshop templates");
        }

        // 3. Validate min/max participants
        if (request.getMinParticipants() != null && request.getMaxParticipants() != null) {
            if (request.getMinParticipants() > request.getMaxParticipants()) {
                throw new BadRequestException("Minimum participants cannot be greater than maximum participants");
            }
        }

        // 4. Validate and fetch tags
        List<WTag> tags = wTagRepository.findAllByIdIn(request.getTagIds());
        if (tags.size() != request.getTagIds().size()) {
            throw new BadRequestException("One or more tag IDs are invalid");
        }

        // 5. Validate thumbnail index
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            if (request.getThumbnailIndex() != null && request.getThumbnailIndex() >= request.getImageUrls().size()) {
                throw new BadRequestException("Thumbnail index is out of bounds");
            }
        }

        // 6. Create WorkshopTemplate entity
        WorkshopTemplate workshopTemplate = WorkshopTemplate.builder()
                .name(request.getName())
                .shortDescription(request.getShortDescription())
                .fullDescription(request.getFullDescription())
                .estimatedDuration(request.getEstimatedDuration())
                .defaultPrice(request.getDefaultPrice())
                .minParticipants(request.getMinParticipants())
                .maxParticipants(request.getMaxParticipants())
                .vendor(vendor)
                .build();

        // 7. Create WorkshopImage entities
        List<WorkshopImage> workshopImages = new ArrayList<>();
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            int thumbnailIndex = request.getThumbnailIndex() != null ? request.getThumbnailIndex() : 0;
            for (int i = 0; i < request.getImageUrls().size(); i++) {
                WorkshopImage image = WorkshopImage.builder()
                        .imageUrl(request.getImageUrls().get(i))
                        .isThumbnail(i == thumbnailIndex)
                        .workshopTemplate(workshopTemplate)
                        .build();
                workshopImages.add(image);
            }
        }
        workshopTemplate.setWorkshopImages(workshopImages);

        // 8. Create WorkshopTag entities (many-to-many relationship)
        List<WorkshopTag> workshopTags = new ArrayList<>();
        for (WTag tag : tags) {
            WorkshopTagId tagId = new WorkshopTagId(workshopTemplate.getId(), tag.getId());
            WorkshopTag workshopTag = WorkshopTag.builder()
                    .id(tagId)
                    .workshopTemplate(workshopTemplate)
                    .wTag(tag)
                    .build();
            workshopTags.add(workshopTag);
        }
        workshopTemplate.setWorkshopTags(workshopTags);

        // 9. Save workshop template (cascades to images and tags)
        WorkshopTemplate savedTemplate = workshopTemplateRepository.save(workshopTemplate);

        // 10. Map to response
        return mapToResponse(savedTemplate, tags);
    }

    // ==================== READ ====================

    @Override
    public WorkshopTemplateResponse getWorkshopTemplateById(UUID id) {
        WorkshopTemplate template = workshopTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkshopTemplate", "id", id));
        return mapToResponse(template);
    }

    @Override
    public List<WorkshopTemplateResponse> getAllWorkshopTemplates() {
        return workshopTemplateRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // @Override
    // public List<WorkshopTemplateResponse>
    // getWorkshopTemplatesByStatus(WorkshopStatus status) {
    // return workshopTemplateRepository.findByStatus(status)
    // .stream()
    // .map(this::mapToResponse)
    // .collect(Collectors.toList());
    // }

    @Override
    public List<WorkshopTemplateResponse> getMyWorkshopTemplates(String email) {
        List<WorkshopTemplate> templates = workshopTemplateRepository.findByVendorUserEmail(email);
        return templates.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ==================== UPDATE ====================

    @Override
    @Transactional
    public WorkshopTemplateResponse updateWorkshopTemplate(String email, UUID id,
            UpdateWorkshopTemplateRequest request) {
        // 1. Find the workshop template
        WorkshopTemplate template = workshopTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkshopTemplate", "id", id));

        // 2. Verify ownership
        if (!template.getVendor().getUser().getEmail().equals(email)) {
            throw new BadRequestException("You do not have permission to update this workshop template");
        }

        // 3. Only allow update if status is PENDING or REJECTED
        if (template.getStatus() == WorkshopStatus.ACTIVE) {
            throw new BadRequestException("Cannot update an active workshop template");
        }

        // 4. Update basic fields if provided
        if (request.getName() != null) {
            template.setName(request.getName());
        }
        if (request.getShortDescription() != null) {
            template.setShortDescription(request.getShortDescription());
        }
        if (request.getFullDescription() != null) {
            template.setFullDescription(request.getFullDescription());
        }
        if (request.getEstimatedDuration() != null) {
            template.setEstimatedDuration(request.getEstimatedDuration());
        }
        if (request.getDefaultPrice() != null) {
            template.setDefaultPrice(request.getDefaultPrice());
        }
        if (request.getMinParticipants() != null) {
            template.setMinParticipants(request.getMinParticipants());
        }
        if (request.getMaxParticipants() != null) {
            template.setMaxParticipants(request.getMaxParticipants());
        }

        // 5. Validate min/max participants
        if (template.getMinParticipants() != null && template.getMaxParticipants() != null) {
            if (template.getMinParticipants() > template.getMaxParticipants()) {
                throw new BadRequestException("Minimum participants cannot be greater than maximum participants");
            }
        }

        // 6. Update images if provided
        if (request.getImageUrls() != null) {
            // Validate thumbnail index
            if (request.getThumbnailIndex() != null && request.getThumbnailIndex() >= request.getImageUrls().size()) {
                throw new BadRequestException("Thumbnail index is out of bounds");
            }

            // Remove old images
            if (template.getWorkshopImages() != null) {
                workshopImageRepository.deleteAll(template.getWorkshopImages());
            }

            // Create new images
            List<WorkshopImage> newImages = new ArrayList<>();
            int thumbnailIndex = request.getThumbnailIndex() != null ? request.getThumbnailIndex() : 0;
            for (int i = 0; i < request.getImageUrls().size(); i++) {
                WorkshopImage image = WorkshopImage.builder()
                        .imageUrl(request.getImageUrls().get(i))
                        .isThumbnail(i == thumbnailIndex)
                        .workshopTemplate(template)
                        .build();
                newImages.add(image);
            }
            template.setWorkshopImages(newImages);
        }

        // 7. Update tags if provided
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            // Validate tags exist
            List<WTag> tags = wTagRepository.findAllByIdIn(request.getTagIds());
            if (tags.size() != request.getTagIds().size()) {
                throw new BadRequestException("One or more tag IDs are invalid");
            }

            // Remove old tags
            if (template.getWorkshopTags() != null) {
                workshopTagRepository.deleteAll(template.getWorkshopTags());
            }

            // Create new tags
            List<WorkshopTag> newTags = new ArrayList<>();
            for (WTag tag : tags) {
                WorkshopTagId tagId = new WorkshopTagId(template.getId(), tag.getId());
                WorkshopTag workshopTag = WorkshopTag.builder()
                        .id(tagId)
                        .workshopTemplate(template)
                        .wTag(tag)
                        .build();
                newTags.add(workshopTag);
            }
            template.setWorkshopTags(newTags);
        }

        // 8. Reset status to PENDING if it was REJECTED (resubmission)
        if (template.getStatus() == WorkshopStatus.REJECTED) {
            template.setStatus(WorkshopStatus.PENDING);
            template.setRejectReason(null);
        }

        // 9. Save and return
        WorkshopTemplate updatedTemplate = workshopTemplateRepository.save(template);
        return mapToResponse(updatedTemplate);
    }

    // ==================== DELETE ====================

    @Override
    @Transactional
    public void deleteWorkshopTemplate(String email, UUID id) {
        // 1. Find the workshop template
        WorkshopTemplate template = workshopTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkshopTemplate", "id", id));

        // 2. Verify ownership
        if (!template.getVendor().getUser().getEmail().equals(email)) {
            throw new BadRequestException("You do not have permission to delete this workshop template");
        }

        // 3. Only allow delete if status is PENDING or REJECTED (no active sessions)
        if (template.getStatus() == WorkshopStatus.ACTIVE) {
            throw new BadRequestException("Cannot delete an active workshop template. Please deactivate it first.");
        }

        // 4. Delete the template (cascades to images and tags)
        workshopTemplateRepository.delete(template);
    }

    // ==================== MAPPERS ====================

    private WorkshopTemplateResponse mapToResponse(WorkshopTemplate template) {
        List<WTag> tags = new ArrayList<>();
        if (template.getWorkshopTags() != null) {
            tags = template.getWorkshopTags().stream()
                    .map(WorkshopTag::getWTag)
                    .collect(Collectors.toList());
        }
        return mapToResponse(template, tags);
    }

    private WorkshopTemplateResponse mapToResponse(WorkshopTemplate template, List<WTag> tags) {
        // Map images
        List<WorkshopImageResponse> imageResponses = new ArrayList<>();
        if (template.getWorkshopImages() != null) {
            imageResponses = template.getWorkshopImages().stream()
                    .map(img -> WorkshopImageResponse.builder()
                            .id(img.getId())
                            .imageUrl(img.getImageUrl())
                            .isThumbnail(img.getIsThumbnail())
                            .build())
                    .collect(Collectors.toList());
        }

        // Map tags
        List<WTagResponse> tagResponses = tags.stream()
                .map(tag -> WTagResponse.builder()
                        .id(tag.getId())
                        .name(tag.getName())
                        .description(tag.getDescription())
                        .tagColor(tag.getTagColor())
                        .iconUrl(tag.getIconUrl())
                        .build())
                .collect(Collectors.toList());

        return WorkshopTemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .shortDescription(template.getShortDescription())
                .fullDescription(template.getFullDescription())
                .estimatedDuration(template.getEstimatedDuration())
                .defaultPrice(template.getDefaultPrice())
                .minParticipants(template.getMinParticipants())
                .maxParticipants(template.getMaxParticipants())
                .status(template.getStatus())
                .averageRating(template.getAverageRating())
                .totalReview(template.getTotalReview())
                .vendorId(template.getVendor().getId())
                .vendorName(template.getVendor().getBusinessName())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .images(imageResponses)
                .tags(tagResponses)
                .build();
    }
}
