package tn.esprit.exam.dto;

import java.util.UUID;

public record DirectMessageUpdate(
        UUID conversationId,
        DirectMessageResponse message,
        UUID recipientId,
        Long recipientUnreadCount
) {}

