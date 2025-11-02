package tn.esprit.exam.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.exam.dto.TrackPointRequest;
import tn.esprit.exam.dto.TrackPointResponse;
import tn.esprit.exam.entity.TrackPoint;
import tn.esprit.exam.entity.Trip;
import tn.esprit.exam.repository.TrackPointRepository;
import tn.esprit.exam.repository.TripRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for TrackPoint operations
 * Handles business logic for location tracking with optimization
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrackPointServiceImpl implements ITrackPointService {

    private final TrackPointRepository trackPointRepository;
    private final TripRepository tripRepository;
    
    // Constants for optimization
    private static final double MIN_DISTANCE_METERS = 10.0; // Minimum distance between points
    private static final long MIN_TIME_SECONDS = 30; // Minimum time between points
    private static final double MAX_SPEED_KMH = 200.0; // Maximum reasonable speed

    @Override
    @Transactional
    public TrackPointResponse addTrackPoint(UUID tripId, TrackPointRequest request) {
        log.debug("Adding track point for trip: {}", tripId);
        
        // Validate trip exists
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found with id: " + tripId));
        
        // Validate request data
        validateTrackPointRequest(request);
        
        // Check if we should skip this point (optimization)
        if (shouldSkipTrackPoint(tripId, request)) {
            log.debug("Skipping track point due to optimization rules");
            return null; // Or return the last point
        }
        
        // Create and save track point
        TrackPoint trackPoint = new TrackPoint();
        trackPoint.setTrip(trip);
        trackPoint.setTs(OffsetDateTime.now());
        trackPoint.setLat(request.lat());
        trackPoint.setLon(request.lon());
        trackPoint.setAccuracyM(request.accuracyM());
        trackPoint.setSpeedMps(request.speedMps());
        
        TrackPoint saved = trackPointRepository.save(trackPoint);
        log.info("Track point saved with id: {} for trip: {}", saved.getId(), tripId);
        
        return TrackPointResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrackPointResponse> getTrackPointsByTrip(UUID tripId) {
        log.debug("Getting track points for trip: {}", tripId);
        
        // Validate trip exists
        if (!tripRepository.existsById(tripId)) {
            throw new RuntimeException("Trip not found with id: " + tripId);
        }
        
        return trackPointRepository.findByTripIdOrderByTsAsc(tripId)
                .stream()
                .map(TrackPointResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrackPointResponse> getTrackPointsByTripAndTimeRange(
            UUID tripId, 
            OffsetDateTime startTime, 
            OffsetDateTime endTime) {
        log.debug("Getting track points for trip: {} between {} and {}", tripId, startTime, endTime);
        
        // Validate trip exists
        if (!tripRepository.existsById(tripId)) {
            throw new RuntimeException("Trip not found with id: " + tripId);
        }
        
        return trackPointRepository.findByTripIdAndTsBetweenOrderByTsAsc(tripId, startTime, endTime)
                .stream()
                .map(TrackPointResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public void deleteTrackPoint(Long trackPointId) {
        log.debug("Deleting track point: {}", trackPointId);
        
        TrackPoint trackPoint = trackPointRepository.findById(trackPointId)
                .orElseThrow(() -> new RuntimeException("Track point not found with id: " + trackPointId));
        
        trackPointRepository.delete(trackPoint);
        log.info("Track point deleted with id: {}", trackPointId);
    }

    @Override
    @Transactional(readOnly = true)
    public TrackPointResponse getLatestTrackPoint(UUID tripId) {
        log.debug("Getting latest track point for trip: {}", tripId);
        
        return trackPointRepository.findTopByTripIdOrderByTsDesc(tripId)
                .map(TrackPointResponse::fromEntity)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public double calculateTotalDistance(UUID tripId) {
        log.debug("Calculating total distance for trip: {}", tripId);
        
        List<TrackPointResponse> trackPoints = getTrackPointsByTrip(tripId);
        
        if (trackPoints.size() < 2) {
            return 0.0;
        }
        
        double totalDistance = 0.0;
        for (int i = 1; i < trackPoints.size(); i++) {
            totalDistance += trackPoints.get(i - 1).distanceTo(trackPoints.get(i));
        }
        
        log.info("Total distance for trip {}: {} meters", tripId, totalDistance);
        return totalDistance;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrackPointResponse> getTrackPointsNearLocation(
            UUID tripId, 
            Double lat, 
            Double lon, 
            Double radiusMeters) {
        log.debug("Getting track points near location ({}, {}) within {}m for trip: {}", 
                lat, lon, radiusMeters, tripId);
        
        // Validate trip exists
        if (!tripRepository.existsById(tripId)) {
            throw new RuntimeException("Trip not found with id: " + tripId);
        }
        
        // Get all track points and filter by distance
        return getTrackPointsByTrip(tripId)
                .stream()
                .filter(tp -> tp.distanceTo(new TrackPointResponse(
                        null, null, null, lat, lon, null, null, null, null, null, null, null
                )) <= radiusMeters)
                .toList();
    }
    
    /**
     * Validates track point request data
     */
    private void validateTrackPointRequest(TrackPointRequest request) {
        if (request == null) {
            throw new RuntimeException("Track point request cannot be null");
        }
        
        if (!request.isValidLocation()) {
            throw new RuntimeException("Invalid location coordinates");
        }
        
        if (request.accuracyM() != null && request.accuracyM() < 0) {
            throw new RuntimeException("Accuracy must be positive");
        }
        
        if (request.speedMps() != null && request.speedMps() < 0) {
            throw new RuntimeException("Speed must be positive");
        }
        
        // Check for unreasonable speed
        if (request.speedMps() != null && (request.speedMps() * 3.6) > MAX_SPEED_KMH) {
            log.warn("Unusually high speed detected: {} km/h", request.speedMps() * 3.6);
        }
    }
    
    /**
     * Determines if a track point should be skipped for optimization
     */
    private boolean shouldSkipTrackPoint(UUID tripId, TrackPointRequest request) {
        TrackPointResponse latest = getLatestTrackPoint(tripId);
        
        if (latest == null) {
            return false; // First point, don't skip
        }
        
        // Check distance
        double distance = latest.distanceTo(new TrackPointResponse(
                null, null, null, request.lat(), request.lon(), null, null, null, null, null, null, null
        ));
        
        if (distance < MIN_DISTANCE_METERS) {
            return true; // Too close to last point
        }
        
        // Check time
        long timeDiff = latest.timeDifferenceSeconds(new TrackPointResponse(
                null, null, OffsetDateTime.now(), null, null, null, null, null, null, null, null, null
        ));
        
        if (timeDiff < MIN_TIME_SECONDS) {
            return true; // Too soon after last point
        }
        
        return false;
    }
    
    @Override
    @Transactional
    public List<TrackPointResponse> addTrackPointsBulk(UUID tripId, List<TrackPointRequest> requests) {
        log.info("Adding {} track points in bulk for trip: {}", requests.size(), tripId);
        
        // Validate trip exists
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found with id: " + tripId));
        
        // Apply smart filtering to remove points that should be skipped
        List<TrackPointRequest> filteredRequests = requests.stream()
                .filter(request -> {
                    // Validate request data
                    validateTrackPointRequest(request);
                    // Check if we should skip this point
                    return !shouldSkipTrackPoint(tripId, request);
                })
                .collect(Collectors.toList());
        
        log.info("After filtering: {} track points will be saved ({} skipped)", 
                filteredRequests.size(), requests.size() - filteredRequests.size());
        
        if (filteredRequests.isEmpty()) {
            log.info("No track points to save after filtering");
            return List.of();
        }
        
        // Batch create track points
        List<TrackPoint> trackPoints = filteredRequests.stream()
                .map(request -> {
                    TrackPoint trackPoint = new TrackPoint();
                    trackPoint.setTrip(trip);
                    trackPoint.setTs(OffsetDateTime.now());
                    trackPoint.setLat(request.lat());
                    trackPoint.setLon(request.lon());
                    trackPoint.setAccuracyM(request.accuracyM());
                    trackPoint.setSpeedMps(request.speedMps());
                    return trackPoint;
                })
                .collect(Collectors.toList());
        
        // Batch save all track points in a single database operation
        List<TrackPoint> savedTrackPoints = trackPointRepository.saveAll(trackPoints);
        
        log.info("Successfully saved {} track points in bulk for trip: {}", 
                savedTrackPoints.size(), tripId);
        
        // Convert to response DTOs
        return savedTrackPoints.stream()
                .map(TrackPointResponse::fromEntity)
                .collect(Collectors.toList());
    }
}