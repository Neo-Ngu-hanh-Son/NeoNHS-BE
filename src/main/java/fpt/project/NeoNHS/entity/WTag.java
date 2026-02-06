package fpt.project.NeoNHS.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "wtags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class  WTag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    private String tagColor;

    private String iconUrl;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "wTag", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WorkshopTag> workshopTags;
}
