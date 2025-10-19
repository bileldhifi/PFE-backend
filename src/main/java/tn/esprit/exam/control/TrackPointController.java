package tn.esprit.exam.control;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.exam.dto.TrackPointRequest;
import tn.esprit.exam.dto.TrackPointResponse;
import tn.esprit.exam.service.ITrackPointService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for TrackPoint operations
 * Provides endpoints for location tracking functionality
 */
@Slf4j
@RestController
@RequestMapping("/trips/{tripId}/track-points")
@RequiredArgsConstructor
public class TrackPointController {

    private final ITrackPointService trackPointService;

    /**
     * Add a new track point to a trip
     * POST /trips/{tripId}/track-points
     */
    @PostMapping
    public ResponseEntity<TrackPointResponse> addTrackPoint(
            @PathVariable UUID tripId,
            @Valid @RequestBody TrackPointRequest request) {
        
        log.info("Adding track point for trip: {}", tripId);
        
        try {
            TrackPointResponse response = trackPointService.addTrackPoint(tripId, request);
            
            if (response == null) {
                // Track point was skipped due to optimization
                return ResponseEntity.ok().build();
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            log.error("Error adding track point for trip {}: {}", tripId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get all track points for a trip
     * GET /trips/{tripId}/track-points
     */
    @GetMapping
    public ResponseEntity<List<TrackPointResponse>> getTrackPoints(@PathVariable UUID tripId) {
        log.info("Getting track points for trip: {}", tripId);
        
        try {
            List<TrackPointResponse> trackPoints = trackPointService.getTrackPointsByTrip(tripId);
            return ResponseEntity.ok(trackPoints);
        } catch (RuntimeException e) {
            log.error("Error getting track points for trip {}: {}", tripId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get track points within a time range
     * GET /trips/{tripId}/track-points?startTime=...&endTime=...
     */
    @GetMapping(params = {"startTime", "endTime"})
    public ResponseEntity<List<TrackPointResponse>> getTrackPointsByTimeRange(
            @PathVariable UUID tripId,
            @RequestParam OffsetDateTime startTime,
            @RequestParam OffsetDateTime endTime) {
        
        log.info("Getting track points for trip {} between {} and {}", tripId, startTime, endTime);
        
        try {
            List<TrackPointResponse> trackPoints = trackPointService
                    .getTrackPointsByTripAndTimeRange(tripId, startTime, endTime);
            return ResponseEntity.ok(trackPoints);
        } catch (RuntimeException e) {
            log.error("Error getting track points by time range for trip {}: {}", tripId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get track points near a specific location
     * GET /trips/{tripId}/track-points?lat=...&lon=...&radius=...
     */
    @GetMapping(params = {"lat", "lon", "radius"})
    public ResponseEntity<List<TrackPointResponse>> getTrackPointsNearLocation(
            @PathVariable UUID tripId,
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam Double radius) {
        
        log.info("Getting track points near location ({}, {}) within {}m for trip: {}", 
                lat, lon, radius, tripId);
        
        try {
            List<TrackPointResponse> trackPoints = trackPointService
                    .getTrackPointsNearLocation(tripId, lat, lon, radius);
            return ResponseEntity.ok(trackPoints);
        } catch (RuntimeException e) {
            log.error("Error getting track points near location for trip {}: {}", tripId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get the latest track point for a trip
     * GET /trips/{tripId}/track-points/latest
     */
    @GetMapping("/latest")
    public ResponseEntity<TrackPointResponse> getLatestTrackPoint(@PathVariable UUID tripId) {
        log.info("Getting latest track point for trip: {}", tripId);
        
        try {
            TrackPointResponse latest = trackPointService.getLatestTrackPoint(tripId);
            return latest != null ? ResponseEntity.ok(latest) : ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            log.error("Error getting latest track point for trip {}: {}", tripId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Calculate total distance for a trip
     * GET /trips/{tripId}/track-points/distance
     */
    @GetMapping("/distance")
    public ResponseEntity<Double> getTotalDistance(@PathVariable UUID tripId) {
        log.info("Calculating total distance for trip: {}", tripId);
        
        try {
            double distance = trackPointService.calculateTotalDistance(tripId);
            return ResponseEntity.ok(distance);
        } catch (RuntimeException e) {
            log.error("Error calculating distance for trip {}: {}", tripId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete a specific track point
     * DELETE /trips/{tripId}/track-points/{trackPointId}
     */
    @DeleteMapping("/{trackPointId}")
    public ResponseEntity<Void> deleteTrackPoint(
            @PathVariable UUID tripId,
            @PathVariable Long trackPointId) {
        
        log.info("Deleting track point {} for trip: {}", trackPointId, tripId);
        
        try {
            trackPointService.deleteTrackPoint(trackPointId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error deleting track point {} for trip {}: {}", trackPointId, tripId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Bulk add track points (for batch operations)
     * POST /trips/{tripId}/track-points/bulk
     */
    @PostMapping("/bulk")
    public ResponseEntity<List<TrackPointResponse>> addTrackPointsBulk(
            @PathVariable UUID tripId,
            @RequestBody List<TrackPointRequest> requests) {
        
        log.info("Adding {} track points in bulk for trip: {}", requests.size(), tripId);
        
        try {
            // Use the new true batch processing method
            List<TrackPointResponse> responses = trackPointService.addTrackPointsBulk(tripId, requests);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(responses);
        } catch (RuntimeException e) {
            log.error("Error adding track points in bulk for trip {}: {}", tripId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}