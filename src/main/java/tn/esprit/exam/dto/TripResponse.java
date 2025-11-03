package tn.esprit.exam.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response DTO for Trip entity
 * Includes trip details and calculated statistics
 */
public record TripResponse(
        UUID id,
        String title,
        OffsetDateTime startedAt,
        OffsetDateTime endedAt,
        String userEmail,
        String coverUrl,
        String visibility,
        TripStatsDTO stats
) {
}
