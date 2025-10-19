package tn.esprit.exam.dto;

import tn.esprit.exam.entity.Visibility;

public record PostRequest(   Long trackPointId,
                             String text,
                             Visibility visibility) {
}
