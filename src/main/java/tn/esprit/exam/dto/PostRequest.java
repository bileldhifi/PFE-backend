package tn.esprit.exam.dto;

import tn.esprit.exam.entity.Visibility;

/**
 * Request DTO for creating a post
 * Simplified for multipart form-data handling
 */
public record PostRequest(
        Long trackPointId,
        Double latitude,
        Double longitude,
        String text,
        Visibility visibility
) {
}
