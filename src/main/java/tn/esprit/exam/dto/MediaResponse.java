package tn.esprit.exam.dto;

import tn.esprit.exam.entity.MediaKind;

import java.util.UUID;

public record MediaResponse(UUID id,
                            MediaKind type,
                            String url,
                            Long sizeBytes,
                            Integer width,
                            Integer height,
                            Integer durationS) {
}
