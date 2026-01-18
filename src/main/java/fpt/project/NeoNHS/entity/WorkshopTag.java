package fpt.project.NeoNHS.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "workshop_tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkshopTag {

    @EmbeddedId
    private WorkshopTagId id;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("workshopId")
    @JoinColumn(name = "workshop_id")
    private WorkshopTemplate workshopTemplate;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("wTagId")
    @JoinColumn(name = "wtag_id")
    private WTag wTag;
}
