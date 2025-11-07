package tn.esprit.exam.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CommentResponse(
    UUID id,
    UUID postId,
    UUID userId,
    String username,
    String content,
    OffsetDateTime createdAt
) {
}

