package tn.esprit.exam.dto;

import java.util.Map;

/**
 * DTO for trip statistics
 * Contains calculated metrics about a trip
 */
public record TripStatsDTO(
        int stepsCount,
        double distanceKm,
        int countriesCount,
        int citiesCount,
        int photosCount,
        Map<String, Double> transportMethods
) {
}

