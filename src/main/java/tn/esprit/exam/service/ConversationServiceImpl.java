package tn.esprit.exam.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.exam.dto.ConversationResponse;
import tn.esprit.exam.entity.Conversation;
import tn.esprit.exam.entity.DirectMessage;
import tn.esprit.exam.entity.User;
import tn.esprit.exam.repository.ConversationRepository;
import tn.esprit.exam.repository.DirectMessageRepository;
import tn.esprit.exam.repository.UserRepository;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationServiceImpl implements IConversationService {

    private final ConversationRepository conversationRepository;
    private final DirectMessageRepository directMessageRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ConversationResponse ensureConversation(UUID currentUserId, UUID otherUserId) {
        if (currentUserId.equals(otherUserId)) {
            throw new IllegalArgumentException("Cannot create a conversation with yourself");
        }

        log.info("Ensuring conversation between {} and {}", currentUserId, otherUserId);

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new RuntimeException("Other user not found"));

        List<User> ordered = List.of(currentUser, otherUser).stream()
                .sorted(Comparator.comparing(User::getId))
                .toList();

        User participantA = ordered.get(0);
        User participantB = ordered.get(1);

        Conversation conversation = conversationRepository
                .findBetweenUsers(participantA.getId(), participantB.getId())
                .orElseGet(() -> {
                    Conversation created = new Conversation();
                    created.setParticipantA(participantA);
                    created.setParticipantB(participantB);
                    log.info("Creating new conversation between {} and {}", participantA.getId(), participantB.getId());
                    return conversationRepository.save(created);
                });

        return mapToResponse(conversation, currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> getUserConversations(UUID currentUserId) {
        log.info("Fetching conversations for user {}", currentUserId);
        List<Conversation> conversations = conversationRepository.findAllForUser(currentUserId);
        return conversations.stream()
                .map(conversation -> mapToResponse(conversation, currentUserId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationResponse getConversation(UUID currentUserId, UUID conversationId) {
        log.info("Fetching conversation {} for user {}", conversationId, currentUserId);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        if (!isParticipant(conversation, currentUserId)) {
            throw new RuntimeException("You are not a participant of this conversation");
        }

        return mapToResponse(conversation, currentUserId);
    }

    private ConversationResponse mapToResponse(Conversation conversation, UUID currentUserId) {
        User other = conversation.getParticipantA().getId().equals(currentUserId)
                ? conversation.getParticipantB()
                : conversation.getParticipantA();

        DirectMessage lastMessage = directMessageRepository
                .findTop1ByConversationIdOrderByCreatedAtDesc(conversation.getId());

        Long unreadCount = directMessageRepository
                .countUnreadForUser(conversation.getId(), currentUserId);

        String avatarUrl = other.getAvatarMedia() != null
                ? other.getAvatarMedia().getUrl()
                : null;

        return new ConversationResponse(
                conversation.getId(),
                other.getId(),
                other.getUsername(),
                avatarUrl,
                lastMessage != null ? lastMessage.getContent() : null,
                lastMessage != null ? lastMessage.getCreatedAt() : conversation.getUpdatedAt(),
                unreadCount
        );
    }

    private boolean isParticipant(Conversation conversation, UUID userId) {
        return conversation.getParticipantA().getId().equals(userId)
                || conversation.getParticipantB().getId().equals(userId);
    }
}

