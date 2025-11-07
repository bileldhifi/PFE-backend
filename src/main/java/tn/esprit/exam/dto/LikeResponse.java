package tn.esprit.exam.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record LikeResponse(
    UUID id,
    UUID postId,
    UUID userId,
    String username,
    OffsetDateTime createdAt
) {
}

