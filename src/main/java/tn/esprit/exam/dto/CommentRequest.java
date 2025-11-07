package tn.esprit.exam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CommentRequest(
    @NotNull(message = "Post ID is required")
    UUID postId,
    
    @NotBlank(message = "Content is required")
    String content
) {
}

