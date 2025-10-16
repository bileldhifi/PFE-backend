package tn.esprit.exam.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponse(UUID id,
                           String email,
                           String username,
                           String role,
                           String defaultVisibility,
                           OffsetDateTime createdAt,
                           String bio,
                           String avatarUrl,
                           int tripsCount,
                           int stepsCount,
                           int followersCount,
                           int followingCount) {
}
