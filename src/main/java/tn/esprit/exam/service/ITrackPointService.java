package tn.esprit.exam.service;

import tn.esprit.exam.entity.TrackPoint;

import java.util.List;
import java.util.UUID;

public interface ITrackPointService {
    List<TrackPoint> addTrackPoints(UUID tripId, List<TrackPoint> points);
    List<TrackPoint> getTrackPoints(UUID tripId);
}
