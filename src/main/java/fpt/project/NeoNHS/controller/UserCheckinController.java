package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.constants.PaginationConstants;
import fpt.project.NeoNHS.dto.request.usercheckin.UpdateUserCheckinRequest;
import fpt.project.NeoNHS.dto.request.usercheckin.UserCheckinRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.checkin.UserCheckinResultResponse;
import fpt.project.NeoNHS.dto.response.usercheckin.UserCheckinGalleryListResponse;
import fpt.project.NeoNHS.dto.response.usercheckin.UserCheckinResponse;
import fpt.project.NeoNHS.service.UserCheckInService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/check-ins")
@RequiredArgsConstructor
@Tag(name = "User - User checkin", description = "User checkin go here")
public class UserCheckinController {

    private final UserCheckInService userCheckInService;

    @GetMapping("/images")
    @Operation(summary = "Get all check-in images for the current user (gallery)")
    public ApiResponse<UserCheckinGalleryListResponse> getMyGallery(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) UUID parentPointId,
            @RequestParam(required = false) UUID checkinPointId) {
        UserCheckinGalleryListResponse data = userCheckInService.getMyGallery(from, to, parentPointId, checkinPointId);
        return ApiResponse.success(data);
    }

    @GetMapping
    public ApiResponse<Page<UserCheckinResponse>> getUserCheckins(
            @RequestParam(value = "page", defaultValue = PaginationConstants.DEFAULT_PAGE) int page,
            @RequestParam(value = "size", defaultValue = PaginationConstants.DEFAULT_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = "checkinTime") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = PaginationConstants.SORT_DESC) String sortDir) {
        Page<UserCheckinResponse> data = userCheckInService.getUserCheckins(page, size, sortBy, sortDir);
        return ApiResponse.success(data);
    }

    @GetMapping("/{id}")
    public ApiResponse<UserCheckinResponse> getUserCheckinById(@PathVariable UUID id) {
        UserCheckinResponse data = userCheckInService.getUserCheckinById(id);
        return ApiResponse.success(data);
    }

    @PostMapping
    public ApiResponse<UserCheckinResultResponse> checkIn(@RequestBody UserCheckinRequest request) {
        var res = userCheckInService.checkIn(request);
        return ApiResponse.success("Check-in successful", res);
    }

    @PutMapping("/{id}")
    public ApiResponse<UserCheckinResponse> updateUserCheckin(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserCheckinRequest request) {
        UserCheckinResponse data = userCheckInService.updateUserCheckin(id, request);
        return ApiResponse.success("Check-in updated successfully", data);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUserCheckin(@PathVariable UUID id) {
        userCheckInService.deleteUserCheckin(id);
        return ApiResponse.success("Check-in deleted successfully", null);
    }

    @DeleteMapping("/{checkinId}/images/{imageId}")
    public ApiResponse<Void> deleteCheckinImage(
            @PathVariable UUID checkinId,
            @PathVariable UUID imageId) {
        userCheckInService.deleteCheckinImage(checkinId, imageId);
        return ApiResponse.success("Check-in image deleted successfully", null);
    }

}
