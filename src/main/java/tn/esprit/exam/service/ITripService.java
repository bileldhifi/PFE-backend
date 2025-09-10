package tn.esprit.exam.service;

import tn.esprit.exam.entity.Trip;

import java.util.List;
import java.util.UUID;

public interface ITripService {
    Trip startTrip(UUID userId, String title);
    Trip endTrip(UUID tripId);
    List<Trip> getTripsByUser(UUID userId);
    Trip getTrip(UUID tripId);
}
