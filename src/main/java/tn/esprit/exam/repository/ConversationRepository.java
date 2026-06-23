package tn.esprit.exam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.exam.entity.Conversation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    @Query("""
            SELECT c FROM Conversation c
            WHERE (c.participantA.id = :user1 AND c.participantB.id = :user2)
               OR (c.participantA.id = :user2 AND c.participantB.id = :user1)
            """)
    Optional<Conversation> findBetweenUsers(
            @Param("user1") UUID user1,
            @Param("user2") UUID user2
    );

    @Query("""
            SELECT c FROM Conversation c
            JOIN FETCH c.participantA
            JOIN FETCH c.participantB
            WHERE c.participantA.id = :userId OR c.participantB.id = :userId
            ORDER BY c.updatedAt DESC
            """)
    List<Conversation> findAllForUser(@Param("userId") UUID userId);
}

