package tn.esprit.exam.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for creating track points
 * Contains validation annotations for data integrity
 */
public record TrackPointRequest(
    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    Double lat,
    
    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    Double lon,
    
    @DecimalMin(value = "0.0", message = "Accuracy must be positive")
    Double accuracyM,
    
    @DecimalMin(value = "0.0", message = "Speed must be positive")
    Double speedMps,
    
    String locationName
) {
    /**
     * Validates that the coordinates are within reasonable bounds
     */
    public boolean isValidLocation() {
        return lat != null && lon != null && 
               lat >= -90.0 && lat <= 90.0 && 
               lon >= -180.0 && lon <= 180.0;
    }
    
    /**
     * Calculates distance to another point using Haversine formula
     */
    public double distanceTo(TrackPointRequest other) {
        if (!isValidLocation() || !other.isValidLocation()) {
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
}