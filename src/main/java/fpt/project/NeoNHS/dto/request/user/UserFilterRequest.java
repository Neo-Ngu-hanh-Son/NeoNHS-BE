package fpt.project.NeoNHS.dto.request.user;

import fpt.project.NeoNHS.enums.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserFilterRequest {
    private String search;
    private UserRole role;
    private Boolean isBanned;
    private Boolean deleted;
    private Boolean includeDeleted;
}
