package tn.esprit.exam.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.exam.dto.DirectMessageResponse;
import tn.esprit.exam.dto.DirectMessageUpdate;
import tn.esprit.exam.entity.Conversation;
import tn.esprit.exam.entity.DirectMessage;
import tn.esprit.exam.entity.User;
import tn.esprit.exam.repository.ConversationRepository;
import tn.esprit.exam.repository.DirectMessageRepository;
import tn.esprit.exam.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DirectMessageServiceImpl implements IDirectMessageService {

    private final ConversationRepository conversationRepository;
    private final DirectMessageRepository directMessageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional(readOnly = true)
    public List<DirectMessageResponse> getMessages(
            UUID conversationId,
            UUID currentUserId,
            OffsetDateTime before,
            int limit
    ) {
        log.info("Fetching messages for conversation {} (before={}, limit={})",
                conversationId, before, limit);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        validateParticipant(conversation, currentUserId);

        List<DirectMessage> messages = before != null
                ? directMessageRepository.findTop50ByConversationIdAndCreatedAtBeforeOrderByCreatedAtDesc(
                        conversationId, before)
                : directMessageRepository.findTop50ByConversationIdOrderByCreatedAtDesc(conversationId);

        if (messages.size() > limit) {
            messages = messages.subList(0, limit);
        }

        Collections.reverse(messages);

        return messages.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public DirectMessageResponse sendMessage(UUID conversationId, UUID senderId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be empty");
        }

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        validateParticipant(conversation, senderId);

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        DirectMessage message = new DirectMessage();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(content.trim());
        message.setCreatedAt(OffsetDateTime.now());

        DirectMessage saved = directMessageRepository.save(message);

        conversation.setUpdatedAt(OffsetDateTime.now());
        conversationRepository.save(conversation);

        UUID recipientId = conversation.getParticipantA().getId().equals(senderId)
                ? conversation.getParticipantB().getId()
                : conversation.getParticipantA().getId();

        Long recipientUnreadCount = directMessageRepository
                .countUnreadForUser(conversationId, recipientId);

        DirectMessageResponse response = mapToResponse(saved);

        DirectMessageUpdate update = new DirectMessageUpdate(
                conversationId,
                response,
                recipientId,
                recipientUnreadCount
        );

        log.info("Publishing DM update to /topic/dm/{} for recipient {}", conversationId, recipientId);
        messagingTemplate.convertAndSend("/topic/dm/" + conversationId, update);

        return response;
    }

    @Override
    @Transactional
    public void markConversationAsRead(UUID conversationId, UUID userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        validateParticipant(conversation, userId);

        directMessageRepository.markConversationMessagesAsRead(conversationId, userId);

        DirectMessageUpdate update = new DirectMessageUpdate(
                conversationId,
                null,
                userId,
                0L
        );

        messagingTemplate.convertAndSend("/topic/dm/" + conversationId, update);
    }

    private DirectMessageResponse mapToResponse(DirectMessage message) {
        return new DirectMessageResponse(
                message.getId(),
                message.getConversation().getId(),
                message.getSender().getId(),
                message.getContent(),
                message.getCreatedAt(),
                message.getReadAt()
        );
    }

    private void validateParticipant(Conversation conversation, UUID userId) {
        boolean participant = conversation.getParticipantA().getId().equals(userId)
                || conversation.getParticipantB().getId().equals(userId);

        if (!participant) {
            throw new RuntimeException("You are not a participant of this conversation");
        }
    }
}

