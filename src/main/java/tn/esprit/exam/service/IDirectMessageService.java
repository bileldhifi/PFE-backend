package tn.esprit.exam.service;

import tn.esprit.exam.dto.DirectMessageResponse;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface IDirectMessageService {

    List<DirectMessageResponse> getMessages(
            UUID conversationId,
            UUID currentUserId,
            OffsetDateTime before,
            int limit
    );

    DirectMessageResponse sendMessage(
            UUID conversationId,
            UUID senderId,
            String content
    );

    void markConversationAsRead(UUID conversationId, UUID userId);
}

