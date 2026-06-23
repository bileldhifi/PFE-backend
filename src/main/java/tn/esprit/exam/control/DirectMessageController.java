package tn.esprit.exam.control;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.exam.dto.ConversationCreateRequest;
import tn.esprit.exam.dto.ConversationResponse;
import tn.esprit.exam.dto.DirectMessageRequest;
import tn.esprit.exam.dto.DirectMessageResponse;
import tn.esprit.exam.service.IConversationService;
import tn.esprit.exam.service.IDirectMessageService;
import tn.esprit.exam.service.IUserService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@Slf4j
public class DirectMessageController {

    private final IConversationService conversationService;
    private final IDirectMessageService directMessageService;
    private final IUserService userService;

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationResponse>> getConversations(Authentication authentication) {
        UUID currentUserId = userService.getUserIdByEmail(authentication.getName());
        log.info("Listing conversations for user {}", currentUserId);
        return ResponseEntity.ok(conversationService.getUserConversations(currentUserId));
    }

    @PostMapping("/conversations")
    public ResponseEntity<ConversationResponse> createConversation(
            @RequestBody ConversationCreateRequest request,
            Authentication authentication
    ) {
        if (request == null || request.otherUserId() == null) {
            throw new IllegalArgumentException("otherUserId is required");
        }

        UUID currentUserId = userService.getUserIdByEmail(authentication.getName());
        log.info("Ensuring conversation for user {} with {}", currentUserId, request.otherUserId());

        ConversationResponse response = conversationService.ensureConversation(
                currentUserId,
                request.otherUserId()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<ConversationResponse> getConversation(
            @PathVariable UUID conversationId,
            Authentication authentication
    ) {
        UUID currentUserId = userService.getUserIdByEmail(authentication.getName());
        log.info("Fetching conversation {} for user {}", conversationId, currentUserId);
        return ResponseEntity.ok(conversationService.getConversation(currentUserId, conversationId));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<DirectMessageResponse>> getMessages(
            @PathVariable UUID conversationId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime before,
            @RequestParam(defaultValue = "50") int limit,
            Authentication authentication
    ) {
        UUID currentUserId = userService.getUserIdByEmail(authentication.getName());
        int safeLimit = Math.max(1, Math.min(limit, 100));

        log.info("Fetching messages for conversation {} (before={}, limit={})",
                conversationId, before, safeLimit);

        List<DirectMessageResponse> messages = directMessageService.getMessages(
                conversationId,
                currentUserId,
                before,
                safeLimit
        );

        return ResponseEntity.ok(messages);
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<DirectMessageResponse> sendMessage(
            @PathVariable UUID conversationId,
            @RequestBody DirectMessageRequest request,
            Authentication authentication
    ) {
        if (request == null || request.content() == null) {
            throw new IllegalArgumentException("Message content is required");
        }

        UUID senderId = userService.getUserIdByEmail(authentication.getName());
        log.info("User {} sending message in conversation {}", senderId, conversationId);

        DirectMessageResponse response = directMessageService.sendMessage(
                conversationId,
                senderId,
                request.content()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/conversations/{conversationId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable UUID conversationId,
            Authentication authentication
    ) {
        UUID currentUserId = userService.getUserIdByEmail(authentication.getName());
        log.info("Marking conversation {} as read for user {}", conversationId, currentUserId);

        directMessageService.markConversationAsRead(conversationId, currentUserId);
        return ResponseEntity.noContent().build();
    }
}

