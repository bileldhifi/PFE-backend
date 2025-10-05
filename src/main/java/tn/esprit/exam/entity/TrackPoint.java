package tn.esprit.exam.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "track_points")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TrackPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    Trip trip;

    @Column(nullable = false)
    OffsetDateTime ts;

    Double lat;
    Double lon;
    Double accuracyM;
    Double speedMps;
}
