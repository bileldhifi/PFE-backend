package tn.esprit.exam.dto;

import tn.esprit.exam.entity.NotificationType;

import java.util.UUID;

/**
 * DTO for WebSocket notification updates
 */
public record NotificationUpdate(
        UUID notificationId,
        UUID userId, // Recipient user ID
        UUID actorId,
        String actorUsername,
        String actorAvatarUrl,
        NotificationType type,
        UUID postId,
        UUID commentId,
        String content,
        Long unreadCount
) {}

