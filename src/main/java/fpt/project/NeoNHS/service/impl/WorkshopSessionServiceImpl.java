package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.constants.NotificationMessages;
import fpt.project.NeoNHS.dto.request.workshop.CreateWorkshopSessionRequest;
import fpt.project.NeoNHS.dto.request.workshop.UpdateWorkshopSessionRequest;
import fpt.project.NeoNHS.dto.response.workshop.WTagResponse;
import fpt.project.NeoNHS.dto.response.workshop.WorkshopImageResponse;
import fpt.project.NeoNHS.dto.response.workshop.WorkshopSessionResponse;
import fpt.project.NeoNHS.entity.Order;
import fpt.project.NeoNHS.entity.OrderDetail;
import fpt.project.NeoNHS.entity.User;
import fpt.project.NeoNHS.entity.WorkshopSession;
import fpt.project.NeoNHS.entity.WorkshopTag;
import fpt.project.NeoNHS.entity.WorkshopTemplate;
import fpt.project.NeoNHS.enums.SessionStatus;
import fpt.project.NeoNHS.enums.TransactionStatus;
import fpt.project.NeoNHS.enums.WorkshopStatus;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.repository.OrderDetailRepository;
import fpt.project.NeoNHS.repository.OrderRepository;
import fpt.project.NeoNHS.repository.UserRepository;
import fpt.project.NeoNHS.repository.VendorProfileRepository;
import fpt.project.NeoNHS.repository.WorkshopSessionRepository;
import fpt.project.NeoNHS.repository.WorkshopTemplateRepository;
import fpt.project.NeoNHS.service.NotificationService;
import fpt.project.NeoNHS.service.WorkshopSessionService;
import fpt.project.NeoNHS.specification.WorkshopSessionSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkshopSessionServiceImpl implements WorkshopSessionService {

    private final WorkshopSessionRepository workshopSessionRepository;
    private final WorkshopTemplateRepository workshopTemplateRepository;
    private final VendorProfileRepository vendorProfileRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

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

    @Override
    @Transactional
    public List<WorkshopSessionResponse> createWorkshopSessionBatch(String email, List<CreateWorkshopSessionRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new BadRequestException("Session requests cannot be empty");
        }

        // Tối ưu hóa: Lấy workshopTemplateId từ request đầu tiên
        // (Giả định tất cả các phiên được tạo cùng lúc đều thuộc về 1 template)
        UUID templateId = requests.get(0).getWorkshopTemplateId();

        // 1. Tìm và xác thực template chỉ 1 lần duy nhất cho toàn bộ batch
        WorkshopTemplate template = workshopTemplateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkshopTemplate", "id", templateId));

        // 2 & 3. Kiểm tra trạng thái và quyền sở hữu (chỉ cần làm 1 lần)
        if (template.getStatus() != WorkshopStatus.ACTIVE) {
            throw new BadRequestException("Can only create sessions from ACTIVE templates. Current status: " + template.getStatus());
        }
        if (!template.getVendor().getUser().getEmail().equals(email)) {
            throw new BadRequestException("You do not have permission to create sessions for this workshop template");
        }

        // Khởi tạo danh sách các Entity cần lưu
        List<WorkshopSession> sessionsToSave = new ArrayList<>();

        // 4. Lặp qua từng request để validate thời gian và build Entity
        for (CreateWorkshopSessionRequest request : requests) {
            // Đảm bảo request không truyền sai templateId trong cùng 1 batch
            if (!request.getWorkshopTemplateId().equals(templateId)) {
                throw new BadRequestException("All sessions in a batch must belong to the same workshop template");
            }

            // Validate thời gian cho từng phiên
            if (request.getStartTime().isBefore(LocalDateTime.now())) {
                throw new BadRequestException("Start time must be in the future");
            }
            if (request.getEndTime().isBefore(request.getStartTime()) || request.getEndTime().equals(request.getStartTime())) {
                throw new BadRequestException("End time must be after start time");
            }

            // Set giá trị mặc định nếu null
            BigDecimal sessionPrice = request.getPrice() != null ? request.getPrice() : template.getDefaultPrice();
            Integer sessionMaxParticipants = request.getMaxParticipants() != null ? request.getMaxParticipants() : template.getMaxParticipants();

            if (sessionMaxParticipants < template.getMinParticipants()) {
                throw new BadRequestException("Session max participants (" + sessionMaxParticipants +
                        ") cannot be less than template's minimum participants (" + template.getMinParticipants() + ")");
            }

            // Build Entity
            WorkshopSession session = WorkshopSession.builder()
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .price(sessionPrice)
                    .maxParticipants(sessionMaxParticipants)
                    .currentEnrolled(0)
                    .status(SessionStatus.SCHEDULED)
                    .workshopTemplate(template)
                    .build();

            sessionsToSave.add(session);
        }

        // 5. Lưu toàn bộ danh sách vào database bằng saveAll
        List<WorkshopSession> savedSessions = workshopSessionRepository.saveAll(sessionsToSave);

        // 6. Map kết quả trả về
        return savedSessions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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
        // Get all SCHEDULED sessions that start in the future, only from published templates
        Specification<WorkshopSession> spec = Specification
                .where(WorkshopSessionSpecification.hasStatus(SessionStatus.SCHEDULED))
                .and(WorkshopSessionSpecification.hasStartTimeAfter(LocalDateTime.now()))
                .and(WorkshopSessionSpecification.hasPublishedTemplate());
        Page<WorkshopSession> sessions = workshopSessionRepository.findAll(spec, pageable);
        return sessions.map(this::mapToResponse);
    }

    @Override
    public Page<WorkshopSessionResponse> getMyWorkshopSessions(String email, Pageable pageable) {
        // Find vendor profile first to validate email
        vendorProfileRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("VendorProfile", "email", email));

        Page<WorkshopSession> sessions = workshopSessionRepository.findByWorkshopTemplateVendorUserEmail(email,
                pageable);
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

    @Override
    public Page<WorkshopSessionResponse> getUpcomingSessionsByTemplateId(UUID templateId, Pageable pageable) {
        WorkshopTemplate template = workshopTemplateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkshopTemplate", "id", templateId));
        // C1: Direct link accessible — only check ACTIVE status, NOT isPublished
        if (template.getStatus() != WorkshopStatus.ACTIVE) {
            throw new ResourceNotFoundException("WorkshopTemplate", "id", templateId);
        }
        Specification<WorkshopSession> spec = Specification
                .where(WorkshopSessionSpecification.hasTemplateId(templateId))
                .and(WorkshopSessionSpecification.hasStatus(SessionStatus.SCHEDULED))
                .and(WorkshopSessionSpecification.hasStartTimeAfter(LocalDateTime.now()));
        return workshopSessionRepository.findAll(spec, pageable).map(this::mapToResponse);
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
        // Only show sessions from published templates in public search
        Specification<WorkshopSession> spec = Specification.where(WorkshopSessionSpecification.hasPublishedTemplate());

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

        // 4. KIỂM TRA ĐĂNG KÝ: Chặn mọi update nếu đã có người đăng ký
        if (session.getCurrentEnrolled() > 0) {
            throw new BadRequestException("Cannot update this session because tourists have already registered");
        }

        // 5. Update fields if provided
        if (request.getStartTime() != null) {
            if (request.getStartTime().isBefore(LocalDateTime.now())) {
                throw new BadRequestException("Start time must be in the future");
            }
            session.setStartTime(request.getStartTime());
        }

        if (request.getEndTime() != null) {
            session.setEndTime(request.getEndTime());
        }

        // 6. Validate time constraints
        if (session.getEndTime() != null && session.getStartTime() != null) {
            if (session.getEndTime().isBefore(session.getStartTime())
                    || session.getEndTime().equals(session.getStartTime())) {
                throw new BadRequestException("End time must be after start time");
            }
        }

        // 7. Update price if provided (Đã xóa logic check enrolled)
        if (request.getPrice() != null) {
            session.setPrice(request.getPrice());
        }

        // 8. Update maxParticipants with validation (Đã xóa logic check enrolled)
        if (request.getMaxParticipants() != null) {
            if (request.getMaxParticipants() < session.getWorkshopTemplate().getMinParticipants()) {
                throw new BadRequestException("Max participants (" + request.getMaxParticipants() +
                        ") cannot be less than template's minimum participants (" +
                        session.getWorkshopTemplate().getMinParticipants() + ")");
            }
            session.setMaxParticipants(request.getMaxParticipants());
        }

        // 9. Save and return
        WorkshopSession updatedSession = workshopSessionRepository.save(session);
        return mapToResponse(updatedSession);
    }

    @Override
    @Transactional
    public WorkshopSessionResponse updateWorkshopSessionStatus(String email, UUID id, SessionStatus status) {
        WorkshopSession session = workshopSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkshopSession", "id", id));

        if (!session.getWorkshopTemplate().getVendor().getUser().getEmail().equals(email)) {
            throw new BadRequestException("You do not have permission to update this workshop session");
        }
        // 1. Validate status update
        if (status == SessionStatus.ONGOING) {
            if (session.getStatus() != SessionStatus.SCHEDULED) {
                throw new BadRequestException("Can only update status to ONGOING if current status is SCHEDULED. Current status: " + session.getStatus());
            }
            if (session.getCurrentEnrolled() == 0) {
                throw new BadRequestException("Cannot start the session because no tourists are registered.");
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime minAllowableStart = session.getStartTime().minusMinutes(30);
            LocalDateTime maxAllowableStart = session.getStartTime().plusMinutes(30);

            if (now.isBefore(minAllowableStart) || now.isAfter(maxAllowableStart)) {
                throw new BadRequestException("You can only change status to ONGOING within 30 minutes of the start time.");
            }

        } else if (status == SessionStatus.COMPLETED) {
            if (session.getStatus() != SessionStatus.ONGOING) {
                throw new BadRequestException("Can only update status to COMPLETED if current status is ONGOING. Current status: " + session.getStatus());
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime minAllowableEnd = session.getEndTime().minusMinutes(30);
            LocalDateTime maxAllowableEnd = session.getEndTime().plusMinutes(30);

            if (now.isBefore(minAllowableEnd) || now.isAfter(maxAllowableEnd)) {
                throw new BadRequestException("You can only change status to COMPLETED within 30 minutes of the end time.");
            }

        } else {
            throw new BadRequestException("Invalid status update. Allowed updates are ONGOING or COMPLETED.");
        }
        

        // 2. Update status
        session.setStatus(status);
        WorkshopSession updatedSession = workshopSessionRepository.save(session);

        // 3. Nếu status = COMPLETED → cộng netAmount vào wallet của vendor
        if (status == SessionStatus.COMPLETED) {
            creditVendorWallet(updatedSession);
        }

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
            throw new BadRequestException(
                    "Cannot delete a session with enrollments. Please cancel the session instead.");
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
        WorkshopSession cancelledSession = workshopSessionRepository.save(session);

        // 5. Hoàn tiền vào balance cho tất cả user đã thanh toán thành công
        List<Order> paidOrders = orderRepository
                .findOrdersByWorkshopSessionIdAndTxStatus(id, TransactionStatus.SUCCESS);

        for (Order order : paidOrders) {
            User buyer = order.getUser();
            double refund = order.getFinalAmount().doubleValue();
            double current = buyer.getBalance() != null ? buyer.getBalance() : 0.0;
            buyer.setBalance(current + refund);
            userRepository.save(buyer);
            log.info("[Cancel Refund] Refunded {} VND to {} (orderId={})",
                    refund, buyer.getEmail(), order.getId());
        }

        // 6. Return
        return mapToResponse(cancelledSession);
    }

    // ==================== HELPERS ====================

    /**
     * Cộng tổng netAmount của tất cả OrderDetail đã thanh toán (Transaction SUCCESS)
     * vào wallet (balance) của vendor sở hữu session.
     *
     * Tái sử dụng được bởi:
     *   - updateWorkshopSessionStatus() khi vendor/admin bấm COMPLETED thủ công
     *   - Scheduler auto-complete khi endTime đã qua
     */
    public void creditVendorWallet(WorkshopSession session) {
        List<OrderDetail> paidDetails =
                orderDetailRepository.findPaidDetailsByWorkshopSessionId(session.getId());

        BigDecimal totalNet = paidDetails.stream()
                .filter(od -> od.getNetAmount() != null)
                .map(OrderDetail::getNetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalNet.compareTo(BigDecimal.ZERO) > 0) {
            // Chain: session → template → VendorProfile → User.balance
            User vendorUser = session.getWorkshopTemplate().getVendor().getUser();
            double currentBalance = vendorUser.getBalance() != null ? vendorUser.getBalance() : 0.0;
            vendorUser.setBalance(currentBalance + totalNet.doubleValue());
            userRepository.save(vendorUser);
            log.info("[Session COMPLETED] Credited {} VND to vendor {} (sessionId={})",
                    totalNet, vendorUser.getEmail(), session.getId());

            // --- NOTIFICATION TRIGGER ---
            try {
                notificationService.createAndSendNotification(
                        vendorUser,
                        NotificationMessages.walletPayoutTitle(),
                        NotificationMessages.walletPayoutMessage(totalNet, session.getWorkshopTemplate().getName()),
                        NotificationMessages.TYPE_WALLET_PAYOUT,
                        session.getId());
            } catch (Exception e) {
                log.error("[Session COMPLETED] Failed to send notification to vendor {}: {}",
                        vendorUser.getEmail(), e.getMessage());
            }
        } else {
            if (paidDetails.isEmpty()) {
                log.info("[Session COMPLETED] No paid orders found for session {}, nothing credited.",
                        session.getId());
            } else {
                log.warn(
                        "[Session COMPLETED] Found {} paid orders for session {}, but total netAmount is zero/null. Check commission settings.",
                        paidDetails.size(), session.getId());
            }
        }
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
                .totalRatings(template.getTotalRatings())
                // Vendor information
                .vendorId(template.getVendor().getId())
                .vendorName(template.getVendor().getBusinessName())
                // Related entities
                .images(imageResponses)
                .tags(tagResponses)
                .build();
    }
}
