package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.usercheckin.UserCheckinRequest;
import fpt.project.NeoNHS.dto.request.usercheckin.UpdateUserCheckinRequest;
import fpt.project.NeoNHS.dto.response.checkin.UserCheckinResultResponse;
import fpt.project.NeoNHS.dto.response.usercheckin.UserCheckinResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface UserCheckInService {
    UserCheckinResultResponse checkIn(UserCheckinRequest request, MultipartFile[] images);
    
    Page<UserCheckinResponse> getUserCheckins(int page, int size, String sortBy, String sortDir);
    UserCheckinResponse getUserCheckinById(UUID id);
    UserCheckinResponse updateUserCheckin(UUID id, UpdateUserCheckinRequest request);
    void deleteUserCheckin(UUID id);
    void deleteCheckinImage(UUID checkinId, UUID imageId);
}
