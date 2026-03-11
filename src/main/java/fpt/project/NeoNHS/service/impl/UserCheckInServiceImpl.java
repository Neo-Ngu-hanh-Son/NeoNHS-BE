package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.constants.GeoConstants;
import fpt.project.NeoNHS.dto.request.usercheckin.UserCheckinRequest;
import fpt.project.NeoNHS.entity.CheckinImage;
import fpt.project.NeoNHS.entity.UserCheckIn;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.exception.InvalidCheckinAttempt;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.exception.UnauthorizedException;
import fpt.project.NeoNHS.repository.CheckinPointRepository;
import fpt.project.NeoNHS.repository.UserCheckInRepository;
import fpt.project.NeoNHS.repository.UserRepository;
import fpt.project.NeoNHS.security.UserPrincipal;
import fpt.project.NeoNHS.service.GeoService;
import fpt.project.NeoNHS.service.UserCheckInService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserCheckInServiceImpl implements UserCheckInService {

    private final UserCheckInRepository userCheckInRepository;
    private final CheckinPointRepository checkinPointRepository;
    private final GeoService geoService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void checkIn(UserCheckinRequest request) {
        var checkinPoint = checkinPointRepository.findById(UUID.fromString(request.getCheckinPointId()))
                .orElseThrow(() -> new ResourceNotFoundException("Check-in point not found"));
        var user = userRepository.findById(getCurrentUserPrincipal().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found, are you sure you are authenticated?"));

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
        // Create checkin images
        if (request.getImages() == null || request.getImages().isEmpty()) {
            throw new BadRequestException("You must provide at least one image for check-in");
        }
        List<CheckinImage> checkinImages = request.getImages().stream()
                .map(imgReq -> CheckinImage.builder()
                        .id(UUID.randomUUID())
                        .imageUrl(imgReq.getImageUrl())
                        .caption(imgReq.getCaption())
                        .userCheckIn(userCheckIn)
                        .build()).collect(Collectors.toList());
        userCheckIn.setCheckinImages(checkinImages);
        userCheckInRepository.save(userCheckIn);
    }

    private UserPrincipal getCurrentUserPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated");
        }
        if (!(auth.getPrincipal() instanceof UserPrincipal userPrincipal)) {
            throw new UnauthorizedException("Invalid authenticated principal");
        }
        return userPrincipal;
    }
}
