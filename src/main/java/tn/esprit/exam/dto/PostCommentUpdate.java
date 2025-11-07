package tn.esprit.exam.dto;

import java.util.UUID;

public record PostCommentUpdate(
    UUID postId,
    UUID commentId,
    UUID userId,
    String username,
    String content,
    long commentsCount
) {
}

