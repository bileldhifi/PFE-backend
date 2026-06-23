package tn.esprit.exam.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ConversationResponse(
        UUID id,
        UUID otherUserId,
        String otherUsername,
        String otherAvatarUrl,
        String lastMessage,
        OffsetDateTime lastMessageAt,
        Long unreadCount
) {}

