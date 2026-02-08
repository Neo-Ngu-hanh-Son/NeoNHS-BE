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
import fpt.project.NeoNHS.repository.UserRepository;
import fpt.project.NeoNHS.repository.VendorProfileRepository;
import fpt.project.NeoNHS.repository.WTagRepository;
import fpt.project.NeoNHS.repository.WorkshopImageRepository;
import fpt.project.NeoNHS.repository.WorkshopTagRepository;
import fpt.project.NeoNHS.repository.WorkshopTemplateRepository;

import fpt.project.NeoNHS.service.WorkshopTemplateService;
import fpt.project.NeoNHS.specification.WorkshopTemplateSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private final UserRepository userRepository;

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

    @Override
    public Page<WorkshopTemplateResponse> getAllWorkshopTemplates(Pageable pageable) {
        Page<WorkshopTemplate> templates = workshopTemplateRepository.findAll(pageable);
        return templates.map(this::mapToResponse);
    }


    @Override
    public List<WorkshopTemplateResponse> getMyWorkshopTemplates(String email) {
        List<WorkshopTemplate> templates = workshopTemplateRepository.findByVendorUserEmail(email);
        return templates.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<WorkshopTemplateResponse> getMyWorkshopTemplates(String email, Pageable pageable) {
        // Find vendor first to validate email
        VendorProfile vendor = vendorProfileRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("VendorProfile", "email", email));

        Page<WorkshopTemplate> templates = workshopTemplateRepository.findByVendorId(vendor.getId(), pageable);
        return templates.map(this::mapToResponse);
    }

    // ==================== SEARCH & FILTER ====================

    @Override
    public List<WorkshopTemplateResponse> searchWorkshopTemplates(
            String keyword,
            String name,
            WorkshopStatus status,
            UUID vendorId,
            UUID tagId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer minDuration,
            Integer maxDuration,
            BigDecimal minRating
    ) {
        Specification<WorkshopTemplate> spec = Specification.where((root, query, cb) -> cb.conjunction());

        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and(WorkshopTemplateSpecification.searchByKeyword(keyword));
        }
        if (name != null && !name.isEmpty()) {
            spec = spec.and(WorkshopTemplateSpecification.hasName(name));
        }
        if (status != null) {
            spec = spec.and(WorkshopTemplateSpecification.hasStatus(status));
        }
        if (vendorId != null) {
            spec = spec.and(WorkshopTemplateSpecification.hasVendorId(vendorId));
        }
        if (tagId != null) {
            spec = spec.and(WorkshopTemplateSpecification.hasTagId(tagId));
        }
        if (minPrice != null || maxPrice != null) {
            spec = spec.and(WorkshopTemplateSpecification.hasPriceBetween(minPrice, maxPrice));
        }
        if (minDuration != null || maxDuration != null) {
            spec = spec.and(WorkshopTemplateSpecification.hasDurationBetween(minDuration, maxDuration));
        }
        if (minRating != null) {
            spec = spec.and(WorkshopTemplateSpecification.hasMinRating(minRating));
        }

        return workshopTemplateRepository.findAll(spec).stream()
                .map(template -> mapToResponse(template))
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

        // 3. Only allow update if status is DRAFT or REJECTED
        if (template.getStatus() == WorkshopStatus.PENDING) {
            throw new BadRequestException("Cannot update a template that is pending approval. Please wait for admin review.");
        }
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

        // 8. Save and return
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

        // 3. Only allow delete if status is DRAFT, PENDING or REJECTED (no active sessions)
        if (template.getStatus() == WorkshopStatus.ACTIVE) {
            throw new BadRequestException("Cannot delete an active workshop template. Please deactivate it first.");
        }

        // 4. Delete the template (cascades to images and tags)
        workshopTemplateRepository.delete(template);
    }

    // ==================== REGISTER/SUBMIT FOR APPROVAL ====================

    @Override
    @Transactional
    public WorkshopTemplateResponse registerWorkshopTemplate(String email, UUID id) {
        // 1. Find the workshop template
        WorkshopTemplate template = workshopTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkshopTemplate", "id", id));

        // 2. Verify ownership
        if (!template.getVendor().getUser().getEmail().equals(email)) {
            throw new BadRequestException("You do not have permission to submit this workshop template");
        }

        // 3. Check if template status allows submission (only DRAFT or REJECTED)
        if (template.getStatus() != WorkshopStatus.DRAFT && template.getStatus() != WorkshopStatus.REJECTED) {
            throw new BadRequestException("Only templates with 'Draft' or 'Rejected' status can be submitted for approval");
        }

        // 4. Validate mandatory fields for submission
        validateTemplateCompleteness(template);

        // 5. Update status to PENDING and clear rejection reason if exists
        template.setStatus(WorkshopStatus.PENDING);
        if (template.getRejectReason() != null) {
            template.setRejectReason(null);
        }

        // 6. Save and return
        WorkshopTemplate submittedTemplate = workshopTemplateRepository.save(template);
        return mapToResponse(submittedTemplate);
    }

    /**
     * Validates that all mandatory fields are present before submission
     */
    private void validateTemplateCompleteness(WorkshopTemplate template) {
        List<String> missingFields = new ArrayList<>();

        // Check mandatory fields
        if (template.getName() == null || template.getName().trim().isEmpty()) {
            missingFields.add("Title/Name");
        }
        if (template.getShortDescription() == null || template.getShortDescription().trim().isEmpty()) {
            missingFields.add("Short Description");
        }
        if (template.getFullDescription() == null || template.getFullDescription().trim().isEmpty()) {
            missingFields.add("Full Description");
        }
        if (template.getDefaultPrice() == null) {
            missingFields.add("Price");
        }
        if (template.getEstimatedDuration() == null || template.getEstimatedDuration() <= 0) {
            missingFields.add("Duration");
        }
        if (template.getMinParticipants() == null || template.getMinParticipants() <= 0) {
            missingFields.add("Minimum Participants");
        }
        if (template.getMaxParticipants() == null || template.getMaxParticipants() <= 0) {
            missingFields.add("Maximum Participants");
        }

        // Check if at least one image exists
        if (template.getWorkshopImages() == null || template.getWorkshopImages().isEmpty()) {
            missingFields.add("Image (at least one required)");
        }

        // Check if at least one tag exists
        if (template.getWorkshopTags() == null || template.getWorkshopTags().isEmpty()) {
            missingFields.add("Category/Tag (at least one required)");
        }

        // If there are missing fields, throw exception with list
        if (!missingFields.isEmpty()) {
            String errorMessage = "Cannot submit incomplete template. Missing required fields: "
                + String.join(", ", missingFields);
            throw new BadRequestException(errorMessage);
        }
    }

    // ==================== APPROVE/REJECT (ADMIN ONLY) ====================

    @Override
    @Transactional
    public WorkshopTemplateResponse approveWorkshopTemplate(String adminEmail, UUID id) {
        // 1. Find the admin user
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", adminEmail));

        // 2. Find the workshop template
        WorkshopTemplate template = workshopTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkshopTemplate", "id", id));

        // 3. Check if template is in PENDING status
        if (template.getStatus() != WorkshopStatus.PENDING) {
            throw new BadRequestException("Only templates with 'Pending' status can be approved. Current status: " + template.getStatus());
        }

        // 4. Update status to ACTIVE and set approval details
        template.setStatus(WorkshopStatus.ACTIVE);
        template.setApprovedBy(admin.getId());
        template.setApprovedAt(LocalDateTime.now());
        template.setRejectReason(null); // Clear any previous rejection reason

        // 5. Save and return
        WorkshopTemplate approvedTemplate = workshopTemplateRepository.save(template);
        return mapToResponse(approvedTemplate);
    }

    @Override
    @Transactional
    public WorkshopTemplateResponse rejectWorkshopTemplate(String adminEmail, UUID id, String rejectReason) {
        // 1. Find the admin user
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", adminEmail));

        // 2. Find the workshop template
        WorkshopTemplate template = workshopTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkshopTemplate", "id", id));

        // 3. Check if template is in PENDING status
        if (template.getStatus() != WorkshopStatus.PENDING) {
            throw new BadRequestException("Only templates with 'Pending' status can be rejected. Current status: " + template.getStatus());
        }

        // 4. Validate reject reason
        if (rejectReason == null || rejectReason.trim().isEmpty()) {
            throw new BadRequestException("Reject reason is required");
        }

        // 5. Update status to REJECTED and set rejection details
        template.setStatus(WorkshopStatus.REJECTED);
        template.setRejectReason(rejectReason);
        template.setApprovedBy(null); // Clear approval details
        template.setApprovedAt(null);

        // 6. Save and return
        WorkshopTemplate rejectedTemplate = workshopTemplateRepository.save(template);
        return mapToResponse(rejectedTemplate);
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
                .rejectReason(template.getRejectReason())
                .approvedBy(template.getApprovedBy())
                .approvedAt(template.getApprovedAt())
                .images(imageResponses)
                .tags(tagResponses)
                .build();
    }
}
