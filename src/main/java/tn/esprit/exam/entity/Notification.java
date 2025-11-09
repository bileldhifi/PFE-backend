package tn.esprit.exam.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notification_user_created", columnList = "user_id, created_at DESC")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Notification {

    @Id
    @GeneratedValue
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user; // The user who receives the notification

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    User actor; // The user who performed the action

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    NotificationType type;

    @Column(nullable = false)
    OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(nullable = false)
    Boolean isRead = false;

    // Related entity IDs (optional, for navigation)
    UUID postId; // For LIKE, COMMENT, NEW_POST
    UUID commentId; // For COMMENT
    String content; // Notification message/content

    // Helper method to generate notification content
    public String generateContent() {
        String actorName = actor != null ? actor.getUsername() : "Someone";
        return switch (type) {
            case LIKE -> actorName + " liked your post";
            case COMMENT -> actorName + " commented on your post";
            case FOLLOW -> actorName + " started following you";
            case NEW_POST -> actorName + " posted something new";
            case MENTION -> actorName + " mentioned you in a comment";
        };
    }
}

