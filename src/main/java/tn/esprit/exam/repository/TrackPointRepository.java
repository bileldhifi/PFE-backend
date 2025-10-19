package tn.esprit.exam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.exam.entity.TrackPoint;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for TrackPoint operations
 * Provides optimized queries for location tracking
 */
public interface TrackPointRepository extends JpaRepository<TrackPoint, Long> {
    
    /**
     * Find all track points for a trip ordered by timestamp
     */
    List<TrackPoint> findByTripIdOrderByTsAsc(UUID tripId);
    
    /**
     * Find track points for a trip within a time range
     */
    List<TrackPoint> findByTripIdAndTsBetweenOrderByTsAsc(
        UUID tripId, 
        OffsetDateTime startTime, 
        OffsetDateTime endTime
    );
    
    /**
     * Find the latest track point for a trip
     */
    Optional<TrackPoint> findTopByTripIdOrderByTsDesc(UUID tripId);
    
    /**
     * Count track points for a trip
     */
    long countByTripId(UUID tripId);
    
    /**
     * Find track points within a geographic bounding box
     * Useful for map-based queries
     */
    @Query("SELECT tp FROM TrackPoint tp WHERE tp.trip.id = :tripId " +
           "AND tp.lat BETWEEN :minLat AND :maxLat " +
           "AND tp.lon BETWEEN :minLon AND :maxLon " +
           "ORDER BY tp.ts ASC")
    List<TrackPoint> findByTripIdAndBoundingBox(
        @Param("tripId") UUID tripId,
        @Param("minLat") Double minLat,
        @Param("maxLat") Double maxLat,
        @Param("minLon") Double minLon,
        @Param("maxLon") Double maxLon
    );
    
    /**
     * Delete all track points for a trip
     * Useful for trip cleanup
     */
    void deleteByTripId(UUID tripId);
    
    /**
     * Find track points with high accuracy (good GPS signal)
     */
    @Query("SELECT tp FROM TrackPoint tp WHERE tp.trip.id = :tripId " +
           "AND tp.accuracyM <= :maxAccuracy " +
           "ORDER BY tp.ts ASC")
    List<TrackPoint> findByTripIdAndHighAccuracy(
        @Param("tripId") UUID tripId,
        @Param("maxAccuracy") Double maxAccuracy
    );
}
