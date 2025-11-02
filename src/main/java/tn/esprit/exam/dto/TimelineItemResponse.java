package tn.esprit.exam.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Timeline item combining track point data with associated posts
 */
@Builder
public record TimelineItemResponse(
        // Track Point Data
        Long trackPointId,
        
        @JsonFormat(
                pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
        )
        OffsetDateTime timestamp,
        
        Double latitude,
        Double longitude,
        String locationName,
        Double speedKmh,
        Double accuracyMeters,
        Boolean isSignificant,
        
        // Calculated Data
        Double distanceFromPreviousKm,
        Long timeFromPreviousSeconds,
        
        // Associated Posts
        List<PostResponse> posts,
        Integer photoCount
) {
    /**
     * Check if this timeline item has media
     */
    public boolean hasMedia() {
        return photoCount != null && photoCount > 0;
    }
    
    /**
     * Get formatted location string
     */
    public String getFormattedLocation() {
        if (locationName != null && !locationName.isBlank()) {
            return locationName;
        }
        return String.format(
                "%.4f, %.4f", 
                latitude, 
                longitude
        );
    }
}

