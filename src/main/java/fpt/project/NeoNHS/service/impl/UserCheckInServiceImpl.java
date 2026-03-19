package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.constants.GeoConstants;
import fpt.project.NeoNHS.dto.request.usercheckin.CheckinImageRequest;
import fpt.project.NeoNHS.dto.request.usercheckin.UpdateCheckinImageCaptionRequest;
import fpt.project.NeoNHS.dto.request.usercheckin.UpdateUserCheckinRequest;
import fpt.project.NeoNHS.dto.request.usercheckin.UserCheckinRequest;
import fpt.project.NeoNHS.dto.response.checkin.UserCheckinResultResponse;
import fpt.project.NeoNHS.dto.response.usercheckin.CheckinImageResponse;
import fpt.project.NeoNHS.dto.response.usercheckin.UserCheckinGalleryItemResponse;
import fpt.project.NeoNHS.dto.response.usercheckin.UserCheckinGalleryListResponse;
import fpt.project.NeoNHS.dto.response.usercheckin.UserCheckinResponse;
import fpt.project.NeoNHS.entity.*;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.exception.InvalidCheckinAttempt;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.repository.CheckinImageRepository;
import fpt.project.NeoNHS.repository.CheckinPointRepository;
import fpt.project.NeoNHS.repository.UserCheckInRepository;
import fpt.project.NeoNHS.repository.UserRepository;
import fpt.project.NeoNHS.service.GeoService;
import fpt.project.NeoNHS.service.ImageUploadService;
import fpt.project.NeoNHS.service.UserCheckInService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fpt.project.NeoNHS.helpers.AuthHelper.getCurrentUserPrincipal;

@Service
@RequiredArgsConstructor
public class UserCheckInServiceImpl implements UserCheckInService {

    private final UserCheckInRepository userCheckInRepository;
    private final CheckinImageRepository checkinImageRepository;
    private final CheckinPointRepository checkinPointRepository;
    private final GeoService geoService;
    private final UserRepository userRepository;
    private final ImageUploadService imageUploadService;

    @Override
    @Transactional
    public UserCheckinResultResponse checkIn(UserCheckinRequest request) {
        var checkinPoint = checkinPointRepository.findById(UUID.fromString(request.getCheckinPointId()))
                .orElseThrow(() -> new ResourceNotFoundException("Check-in point not found"));
        var user = userRepository.findById(getCurrentUserPrincipal().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found, are you sure you are authenticated?"));

        // If user already checked in, add the checkin image into the current check in. (Point not increase)
        if (user.getCheckIns() != null) {
            addImageToExistingCheckin(request, checkinPoint, user);
        }

        // Calculate the distance between the user's location and the check-in point
        double distance = geoService.calculateDistanceManually(request.getLatitude(), request.getLongitude(),
                checkinPoint.getLatitude().doubleValue(), checkinPoint.getLongitude().doubleValue());

        if (distance > GeoConstants.MAX_CHECKIN_RADIUS_METERS) {
            throw new InvalidCheckinAttempt("You are too far from the check-in point to check in");
        }

        var userCheckIn = UserCheckIn.builder()
                .note(request.getNote())
                .checkinMethod(request.getMethod().name())
                .earnedPoints(checkinPoint.getRewardPoints())
                .checkinPoint(checkinPoint)
                .user(user)
                .checkinTime(LocalDateTime.now())
                .build();

        if (request.getCheckinImageRequest() == null || request.getCheckinImageRequest().isEmpty()) {
            throw new InvalidCheckinAttempt("You must provide at least one image for check-in");
        }

        var checkInImageRequests = request.getCheckinImageRequest();
        List<CheckinImage> checkinImages = new ArrayList<>();
        for (var imgReq : checkInImageRequests) {
            checkinImages.add(
                    CheckinImage.builder()
                            .imageUrl(imgReq.getImageUrl())
                            .userCheckIn(userCheckIn)
                            .caption(imgReq.getCaption())
                            .build());
        }

        userCheckIn.setCheckinImages(checkinImages);
        userCheckInRepository.save(userCheckIn);

        int userTotalPoint = user.getCheckIns().stream()
                .reduce(0, (sum, checkIn) -> sum + checkIn.getEarnedPoints(),
                        (first, second) -> first + second);
        userTotalPoint += checkinPoint.getRewardPoints();
        return UserCheckinResultResponse.builder()
                .earnedPoints(userCheckIn.getEarnedPoints())
                .userTotalPoints(userTotalPoint)
                .build();
    }

    // TODO: Allow user to handle CRUD on their own ?
    private void addImageToExistingCheckin(UserCheckinRequest request, CheckinPoint checkinPoint, User user) {
        checkinPoint.getUserCheckIns().stream().filter(checkIn -> checkIn.getUser().getId().equals(user.getId()))
                .findFirst().ifPresent(checkIn -> {
                    List<CheckinImage> existingImages = checkIn.getCheckinImages();
                    List<CheckinImageRequest> newImages = request.getCheckinImageRequest();
                    for (var newImg : newImages) {
                        existingImages.add(CheckinImage.builder()
                                .id(UUID.randomUUID())
                                .imageUrl(newImg.getImageUrl())
                                .userCheckIn(checkIn)
                                .caption(newImg.getCaption())
                                .build());
                    }
                    userCheckInRepository.save(checkIn);
                });
    }


    @Override
    public Page<UserCheckinResponse> getUserCheckins(int page, int size, String sortBy, String sortDir) {
        UUID currentUserId = getCurrentUserPrincipal().getId();
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), sort);

        return userCheckInRepository.findAllByUser_Id(currentUserId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public UserCheckinResponse getUserCheckinById(UUID id) {
        UUID currentUserId = getCurrentUserPrincipal().getId();
        UserCheckIn checkIn = userCheckInRepository.findByIdAndUser_Id(id, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Checkin not found or you don't have permission to view it."));
        return mapToResponse(checkIn);
    }

    @Override
    @Transactional
    public UserCheckinResponse updateUserCheckin(UUID id, UpdateUserCheckinRequest request) {
        UUID currentUserId = getCurrentUserPrincipal().getId();
        UserCheckIn checkIn = userCheckInRepository.findByIdAndUser_Id(id, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Checkin not found or you don't have permission to update it."));

        if (request.getNote() != null) {
            checkIn.setNote(request.getNote());
        }

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            // Create a lookup map for faster processing
            java.util.Map<UUID, String> updateMap = request.getImages().stream()
                    .filter(img -> img.getImageId() != null)
                    .collect(java.util.stream.Collectors.toMap(
                            UpdateCheckinImageCaptionRequest::getImageId,
                            UpdateCheckinImageCaptionRequest::getCaption,
                            (existing, replacement) -> replacement
                    ));

            for (CheckinImage image : checkIn.getCheckinImages()) {
                if (updateMap.containsKey(image.getId())) {
                    image.setCaption(updateMap.get(image.getId()));
                }
            }
        }

        userCheckInRepository.save(checkIn);
        return mapToResponse(checkIn);
    }

    @Override
    @Transactional
    public void deleteUserCheckin(UUID id) {
        UUID currentUserId = getCurrentUserPrincipal().getId();
        UserCheckIn checkIn = userCheckInRepository.findByIdAndUser_Id(id, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Checkin not found or you don't have permission to delete it."));

        // This is a hard delete as UserCheckIn does not extend BaseEntity, cascading its CheckinImages.
        userCheckInRepository.delete(checkIn);
    }

    @Override
    @Transactional
    public void deleteCheckinImage(UUID checkinId, UUID imageId) {
        UUID currentUserId = getCurrentUserPrincipal().getId();
        UserCheckIn checkIn = userCheckInRepository.findByIdAndUser_Id(checkinId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Checkin not found or you don't have permission to update it."));

        if (checkIn.getCheckinImages().size() <= 1) {
            throw new BadRequestException("Cannot delete the only image of a check-in. If you want to remove the image, please delete the entire check-in instead.");
        }

        boolean removed = checkIn.getCheckinImages().removeIf(img -> img.getId().equals(imageId));
        if (!removed) {
            throw new ResourceNotFoundException("Image with id " + imageId + " not found in this check-in.");
        }

        // Saving the checkIn will cascade the orphanRemoval and delete the CheckinImage entity
        userCheckInRepository.save(checkIn);
    }

    @Override
    public UserCheckinGalleryListResponse getMyGallery(LocalDateTime from, LocalDateTime to,
                                                       UUID parentPointId, UUID checkinPointId) {
        UUID currentUserId = getCurrentUserPrincipal().getId();

        List<CheckinImage> images = checkinImageRepository
                .findAllByUserCheckIn_User_IdOrderByCreatedAtDesc(currentUserId);

        // Apply optional filters via stream
        Stream<CheckinImage> stream = images.stream();

        if (from != null) {
            stream = stream.filter(img -> !img.getCreatedAt().isBefore(from));
        }
        if (to != null) {
            stream = stream.filter(img -> !img.getCreatedAt().isAfter(to));
        }
        if (parentPointId != null) {
            stream = stream.filter(img ->
                    parentPointId.equals(img.getUserCheckIn().getCheckinPoint().getPoint().getId()));
        }
        if (checkinPointId != null) {
            stream = stream.filter(img ->
                    checkinPointId.equals(img.getUserCheckIn().getCheckinPoint().getId()));
        }

        List<UserCheckinGalleryItemResponse> items = stream
                .map(this::mapToGalleryItem)
                .collect(Collectors.toList());

        return UserCheckinGalleryListResponse.builder()
                .items(items)
                .build();
    }

    private UserCheckinGalleryItemResponse mapToGalleryItem(CheckinImage image) {
        CheckinPoint checkinPoint = image.getUserCheckIn().getCheckinPoint();
        Point point = checkinPoint.getPoint();
        Attraction attraction = point.getAttraction();

        return UserCheckinGalleryItemResponse.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .caption(image.getCaption())
                .takenAt(image.getCreatedAt())
                .checkinPointId(checkinPoint.getId())
                .checkinPointName(checkinPoint.getName())
                .parentPointId(point.getId())
                .parentPointName(point.getName())
                .destinationId(attraction.getId())
                .destinationName(attraction.getName())
                .build();
    }

    private UserCheckinResponse mapToResponse(UserCheckIn entity) {
        List<CheckinImageResponse> imageResponses = entity.getCheckinImages().stream()
                .map(img -> CheckinImageResponse.builder()
                        .id(img.getId())
                        .imageUrl(img.getImageUrl())
                        .caption(img.getCaption())
                        .build())
                .collect(Collectors.toList());

        return UserCheckinResponse.builder()
                .id(entity.getId())
                .checkinTime(entity.getCheckinTime())
                .checkinMethod(entity.getCheckinMethod())
                .note(entity.getNote())
                .earnedPoints(entity.getEarnedPoints())
                .checkinPointId(entity.getCheckinPoint().getId())
                .images(imageResponses)
                .build();
    }
}
