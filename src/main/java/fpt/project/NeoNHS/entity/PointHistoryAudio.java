package fpt.project.NeoNHS.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "point_history_audios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointHistoryAudio extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String audioUrl;

    @Column(length = 500)
    private String title;

    @Column(length = 500)
    private String artist;

    @Column(columnDefinition = "TEXT")
    private String historyText;

    private String language;

    private String modelId;

    private String voiceId;

    private String mode;

    @Column(columnDefinition = "JSON")
    private String words;

    private String coverImage; // Default to the point's thumbnail if not provided

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_id", nullable = false)
    private Point point;
}
