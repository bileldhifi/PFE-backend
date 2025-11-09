package tn.esprit.exam.dto;

import tn.esprit.exam.entity.NotificationType;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO for notification responses
 */
public record NotificationResponse(
        UUID id,
        UUID actorId,
        String actorUsername,
        String actorAvatarUrl,
        NotificationType type,
        OffsetDateTime createdAt,
        Boolean isRead,
        UUID postId,
        UUID commentId,
        String content
) {}

