package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.request.workshop.CreateWorkshopSessionRequest;
import fpt.project.NeoNHS.dto.request.workshop.UpdateWorkshopSessionRequest;
import fpt.project.NeoNHS.dto.response.workshop.WTagResponse;
import fpt.project.NeoNHS.dto.response.workshop.WorkshopImageResponse;
import fpt.project.NeoNHS.dto.response.workshop.WorkshopSessionResponse;
import fpt.project.NeoNHS.entity.WorkshopSession;
import fpt.project.NeoNHS.entity.WorkshopTag;
import fpt.project.NeoNHS.entity.WorkshopTemplate;
import fpt.project.NeoNHS.enums.SessionStatus;
import fpt.project.NeoNHS.enums.WorkshopStatus;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.repository.VendorProfileRepository;
import fpt.project.NeoNHS.repository.WorkshopSessionRepository;
import fpt.project.NeoNHS.repository.WorkshopTemplateRepository;
import fpt.project.NeoNHS.service.WorkshopSessionService;
import fpt.project.NeoNHS.specification.WorkshopSessionSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public class WorkshopSessionServiceImpl implements WorkshopSessionService {

    private final WorkshopSessionRepository workshopSessionRepository;
    private final WorkshopTemplateRepository workshopTemplateRepository;
    private final VendorProfileRepository vendorProfileRepository;

    // ==================== CREATE ====================

    @Override
    @Transactional
    public WorkshopSessionResponse createWorkshopSession(String email, CreateWorkshopSessionRequest request) {
        // 1. Find and validate the workshop template
        WorkshopTemplate template = workshopTemplateRepository.findById(request.getWorkshopTemplateId())
                .orElseThrow(() -> new ResourceNotFoundException("WorkshopTemplate", "id", request.getWorkshopTemplateId()));

        // 2. Verify template is ACTIVE
        if (template.getStatus() != WorkshopStatus.ACTIVE) {
            throw new BadRequestException("Can only create sessions from ACTIVE templates. Current status: " + template.getStatus());
        }

        // 3. Verify ownership
        if (!template.getVendor().getUser().getEmail().equals(email)) {
            throw new BadRequestException("You do not have permission to create sessions for this workshop template");
        }

        // 4. Validate time constraints
        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Start time must be in the future");
        }
        if (request.getEndTime().isBefore(request.getStartTime()) || request.getEndTime().equals(request.getStartTime())) {
            throw new BadRequestException("End time must be after start time");
        }

        // 5. Set default values from template if not provided
        BigDecimal sessionPrice = request.getPrice() != null ? request.getPrice() : template.getDefaultPrice();
        Integer sessionMaxParticipants = request.getMaxParticipants() != null ? request.getMaxParticipants() : template.getMaxParticipants();

        // 6. Validate maxParticipants is >= template's minParticipants
        if (sessionMaxParticipants < template.getMinParticipants()) {
            throw new BadRequestException("Session max participants (" + sessionMaxParticipants + 
                    ") cannot be less than template's minimum participants (" + template.getMinParticipants() + ")");
        }

        // 7. Create WorkshopSession entity
        WorkshopSession session = WorkshopSession.builder()
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .price(sessionPrice)
                .maxParticipants(sessionMaxParticipants)
                .currentEnrolled(0)
                .status(SessionStatus.SCHEDULED)
                .workshopTemplate(template)
                .build();

        // 8. Save and return
        WorkshopSession savedSession = workshopSessionRepository.save(session);
        return mapToResponse(savedSession);
    }

    // ==================== READ ====================

    @Override
    public WorkshopSessionResponse getWorkshopSessionById(UUID id) {
        WorkshopSession session = workshopSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkshopSession", "id", id));
        return mapToResponse(session);
    }

    @Override
    public Page<WorkshopSessionResponse> getAllUpcomingSessions(Pageable pageable) {
        // Get all SCHEDULED sessions that start in the future
        Page<WorkshopSession> sessions = workshopSessionRepository.findByStatusAndStartTimeAfter(
                SessionStatus.SCHEDULED, 
                LocalDateTime.now(), 
                pageable
        );
        return sessions.map(this::mapToResponse);
    }

    @Override
    public Page<WorkshopSessionResponse> getMyWorkshopSessions(String email, Pageable pageable) {
        // Find vendor profile first to validate email
        vendorProfileRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("VendorProfile", "email", email));

        Page<WorkshopSession> sessions = workshopSessionRepository.findByWorkshopTemplateVendorUserEmail(email, pageable);
        return sessions.map(this::mapToResponse);
    }

    @Override
    public Page<WorkshopSessionResponse> getSessionsByTemplateId(UUID templateId, Pageable pageable) {
        // Validate template exists
        workshopTemplateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkshopTemplate", "id", templateId));

        Page<WorkshopSession> sessions = workshopSessionRepository.findByWorkshopTemplateId(templateId, pageable);
        return sessions.map(this::mapToResponse);
    }

    // ==================== SEARCH & FILTER ====================

    @Override
    public Page<WorkshopSessionResponse> searchWorkshopSessions(
            String keyword,
            UUID vendorId,
            UUID tagId,
            SessionStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean availableOnly,
            Pageable pageable
    ) {
        Specification<WorkshopSession> spec = Specification.where((root, query, cb) -> cb.conjunction());

        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and(WorkshopSessionSpecification.searchByKeyword(keyword));
        }
        if (vendorId != null) {
            spec = spec.and(WorkshopSessionSpecification.hasVendorId(vendorId));
        }
        if (tagId != null) {
            spec = spec.and(WorkshopSessionSpecification.hasTagId(tagId));
        }
        if (status != null) {
            spec = spec.and(WorkshopSessionSpecification.hasStatus(status));
        }
        if (startDate != null) {
            spec = spec.and(WorkshopSessionSpecification.hasStartTimeAfter(startDate));
        }
        if (endDate != null) {
            spec = spec.and(WorkshopSessionSpecification.hasStartTimeBefore(endDate));
        }
        if (minPrice != null || maxPrice != null) {
            spec = spec.and(WorkshopSessionSpecification.hasPriceBetween(minPrice, maxPrice));
        }
        if (availableOnly != null && availableOnly) {
            spec = spec.and(WorkshopSessionSpecification.hasAvailableSlots());
        }

        Page<WorkshopSession> sessions = workshopSessionRepository.findAll(spec, pageable);
        return sessions.map(this::mapToResponse);
    }

    // ==================== UPDATE ====================

    @Override
    @Transactional
    public WorkshopSessionResponse updateWorkshopSession(String email, UUID id, UpdateWorkshopSessionRequest request) {
        // 1. Find the workshop session
        WorkshopSession session = workshopSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkshopSession", "id", id));

        // 2. Verify ownership
        if (!session.getWorkshopTemplate().getVendor().getUser().getEmail().equals(email)) {
            throw new BadRequestException("You do not have permission to update this workshop session");
        }

        // 3. Only allow update if status is SCHEDULED
        if (session.getStatus() != SessionStatus.SCHEDULED) {
            throw new BadRequestException("Can only update SCHEDULED sessions. Current status: " + session.getStatus());
        }

        // 4. Update fields if provided
        if (request.getStartTime() != null) {
            if (request.getStartTime().isBefore(LocalDateTime.now())) {
                throw new BadRequestException("Start time must be in the future");
            }
            session.setStartTime(request.getStartTime());
        }

        if (request.getEndTime() != null) {
            session.setEndTime(request.getEndTime());
        }

        // 5. Validate time constraints
        if (session.getEndTime().isBefore(session.getStartTime()) || session.getEndTime().equals(session.getStartTime())) {
            throw new BadRequestException("End time must be after start time");
        }

        // 6. Update price if provided
        if (request.getPrice() != null) {
            session.setPrice(request.getPrice());
        }

        // 7. Update maxParticipants with validation
        if (request.getMaxParticipants() != null) {
            if (request.getMaxParticipants() < session.getCurrentEnrolled()) {
                throw new BadRequestException("Cannot reduce max participants (" + request.getMaxParticipants() + 
                        ") below current enrollments (" + session.getCurrentEnrolled() + ")");
            }
            if (request.getMaxParticipants() < session.getWorkshopTemplate().getMinParticipants()) {
                throw new BadRequestException("Max participants (" + request.getMaxParticipants() + 
                        ") cannot be less than template's minimum participants (" + 
                        session.getWorkshopTemplate().getMinParticipants() + ")");
            }
            session.setMaxParticipants(request.getMaxParticipants());
        }

        // 8. Save and return
        WorkshopSession updatedSession = workshopSessionRepository.save(session);
        return mapToResponse(updatedSession);
    }

    // ==================== DELETE ====================

    @Override
    @Transactional
    public void deleteWorkshopSession(String email, UUID id) {
        // 1. Find the workshop session
        WorkshopSession session = workshopSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkshopSession", "id", id));

        // 2. Verify ownership
        if (!session.getWorkshopTemplate().getVendor().getUser().getEmail().equals(email)) {
            throw new BadRequestException("You do not have permission to delete this workshop session");
        }

        // 3. Only allow delete if status is SCHEDULED and no enrollments
        if (session.getStatus() != SessionStatus.SCHEDULED) {
            throw new BadRequestException("Can only delete SCHEDULED sessions. Current status: " + session.getStatus());
        }

        if (session.getCurrentEnrolled() > 0) {
            throw new BadRequestException("Cannot delete a session with enrollments. Please cancel the session instead.");
        }

        // 4. Delete the session
        workshopSessionRepository.delete(session);
    }

    @Override
    @Transactional
    public WorkshopSessionResponse cancelWorkshopSession(String email, UUID id) {
        // 1. Find the workshop session
        WorkshopSession session = workshopSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkshopSession", "id", id));

        // 2. Verify ownership
        if (!session.getWorkshopTemplate().getVendor().getUser().getEmail().equals(email)) {
            throw new BadRequestException("You do not have permission to cancel this workshop session");
        }

        // 3. Only allow cancel if status is SCHEDULED
        if (session.getStatus() != SessionStatus.SCHEDULED) {
            throw new BadRequestException("Can only cancel SCHEDULED sessions. Current status: " + session.getStatus());
        }

        // 4. Update status to CANCELLED
        session.setStatus(SessionStatus.CANCELLED);

        // 5. Save and return
        WorkshopSession cancelledSession = workshopSessionRepository.save(session);
        return mapToResponse(cancelledSession);
    }

    // ==================== MAPPERS ====================

    private WorkshopSessionResponse mapToResponse(WorkshopSession session) {
        WorkshopTemplate template = session.getWorkshopTemplate();

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
        List<WTagResponse> tagResponses = new ArrayList<>();
        if (template.getWorkshopTags() != null) {
            tagResponses = template.getWorkshopTags().stream()
                    .map(WorkshopTag::getWTag)
                    .map(tag -> WTagResponse.builder()
                            .id(tag.getId())
                            .name(tag.getName())
                            .description(tag.getDescription())
                            .tagColor(tag.getTagColor())
                            .iconUrl(tag.getIconUrl())
                            .build())
                    .collect(Collectors.toList());
        }

        // Calculate available slots
        Integer availableSlots = session.getMaxParticipants() - session.getCurrentEnrolled();

        return WorkshopSessionResponse.builder()
                // Session-specific fields
                .id(session.getId())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .price(session.getPrice())
                .maxParticipants(session.getMaxParticipants())
                .currentEnrolled(session.getCurrentEnrolled())
                .availableSlots(availableSlots)
                .status(session.getStatus())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                // Template information
                .workshopTemplateId(template.getId())
                .name(template.getName())
                .shortDescription(template.getShortDescription())
                .fullDescription(template.getFullDescription())
                .estimatedDuration(template.getEstimatedDuration())
                .averageRating(template.getAverageRating())
                .totalReview(template.getTotalReview())
                // Vendor information
                .vendorId(template.getVendor().getId())
                .vendorName(template.getVendor().getBusinessName())
                // Related entities
                .images(imageResponses)
                .tags(tagResponses)
                .build();
    }
}
