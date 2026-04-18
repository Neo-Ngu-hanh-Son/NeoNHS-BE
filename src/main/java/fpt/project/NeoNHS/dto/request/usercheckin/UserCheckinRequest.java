package fpt.project.NeoNHS.dto.request.usercheckin;

import fpt.project.NeoNHS.enums.UserCheckinMethod;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class UserCheckinRequest {
    private double latitude;
    private double longitude;
    private String imageUrl;
    private UserCheckinMethod method;
    private String note;
    private String checkinPointId;
    private List<CheckinImageRequest> checkinImageRequest;
}
