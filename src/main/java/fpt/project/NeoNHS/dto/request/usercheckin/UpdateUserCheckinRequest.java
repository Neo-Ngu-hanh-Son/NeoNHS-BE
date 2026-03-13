package fpt.project.NeoNHS.dto.request.usercheckin;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Data
public class UpdateUserCheckinRequest {
    private String note;
    
    @Valid
    private List<UpdateCheckinImageCaptionRequest> images;
}
