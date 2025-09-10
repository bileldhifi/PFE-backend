package tn.esprit.exam.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.exam.entity.Trip;
import tn.esprit.exam.entity.User;
import tn.esprit.exam.repository.TripRepository;
import tn.esprit.exam.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TripServiceImpl implements ITripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    @Override
    public Trip startTrip(UUID userId, String title) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Trip trip = new Trip();
        trip.setUser(user);
        trip.setTitle(title);
        trip.setStartedAt(OffsetDateTime.now());

        return tripRepository.save(trip);
    }

    @Override
    public Trip endTrip(UUID tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        trip.setEndedAt(OffsetDateTime.now());
        return tripRepository.save(trip);
    }

    @Override
    public List<Trip> getTripsByUser(UUID userId) {
        return tripRepository.findByUserId(userId);
    }

    @Override
    public Trip getTrip(UUID tripId) {
        return tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));
    }
}
