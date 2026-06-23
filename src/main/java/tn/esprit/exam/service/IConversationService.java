package tn.esprit.exam.service;

import tn.esprit.exam.dto.ConversationResponse;

import java.util.List;
import java.util.UUID;

public interface IConversationService {

    ConversationResponse ensureConversation(UUID currentUserId, UUID otherUserId);

    List<ConversationResponse> getUserConversations(UUID currentUserId);

    ConversationResponse getConversation(UUID currentUserId, UUID conversationId);
}

