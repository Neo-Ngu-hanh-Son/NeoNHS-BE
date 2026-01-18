package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.service.CheckinPointService;
import fpt.project.NeoNHS.service.UserCheckInService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checkins")
@RequiredArgsConstructor
public class CheckinController {

    private final CheckinPointService checkinPointService;
    private final UserCheckInService userCheckInService;
}
