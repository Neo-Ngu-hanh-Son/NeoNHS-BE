package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.dto.request.UpdateUserProfileRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.auth.UserProfileResponse;
import fpt.project.NeoNHS.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Get Tourist Information
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(Principal principal) {
        UserProfileResponse data = userService.getMyProfile(principal.getName());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Profile retrieved successfully", data));
    }

    @PutMapping("/update-profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateBasicProfile(
            Principal principal, @RequestBody UpdateUserProfileRequest request) {
        UserProfileResponse data = userService.updateProfile(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Basic info updated", data));
    }
}
