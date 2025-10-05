package tn.esprit.exam.dto;

import java.time.OffsetDateTime;

public record TrackPointResponse(Long id,
                                 OffsetDateTime ts,
                                 double lat,
                                 double lon,
                                 Double accuracyM,
                                 Double speedMps) {
}
