package tn.esprit.exam.dto;

/**
 * Aggregated travel statistics for a user across all trips.
 */
public record UserStatsResponse(
        int tripsCount,
        double totalDistanceKm,
        int countriesVisited,
        int citiesVisited,
        int postsCount,
        int photosCount
) {
}

