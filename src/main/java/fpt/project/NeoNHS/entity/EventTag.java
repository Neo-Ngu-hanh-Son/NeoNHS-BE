package fpt.project.NeoNHS.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "event_tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventTag {

    @EmbeddedId
    private EventTagId id;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("eventId")
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("eTagId")
    @JoinColumn(name = "etag_id")
    private ETag eTag;
}
