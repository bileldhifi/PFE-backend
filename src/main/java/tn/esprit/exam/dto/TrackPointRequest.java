package tn.esprit.exam.dto;

import java.time.OffsetDateTime;

public record TrackPointRequest(OffsetDateTime ts,
                                double lat,
                                double lon,
                                Double accuracyM,
                                Double speedMps) {
}
