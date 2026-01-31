package fpt.project.NeoNHS.dto.request;

import lombok.Data;

@Data
public class UpdateUserProfileRequest {
    private String fullname;
    private String phoneNumber;
    private String avatarUrl;
    private String email;
}
