package tn.esprit.exam.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.exam.dto.TripRequest;
import tn.esprit.exam.dto.TripResponse;
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
    public TripResponse startTrip(UUID userId, TripRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Trip trip = new Trip();
        trip.setUser(user);
        trip.setTitle(request.title());
        trip.setStartedAt(OffsetDateTime.now());

        Trip saved = tripRepository.save(trip);

        return new TripResponse(
                saved.getId(),
                saved.getTitle(),
                saved.getStartedAt(),
                saved.getEndedAt(),
                saved.getUser().getEmail()
        );
    }

    @Override
    public TripResponse endTrip(UUID tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        trip.setEndedAt(OffsetDateTime.now());
        Trip updated = tripRepository.save(trip);

        return new TripResponse(
                updated.getId(),
                updated.getTitle(),
                updated.getStartedAt(),
                updated.getEndedAt(),
                updated.getUser().getEmail()
        );
    }

    @Override
    public List<TripResponse> getTripsByUser(UUID userId) {
        return tripRepository.findByUserId(userId)
                .stream()
                .map(t -> new TripResponse(
                        t.getId(),
                        t.getTitle(),
                        t.getStartedAt(),
                        t.getEndedAt(),
                        t.getUser().getEmail()
                ))
                .toList();
    }

    @Override
    public TripResponse getTrip(UUID tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        return new TripResponse(
                trip.getId(),
                trip.getTitle(),
                trip.getStartedAt(),
                trip.getEndedAt(),
                trip.getUser().getEmail()
        );
    }
}
