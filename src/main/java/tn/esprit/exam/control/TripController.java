package tn.esprit.exam.control;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.exam.dto.TripRequest;
import tn.esprit.exam.dto.TripResponse;
import tn.esprit.exam.entity.Trip;
import tn.esprit.exam.service.ITripService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/trips")
@RequiredArgsConstructor
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
}
