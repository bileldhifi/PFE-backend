package tn.esprit.exam.service;

import tn.esprit.exam.dto.TrackPointRequest;
import tn.esprit.exam.dto.TrackPointResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for TrackPoint operations
 * Provides business logic for location tracking
 */
public interface ITrackPointService {
    
    /**
     * Add a new track point to a trip
     * @param tripId The ID of the trip
     * @param request The track point data
     * @return The created track point response
     * @throws RuntimeException if trip not found or invalid data
     */
    TrackPointResponse addTrackPoint(UUID tripId, TrackPointRequest request);
    
    /**
     * Get all track points for a specific trip
     * @param tripId The ID of the trip
     * @return List of track points ordered by timestamp
     * @throws RuntimeException if trip not found
     */
    List<TrackPointResponse> getTrackPointsByTrip(UUID tripId);
    
    /**
     * Get track points for a trip within a time range
     * @param tripId The ID of the trip
     * @param startTime Start of time range (inclusive)
     * @param endTime End of time range (inclusive)
     * @return List of track points within the time range
     */
    List<TrackPointResponse> getTrackPointsByTripAndTimeRange(
        UUID tripId, 
        java.time.OffsetDateTime startTime, 
        java.time.OffsetDateTime endTime
    );
    
    /**
     * Delete a specific track point
     * @param trackPointId The ID of the track point to delete
     * @throws RuntimeException if track point not found
     */
    void deleteTrackPoint(Long trackPointId);
    
    /**
     * Get the latest track point for a trip
     * @param tripId The ID of the trip
     * @return The most recent track point, or null if none exist
     */
    TrackPointResponse getLatestTrackPoint(UUID tripId);
    
    /**
     * Calculate total distance traveled for a trip
     * @param tripId The ID of the trip
     * @return Total distance in meters
     */
    double calculateTotalDistance(UUID tripId);
    
    /**
     * Get track points within a specific radius of a location
     * @param tripId The ID of the trip
     * @param lat Latitude of center point
     * @param lon Longitude of center point
     * @param radiusMeters Radius in meters
     * @return List of track points within the radius
     */
    List<TrackPointResponse> getTrackPointsNearLocation(
        UUID tripId, 
        Double lat, 
        Double lon, 
        Double radiusMeters
    );
    
    /**
     * Add multiple track points in a single batch operation
     * @param tripId The ID of the trip
     * @param requests List of track point requests
     * @return List of created track point responses
     */
    List<TrackPointResponse> addTrackPointsBulk(UUID tripId, List<TrackPointRequest> requests);
}