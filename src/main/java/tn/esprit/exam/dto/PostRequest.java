package tn.esprit.exam.dto;

import tn.esprit.exam.entity.Visibility;

import java.util.UUID;

public record PostRequest(   Long trackPointId,
                             String text,
                             Visibility visibility) {
}
