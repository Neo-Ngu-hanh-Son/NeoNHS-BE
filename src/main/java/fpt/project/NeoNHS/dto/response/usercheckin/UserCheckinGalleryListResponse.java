package fpt.project.NeoNHS.dto.response.usercheckin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCheckinGalleryListResponse {
    private List<UserCheckinGalleryItemResponse> items;
}
