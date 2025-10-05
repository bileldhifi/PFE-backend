package tn.esprit.exam.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "posts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Post {

    @Id
    @GeneratedValue
    UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    Trip trip;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(nullable = false)
    private OffsetDateTime ts = OffsetDateTime.now();

    @Column(columnDefinition = "TEXT")
    String text;

    String city;
    String country;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    Visibility visibility;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_point_id", nullable = true)
    private TrackPoint trackPoint;

    @JsonIgnore
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Media> media;
}
