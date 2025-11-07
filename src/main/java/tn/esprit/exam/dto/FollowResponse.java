package tn.esprit.exam.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FollowResponse(
    UUID id,
    UUID followerId,
    String followerUsername,
    UUID followingId,
    String followingUsername,
    OffsetDateTime createdAt
) {
}

