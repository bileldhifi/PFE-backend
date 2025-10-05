package tn.esprit.exam.service;

import tn.esprit.exam.dto.TrackPointRequest;
import tn.esprit.exam.dto.TrackPointResponse;
import tn.esprit.exam.entity.TrackPoint;

import java.util.List;
import java.util.UUID;

public interface ITrackPointService {
    List<TrackPointResponse> addTrackPoints(UUID tripId, List<TrackPointRequest> points);
    List<TrackPointResponse> getTrackPoints(UUID tripId);}
