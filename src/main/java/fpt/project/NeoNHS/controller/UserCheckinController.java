package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.dto.request.usercheckin.UserCheckinRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.service.UserCheckInService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/check-in")
@RequiredArgsConstructor
@Tag(name = "User - User checkin", description = "User checkin go here")
public class UserCheckinController {

    private final UserCheckInService userCheckInService;

    @PostMapping()
    public ApiResponse<Void> checkIn(@RequestBody UserCheckinRequest request) {
        userCheckInService.checkIn(request);
        return ApiResponse.success("Check-in successful", null);
    }

}
