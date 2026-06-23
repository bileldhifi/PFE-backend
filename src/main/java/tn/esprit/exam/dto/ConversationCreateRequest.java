package tn.esprit.exam.dto;

import java.util.UUID;

public record ConversationCreateRequest(
        UUID otherUserId
) {}

