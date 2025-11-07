package tn.esprit.exam.dto;

import java.util.UUID;

public record PostLikeUpdate(
    UUID postId,
    UUID userId,
    String username,
    boolean isLiked,
    long likesCount
) {
}

