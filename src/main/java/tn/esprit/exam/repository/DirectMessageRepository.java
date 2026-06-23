package tn.esprit.exam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.exam.entity.DirectMessage;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, UUID> {

    List<DirectMessage> findTop50ByConversationIdOrderByCreatedAtDesc(UUID conversationId);

    DirectMessage findTop1ByConversationIdOrderByCreatedAtDesc(UUID conversationId);

    List<DirectMessage> findTop50ByConversationIdAndCreatedAtBeforeOrderByCreatedAtDesc(
            UUID conversationId,
            OffsetDateTime createdAt
    );

    @Query("""
            SELECT COUNT(dm) FROM DirectMessage dm
            WHERE dm.conversation.id = :conversationId
              AND dm.sender.id <> :userId
              AND dm.readAt IS NULL
            """)
    Long countUnreadForUser(
            @Param("conversationId") UUID conversationId,
            @Param("userId") UUID userId
    );

    @Modifying
    @Query("""
            UPDATE DirectMessage dm
            SET dm.readAt = CURRENT_TIMESTAMP
            WHERE dm.conversation.id = :conversationId
              AND dm.sender.id <> :userId
              AND dm.readAt IS NULL
            """)
    void markConversationMessagesAsRead(
            @Param("conversationId") UUID conversationId,
            @Param("userId") UUID userId
    );
}

