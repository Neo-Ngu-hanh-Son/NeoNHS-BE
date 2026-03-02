package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.request.admin.ReportFilterRequest;
import fpt.project.NeoNHS.dto.request.admin.ResolveReportRequest;
import fpt.project.NeoNHS.dto.request.report.CreateReportRequest;
import fpt.project.NeoNHS.dto.response.report.ReportResponse;
import fpt.project.NeoNHS.entity.Report;
import fpt.project.NeoNHS.entity.User;
import fpt.project.NeoNHS.enums.ReportStatus;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.repository.ReportRepository;
import fpt.project.NeoNHS.repository.UserRepository;
import fpt.project.NeoNHS.repository.EventRepository;
import fpt.project.NeoNHS.repository.PointRepository;
import fpt.project.NeoNHS.repository.WorkshopTemplateRepository;
import fpt.project.NeoNHS.service.ReportService;
import fpt.project.NeoNHS.specification.ReportSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final PointRepository pointRepository;
    private final WorkshopTemplateRepository workshopTemplateRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ReportResponse> getAllReports(ReportFilterRequest filter, Pageable pageable) {
        return reportRepository.findAll(ReportSpecification.withFilters(filter), pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResponse getReportById(UUID id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + id));
        return mapToResponse(report);
    }

    @Override
    @Transactional
    public ReportResponse resolveReport(UUID id, ResolveReportRequest request, UUID adminId) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + id));

        if (report.getStatus() != ReportStatus.PENDING) {
            throw new BadRequestException("Report is already " + report.getStatus());
        }

        report.setStatus(request.getStatus());
        report.setHandleNote(request.getHandleNote());
        report.setHandlerId(adminId.toString());
        report.setResolvedAt(LocalDateTime.now());

        return mapToResponse(reportRepository.save(report));
    }

    private ReportResponse mapToResponse(Report report) {
        ReportResponse response = ReportResponse.fromEntity(report);

        String targetName = fetchTargetName(report.getTargetType(), report.getTargetId());
        response.setTargetName(targetName);

        if (report.getHandlerId() != null) {
            try {
                userRepository.findById(UUID.fromString(report.getHandlerId()))
                        .ifPresent(admin -> response.setHandlerName(admin.getFullname()));
            } catch (IllegalArgumentException e) {
                response.setHandlerName("System");
            }
        }

        return response;
    }

    private String fetchTargetName(String targetType, UUID targetId) {
        if (targetType == null || targetId == null) return "Unknown";

        return switch (targetType.toUpperCase()) {
            case "POINT" -> pointRepository.findById(targetId)
                    .map(p -> p.getName())
                    .orElse("Point (Deleted)");

            case "EVENT" -> eventRepository.findById(targetId)
                    .map(e -> e.getName())
                    .orElse("Event (Deleted)");

            case "WORKSHOP" -> workshopTemplateRepository.findById(targetId)
                    .map(w -> w.getName())
                    .orElse("Workshop (Deleted)");

            case "USER" -> userRepository.findById(targetId)
                    .map(u -> u.getFullname())
                    .orElse("User (Not Found)");

            default -> "Type: " + targetType + " (ID: " + targetId + ")";
        };
    }

    @Override
    @Transactional
    public ReportResponse createReport(CreateReportRequest request, UUID reporterId) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        validateTargetExists(request.getTargetType(), request.getTargetId());

        Report report = Report.builder()
                .targetId(request.getTargetId())
                .targetType(request.getTargetType().toUpperCase())
                .reason(request.getReason())
                .description(request.getDescription())
                .evidenceUrl(request.getEvidenceUrl())
                .reporter(reporter)
                .status(ReportStatus.PENDING)
                .build();

        Report savedReport = reportRepository.save(report);

        return mapToResponse(savedReport);
    }

    private void validateTargetExists(String type, UUID id) {
        boolean exists = switch (type.toUpperCase()) {
            case "EVENT" -> eventRepository.existsById(id);
            case "POINT" -> pointRepository.existsById(id);
            case "WORKSHOP" -> workshopTemplateRepository.existsById(id);
            case "USER" -> userRepository.existsById(id);
            default -> throw new BadRequestException("Invalid target type: " + type);
        };

        if (!exists) {
            throw new ResourceNotFoundException(type + " with id " + id + " does not exist");
        }
    }
}