package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.usercheckin.UserCheckinRequest;

public interface UserCheckInService {
    void checkIn(UserCheckinRequest request);
}
