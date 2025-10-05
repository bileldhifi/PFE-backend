package tn.esprit.exam.control;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.exam.dto.TrackPointRequest;
import tn.esprit.exam.dto.TrackPointResponse;
import tn.esprit.exam.entity.TrackPoint;
import tn.esprit.exam.service.ITrackPointService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/trackpoints")
@RequiredArgsConstructor
public class TrackPointController {

    private final ITrackPointService trackPointService;

    @PostMapping("/{tripId}")
    public List<TrackPointResponse> addTrackPoints(@PathVariable UUID tripId,
                                                   @RequestBody List<TrackPointRequest> points) {
        return trackPointService.addTrackPoints(tripId, points);
    }

    @GetMapping("/{tripId}")
    public List<TrackPointResponse> getTrackPoints(@PathVariable UUID tripId) {
        return trackPointService.getTrackPoints(tripId);
    }
}
