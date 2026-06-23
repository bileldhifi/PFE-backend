package tn.esprit.exam.service;

import tn.esprit.exam.dto.TimelineResponse;
import tn.esprit.exam.dto.TripRequest;
import tn.esprit.exam.dto.TripResponse;
import tn.esprit.exam.dto.UserStatsResponse;

import java.util.List;
import java.util.UUID;

public interface ITripService {
    TripResponse startTrip(UUID userId, TripRequest request);
    TripResponse endTrip(UUID tripId);
    List<TripResponse> getTripsByUser(UUID userId);
    TripResponse getTrip(UUID tripId);
    void deleteTrip(UUID tripId);
    
    /**
     * Get timeline for a trip with track points and associated posts
     *
     * @param tripId Trip identifier
     * @return Timeline with items and statistics
     */
    TimelineResponse getTimeline(UUID tripId);

    /**
     * Aggregated travel statistics for a user across all trips.
     *
     * @param userId User identifier
     * @return UserStatsResponse containing totals
     */
    UserStatsResponse getUserTravelStats(UUID userId);
}
