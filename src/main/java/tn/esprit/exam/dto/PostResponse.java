package tn.esprit.exam.dto;

import tn.esprit.exam.entity.Visibility;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for post data including media
 */
public record PostResponse(
        UUID id,
        String text,
        Visibility visibility,
        OffsetDateTime ts,
        UUID tripId,
        Long trackPointId,
        Double latitude,
        Double longitude,
        String userEmail,
        String username,
        String city,
        String country,
        List<MediaResponse> media
) {
}
