package tn.esprit.exam.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.exam.dto.TrackPointRequest;
import tn.esprit.exam.dto.TrackPointResponse;
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
    public List<TrackPointResponse> addTrackPoints(UUID tripId, List<TrackPointRequest> points) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        List<TrackPoint> entities = points.stream().map(dto -> {
            TrackPoint tp = new TrackPoint();
            tp.setTrip(trip);
            tp.setTs(dto.ts());
            tp.setLat(dto.lat());
            tp.setLon(dto.lon());
            tp.setAccuracyM(dto.accuracyM());
            tp.setSpeedMps(dto.speedMps());
            return tp;
        }).toList();

        List<TrackPoint> saved = trackPointRepository.saveAll(entities);

        return saved.stream()
                .map(tp -> new TrackPointResponse(
                        tp.getId(),
                        tp.getTs(),
                        tp.getLat(),
                        tp.getLon(),
                        tp.getAccuracyM(),
                        tp.getSpeedMps()
                ))
                .toList();
    }

    @Override
    public List<TrackPointResponse> getTrackPoints(UUID tripId) {
        return trackPointRepository.findByTripId(tripId)
                .stream()
                .map(tp -> new TrackPointResponse(
                        tp.getId(),
                        tp.getTs(),
                        tp.getLat(),
                        tp.getLon(),
                        tp.getAccuracyM(),
                        tp.getSpeedMps()
                ))
                .toList();
    }
}
