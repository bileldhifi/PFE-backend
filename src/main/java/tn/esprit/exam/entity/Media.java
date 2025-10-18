package tn.esprit.exam.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity
@Table(name = "media")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Media {

    @Id
    @GeneratedValue
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = true)
    Post post;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    MediaKind type;  // PHOTO, VIDEO, AUDIO

    @Column(nullable = false)
    String url;

    String thumbUrl;
    Long sizeBytes;
    Integer width;
    Integer height;
    Integer durationS;
}
