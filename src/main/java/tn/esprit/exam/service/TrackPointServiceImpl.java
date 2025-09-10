package tn.esprit.exam.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.exam.entity.TrackPoint;
import tn.esprit.exam.entity.Trip;
import tn.esprit.exam.repository.TrackPointRepository;
import tn.esprit.exam.repository.TripRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrackPointServiceImpl implements ITrackPointService {

    private final TrackPointRepository trackPointRepository;
    private final TripRepository tripRepository;

    @Override
    public List<TrackPoint> addTrackPoints(UUID tripId, List<TrackPoint> points) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        points.forEach(p -> p.setTrip(trip));

        return trackPointRepository.saveAll(points);
    }

    @Override
    public List<TrackPoint> getTrackPoints(UUID tripId) {
        return trackPointRepository.findByTripId(tripId);
    }
}
