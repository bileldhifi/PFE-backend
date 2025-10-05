package tn.esprit.exam.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TripResponse(UUID id,
                           String title,
                           OffsetDateTime startedAt,
                           OffsetDateTime endedAt,
                           String userEmail) {
}
