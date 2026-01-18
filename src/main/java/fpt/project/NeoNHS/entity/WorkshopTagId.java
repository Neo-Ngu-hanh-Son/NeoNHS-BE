package fpt.project.NeoNHS.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class WorkshopTagId implements Serializable {

    private UUID workshopId;

    private UUID wTagId;
}
