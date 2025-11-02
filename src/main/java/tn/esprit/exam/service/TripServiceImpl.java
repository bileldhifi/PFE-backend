package tn.esprit.exam.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprit.exam.dto.*;
import tn.esprit.exam.entity.TrackPoint;
import tn.esprit.exam.entity.Trip;
import tn.esprit.exam.entity.User;
import tn.esprit.exam.repository.TrackPointRepository;
import tn.esprit.exam.repository.TripRepository;
import tn.esprit.exam.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripServiceImpl implements ITripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TrackPointRepository trackPointRepository;
    private final IPostService postService;

    @Override
    public TripResponse startTrip(UUID userId, TripRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Trip trip = new Trip();
        trip.setUser(user);
        trip.setTitle(request.title());
        trip.setStartedAt(OffsetDateTime.now());

        Trip saved = tripRepository.save(trip);

        return new TripResponse(
                saved.getId(),
                saved.getTitle(),
                saved.getStartedAt(),
                saved.getEndedAt(),
                saved.getUser().getEmail()
        );
    }

    @Override
    public TripResponse endTrip(UUID tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        trip.setEndedAt(OffsetDateTime.now());
        Trip updated = tripRepository.save(trip);

        return new TripResponse(
                updated.getId(),
                updated.getTitle(),
                updated.getStartedAt(),
                updated.getEndedAt(),
                updated.getUser().getEmail()
        );
    }

    @Override
    public List<TripResponse> getTripsByUser(UUID userId) {
        return tripRepository.findByUserId(userId)
                .stream()
                .map(t -> new TripResponse(
                        t.getId(),
                        t.getTitle(),
                        t.getStartedAt(),
                        t.getEndedAt(),
                        t.getUser().getEmail()
                ))
                .toList();
    }

    @Override
    public TripResponse getTrip(UUID tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        return new TripResponse(
                trip.getId(),
                trip.getTitle(),
                trip.getStartedAt(),
                trip.getEndedAt(),
                trip.getUser().getEmail()
        );
    }

    @Override
    public void deleteTrip(UUID tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));
        
        tripRepository.delete(trip);
    }

    @Override
    public TimelineResponse getTimeline(UUID tripId) {
        log.info("Generating timeline for trip: {}", tripId);
        
        // Verify trip exists
        tripRepository.findById(tripId)
                .orElseThrow(() -> 
                    new RuntimeException("Trip not found")
                );
        
        // Get all track points for trip, ordered by timestamp
        List<TrackPoint> trackPoints = 
                trackPointRepository.findByTripIdOrderByTsAsc(tripId);
        
        log.info(
                "Found {} track points for trip {}", 
                trackPoints.size(), 
                tripId
        );
        
        // Build timeline items
        List<TimelineItemResponse> items = new ArrayList<>();
        TrackPoint previousPoint = null;
        
        double totalDistance = 0;
        long totalDuration = 0;
        double maxSpeed = 0;
        int totalPhotos = 0;
        double totalSpeed = 0;
        int pointsWithSpeed = 0;
        
        for (TrackPoint trackPoint : trackPoints) {
            // Get posts at this track point
            List<PostResponse> posts = 
                    postService.getPostsByTrackPoint(
                            trackPoint.getId()
                    );
            
            // Count photos
            int photoCount = posts.stream()
                    .mapToInt(p -> p.media().size())
                    .sum();
            totalPhotos += photoCount;
            
            // Calculate distance and time from previous point
            Double distanceFromPrevious = null;
            Long timeFromPrevious = null;
            
            if (previousPoint != null) {
                distanceFromPrevious = 
                        calculateDistance(previousPoint, trackPoint);
                timeFromPrevious = 
                        previousPoint.getTs()
                                .until(
                                        trackPoint.getTs(), 
                                        java.time.temporal.ChronoUnit.SECONDS
                                );
                
                totalDistance += distanceFromPrevious;
                totalDuration += timeFromPrevious;
            }
            
            // Track max speed and average speed
            Double speedKmh = trackPoint.getSpeedMps() != null
                    ? trackPoint.getSpeedMps() * 3.6
                    : null;
            
            if (speedKmh != null) {
                maxSpeed = Math.max(maxSpeed, speedKmh);
                totalSpeed += speedKmh;
                pointsWithSpeed++;
            }
            
            // Build timeline item
            TimelineItemResponse item = TimelineItemResponse.builder()
                    .trackPointId(trackPoint.getId())
                    .timestamp(trackPoint.getTs())
                    .latitude(trackPoint.getLat())
                    .longitude(trackPoint.getLon())
                    .locationName(null)  
                    // TODO: Add geocoding service
                    .speedKmh(speedKmh)
                    .accuracyMeters(trackPoint.getAccuracyM())
                    .isSignificant(photoCount > 0)  
                    // Mark as significant if has photos
                    .distanceFromPreviousKm(distanceFromPrevious)
                    .timeFromPreviousSeconds(timeFromPrevious)
                    .posts(posts)
                    .photoCount(photoCount)
                    .build();
            
            items.add(item);
            previousPoint = trackPoint;
        }
        
        // Calculate average speed
        double avgSpeed = pointsWithSpeed > 0
                ? totalSpeed / pointsWithSpeed
                : 0;
        
        // Build statistics
        TimelineResponse.TimelineStats stats = 
                TimelineResponse.TimelineStats.builder()
                        .totalDistanceKm(totalDistance)
                        .totalDurationSeconds(totalDuration)
                        .avgSpeedKmh(avgSpeed)
                        .maxSpeedKmh(maxSpeed)
                        .totalPhotos(totalPhotos)
                        .totalTrackPoints(trackPoints.size())
                        .build();
        
        log.info(
                "Generated timeline with {} items, " +
                "total distance: {:.2f}km, " +
                "total photos: {}", 
                items.size(), 
                totalDistance, 
                totalPhotos
        );
        
        return TimelineResponse.builder()
                .items(items)
                .stats(stats)
                .build();
    }
    
    /**
     * Calculate distance between two track points using Haversine formula
     *
     * @param point1 First track point
     * @param point2 Second track point
     * @return Distance in kilometers
     */
    private double calculateDistance(
            TrackPoint point1, 
            TrackPoint point2
    ) {
        final double EARTH_RADIUS_KM = 6371.0;
        
        double lat1Rad = Math.toRadians(point1.getLat());
        double lat2Rad = Math.toRadians(point2.getLat());
        double deltaLatRad = Math.toRadians(
                point2.getLat() - point1.getLat()
        );
        double deltaLonRad = Math.toRadians(
                point2.getLon() - point1.getLon()
        );
        
        double a = Math.sin(deltaLatRad / 2) * 
                   Math.sin(deltaLatRad / 2) +
                   Math.cos(lat1Rad) * 
                   Math.cos(lat2Rad) *
                   Math.sin(deltaLonRad / 2) * 
                   Math.sin(deltaLonRad / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM * c;
    }
}
