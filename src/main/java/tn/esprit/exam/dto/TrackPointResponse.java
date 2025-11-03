package tn.esprit.exam.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import tn.esprit.exam.entity.TrackPoint;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO for returning track point data
 * Includes formatted timestamps and calculated fields
 * Extended with media info for Snapchat-style map display
 */
public record TrackPointResponse(
    Long id,
    UUID tripId,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    OffsetDateTime ts,
    
    Double lat,
    Double lon,
    Double accuracyM,
    Double speedMps,
    
    // Calculated fields for better UX
    Double speedKmh,
    String locationName,
    Boolean isSignificant, // True if this is a significant location change
    
    // Media fields for map display
    Integer mediaCount,
    String firstMediaUrl
) {
    
    /**
     * Factory method to create response from entity
     * Basic version without media info
     */
    public static TrackPointResponse fromEntity(TrackPoint trackPoint) {
        return new TrackPointResponse(
            trackPoint.getId(),
            trackPoint.getTrip().getId(),
            trackPoint.getTs(),
            trackPoint.getLat(),
            trackPoint.getLon(),
            trackPoint.getAccuracyM(),
            trackPoint.getSpeedMps(),
            trackPoint.getSpeedMps() != null ? trackPoint.getSpeedMps() * 3.6 : null, // Convert m/s to km/h
            trackPoint.getLocationName(), // Use location name from entity
            false, // Would be determined by business logic
            null, // Media count not included in basic version
            null  // First media URL not included in basic version
        );
    }
    
    /**
     * Factory method to create response from entity with media info
     * Used for Snapchat-style map display
     */
    public static TrackPointResponse fromEntityWithMedia(
            TrackPoint trackPoint, 
            Integer mediaCount, 
            String firstMediaUrl
    ) {
        return new TrackPointResponse(
            trackPoint.getId(),
            trackPoint.getTrip().getId(),
            trackPoint.getTs(),
            trackPoint.getLat(),
            trackPoint.getLon(),
            trackPoint.getAccuracyM(),
            trackPoint.getSpeedMps(),
            trackPoint.getSpeedMps() != null ? trackPoint.getSpeedMps() * 3.6 : null,
            trackPoint.getLocationName(), // Use location name from entity
            false, // Is significant
            mediaCount,
            firstMediaUrl
        );
    }
    
    /**
     * Calculates distance to another track point
     */
    public double distanceTo(TrackPointResponse other) {
        if (lat == null || lon == null || other.lat == null || other.lon == null) {
            return 0.0;
        }
        
        final int R = 6371; // Earth's radius in kilometers
        double latDistance = Math.toRadians(other.lat - this.lat);
        double lonDistance = Math.toRadians(other.lon - this.lon);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(this.lat)) * Math.cos(Math.toRadians(other.lat))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000; // Convert to meters
    }
    
    /**
     * Calculates time difference with another track point
     */
    public long timeDifferenceSeconds(TrackPointResponse other) {
        if (ts == null || other.ts == null) {
            return 0;
        }
        return Math.abs(java.time.Duration.between(ts, other.ts).getSeconds());
    }
}
