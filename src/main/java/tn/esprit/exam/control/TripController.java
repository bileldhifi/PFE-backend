package tn.esprit.exam.control;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import tn.esprit.exam.dto.TimelineResponse;
import tn.esprit.exam.dto.TripRequest;
import tn.esprit.exam.dto.TripResponse;
import tn.esprit.exam.service.ITripService;

import java.util.List;
import java.util.UUID;

@Tag(name = "Trip Web Service")
@RestController
@RequestMapping("/trips")
@RequiredArgsConstructor
@Slf4j
public class TripController {

    private final ITripService tripService;

    @PostMapping("/start/{userId}")
    public TripResponse startTrip(@PathVariable UUID userId,
                                  @RequestBody TripRequest request) {
        return tripService.startTrip(userId, request);
    }

    @PatchMapping("/end/{tripId}")
    public TripResponse endTrip(@PathVariable UUID tripId) {
        return tripService.endTrip(tripId);
    }

    @GetMapping("/user/{userId}")
    public List<TripResponse> getTripsByUser(@PathVariable UUID userId) {
        return tripService.getTripsByUser(userId);
    }

    @GetMapping("/{tripId}")
    public TripResponse getTrip(@PathVariable UUID tripId) {
        return tripService.getTrip(tripId);
    }

    @DeleteMapping("/{tripId}")
    public void deleteTrip(@PathVariable UUID tripId) {
        tripService.deleteTrip(tripId);
    }

    @GetMapping("/{tripId}/timeline")
    public TimelineResponse getTimeline(@PathVariable UUID tripId) {
        log.info(
                "Fetching timeline for trip: {}", 
                tripId
        );
        return tripService.getTimeline(tripId);
    }
}
