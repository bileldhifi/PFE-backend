package tn.esprit.exam.dto;

import tn.esprit.exam.entity.Visibility;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PostResponse(   UUID id,
                              String text,
                              Visibility visibility,
                              OffsetDateTime ts,
                              UUID tripId,
                              Long trackPointId,
                              String userEmail) {
}
