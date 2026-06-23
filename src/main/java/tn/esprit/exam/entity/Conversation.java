package tn.esprit.exam.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "conversations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_conversation_participants",
                columnNames = {"participant_a_id", "participant_b_id"}
        ),
        indexes = {
                @Index(name = "idx_conversation_updated", columnList = "updated_at DESC"),
                @Index(name = "idx_conversation_participant_a", columnList = "participant_a_id"),
                @Index(name = "idx_conversation_participant_b", columnList = "participant_b_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Conversation {

    @Id
    @GeneratedValue
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_a_id", nullable = false)
    User participantA;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_b_id", nullable = false)
    User participantB;

    @Column(nullable = false, name = "created_at")
    OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(nullable = false, name = "updated_at")
    OffsetDateTime updatedAt = OffsetDateTime.now();

    @OneToMany(
            mappedBy = "conversation",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    List<DirectMessage> messages = new ArrayList<>();

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}

