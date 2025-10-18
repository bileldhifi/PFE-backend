package tn.esprit.exam.service;

import tn.esprit.exam.dto.TripRequest;
import tn.esprit.exam.dto.TripResponse;
import tn.esprit.exam.entity.Trip;

import java.util.List;
import java.util.UUID;

public interface ITripService {
    TripResponse startTrip(UUID userId, TripRequest request);
    TripResponse endTrip(UUID tripId);
    List<TripResponse> getTripsByUser(UUID userId);
    TripResponse getTrip(UUID tripId);
    void deleteTrip(UUID tripId);
}
