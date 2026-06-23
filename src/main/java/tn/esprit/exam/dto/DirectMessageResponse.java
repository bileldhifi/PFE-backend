package tn.esprit.exam.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DirectMessageResponse(
        UUID id,
        UUID conversationId,
        UUID senderId,
        String content,
        OffsetDateTime createdAt,
        OffsetDateTime readAt
) {}

