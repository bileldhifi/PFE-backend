package tn.esprit.exam.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprit.exam.dto.*;
import tn.esprit.exam.entity.Media;
import tn.esprit.exam.entity.Post;
import tn.esprit.exam.entity.TrackPoint;
import tn.esprit.exam.entity.Trip;
import tn.esprit.exam.entity.User;
import tn.esprit.exam.repository.PostRepository;
import tn.esprit.exam.repository.TrackPointRepository;
import tn.esprit.exam.repository.TripRepository;
import tn.esprit.exam.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripServiceImpl implements ITripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TrackPointRepository trackPointRepository;
    private final PostRepository postRepository;
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

        return mapToTripResponse(saved);
    }

    @Override
    public TripResponse endTrip(UUID tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        trip.setEndedAt(OffsetDateTime.now());
        Trip updated = tripRepository.save(trip);

        return mapToTripResponse(updated);
    }

    @Override
    public List<TripResponse> getTripsByUser(UUID userId) {
        return tripRepository.findByUserId(userId)
                .stream()
                .map(this::mapToTripResponse)
                .toList();
    }

    @Override
    public TripResponse getTrip(UUID tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        return mapToTripResponse(trip);
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
            
            // Get location name with priority: TrackPoint > Post
            String locationName = trackPoint.getLocationName();
            
            // Fallback to post location name if track point doesn't have one
            if (locationName == null || locationName.isBlank()) {
                if (!posts.isEmpty()) {
                    PostResponse firstPost = posts.get(0);
                    if (firstPost.city() != null && !firstPost.city().isBlank()) {
                        if (firstPost.country() != null && 
                                !firstPost.country().isBlank()) {
                            locationName = firstPost.city() + ", " + 
                                    firstPost.country();
                        } else {
                            locationName = firstPost.city();
                        }
                    } else if (firstPost.country() != null && 
                            !firstPost.country().isBlank()) {
                        locationName = firstPost.country();
                    }
                }
            }
            
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
                    .locationName(locationName)
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
    
    /**
     * Map Trip entity to TripResponse with calculated statistics
     *
     * @param trip Trip entity
     * @return TripResponse with stats
     */
    private TripResponse mapToTripResponse(Trip trip) {
        System.out.println("========================================");
        System.out.println("🔥 CALCULATING STATS FOR TRIP: " + trip.getId());
        System.out.println("========================================");
        
        TripStatsDTO stats = calculateTripStats(trip);
        
        System.out.println("========================================");
        System.out.println("✅ STATS RESULT:");
        System.out.println("   Steps: " + stats.stepsCount());
        System.out.println("   Distance: " + stats.distanceKm() + " km");
        System.out.println("   Countries: " + stats.countriesCount());
        System.out.println("   Cities: " + stats.citiesCount());
        System.out.println("   Photos: " + stats.photosCount());
        System.out.println("   Transport methods: " + stats.transportMethods());
        System.out.println("========================================");
        
        return new TripResponse(
                trip.getId(),
                trip.getTitle(),
                trip.getStartedAt(),
                trip.getEndedAt(),
                trip.getUser().getEmail(),
                null, // coverUrl - can be added later
                "PUBLIC", // visibility - can be added later
                stats
        );
    }
    
    /**
     * Calculate comprehensive trip statistics
     *
     * @param trip Trip entity
     * @return TripStatsDTO with all calculated metrics
     */
    private TripStatsDTO calculateTripStats(Trip trip) {
        // Get all track points and posts for this trip
        List<TrackPoint> trackPoints = trackPointRepository
                .findByTripIdOrderByTsAsc(trip.getId());
        List<Post> posts = postRepository.findByTripId(trip.getId());
        
        System.out.println("📊 Found " + trackPoints.size() + " track points");
        System.out.println("📝 Found " + posts.size() + " posts");
        
        log.info("Calculating stats for trip {}: {} track points, {} posts", 
                trip.getId(), trackPoints.size(), posts.size());
        
        // Calculate total distance
        double totalDistance = calculateTotalDistance(trackPoints);
        System.out.println("📏 Calculated distance: " + totalDistance + " km");
        
        // Count unique countries and cities
        Set<String> countries = new HashSet<>();
        Set<String> cities = new HashSet<>();
        int photoCount = 0;
        
        for (Post post : posts) {
            System.out.println("🔍 Checking post: " + post.getId());
            System.out.println("   City: " + post.getCity());
            System.out.println("   Country: " + post.getCountry());
            System.out.println("   Media: " + (post.getMedia() != null ? post.getMedia().size() : "null"));
            
            if (post.getCountry() != null && !post.getCountry().isBlank()) {
                countries.add(post.getCountry());
            }
            if (post.getCity() != null && !post.getCity().isBlank()) {
                cities.add(post.getCity());
            }
            if (post.getMedia() != null) {
                log.debug("Post {} has {} media items", 
                        post.getId(), post.getMedia().size());
                photoCount += post.getMedia().size();
            }
        }
        
        System.out.println("🌍 Unique countries: " + countries);
        System.out.println("🏙️  Unique cities: " + cities);
        System.out.println("📷 Total photos: " + photoCount);
        
        log.info("Stats calculated - Distance: {}km, Photos: {}, Countries: {}, Cities: {}", 
                totalDistance, photoCount, countries.size(), cities.size());
        
        // Detect transport methods
        Map<String, Double> transportMethods = 
                detectTransportMethods(trackPoints);
        
        return new TripStatsDTO(
                posts.size(), // stepsCount
                totalDistance,
                countries.size(),
                cities.size(),
                photoCount,
                transportMethods
        );
    }
    
    /**
     * Calculate total distance traveled from track points
     *
     * @param trackPoints List of track points ordered by timestamp
     * @return Total distance in kilometers
     */
    private double calculateTotalDistance(List<TrackPoint> trackPoints) {
        if (trackPoints == null || trackPoints.size() < 2) {
            return 0.0;
        }
        
        double totalDistance = 0.0;
        for (int i = 1; i < trackPoints.size(); i++) {
            TrackPoint prev = trackPoints.get(i - 1);
            TrackPoint curr = trackPoints.get(i);
            totalDistance += calculateDistance(prev, curr);
        }
        
        return totalDistance;
    }
    
    /**
     * Detect transport methods based on speed from track points
     * Speed thresholds (in km/h):
     * - Walking: < 6 km/h
     * - Biking: 6-25 km/h
     * - Driving: 25-150 km/h
     * - Flying: > 150 km/h
     *
     * @param trackPoints List of track points with speed data
     * @return Map of transport method to distance covered
     */
    private Map<String, Double> detectTransportMethods(
            List<TrackPoint> trackPoints
    ) {
        Map<String, Double> methods = new HashMap<>();
        
        if (trackPoints == null || trackPoints.size() < 2) {
            return methods;
        }
        
        double walking = 0;
        double biking = 0;
        double driving = 0;
        double flying = 0;
        
        for (int i = 1; i < trackPoints.size(); i++) {
            TrackPoint prev = trackPoints.get(i - 1);
            TrackPoint curr = trackPoints.get(i);
            
            double distance = calculateDistance(prev, curr);
            
            // Convert speed from m/s to km/h (1 m/s = 3.6 km/h)
            if (curr.getSpeedMps() != null) {
                double speedKmh = curr.getSpeedMps() * 3.6;
                
                if (speedKmh < 6) {
                    walking += distance;
                } else if (speedKmh < 25) {
                    biking += distance;
                } else if (speedKmh < 150) {
                    driving += distance;
                } else {
                    flying += distance;
                }
            }
        }
        
        // Only include methods that were actually used
        if (walking > 0.1) methods.put("Walking", walking);
        if (biking > 0.1) methods.put("Biking", biking);
        if (driving > 0.1) methods.put("Driving", driving);
        if (flying > 0.1) methods.put("Flying", flying);
        
        return methods;
    }
}
