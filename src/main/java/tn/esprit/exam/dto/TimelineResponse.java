package tn.esprit.exam.dto;

import lombok.Builder;

import java.util.List;

/**
 * Complete timeline response with items and statistics
 */
@Builder
public record TimelineResponse(
        List<TimelineItemResponse> items,
        TimelineStats stats
) {
    @Builder
    public record TimelineStats(
            Double totalDistanceKm,
            Long totalDurationSeconds,
            Double avgSpeedKmh,
            Double maxSpeedKmh,
            Integer totalPhotos,
            Integer totalTrackPoints
    ) {}
}

