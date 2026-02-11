package fpt.project.NeoNHS.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "wtags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    private String tagColor;

    private String iconUrl;

    // Relationships
    @OneToMany(mappedBy = "wTag", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WorkshopTag> workshopTags;
}
