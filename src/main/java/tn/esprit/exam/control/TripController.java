package tn.esprit.exam.control;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
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
    public Trip startTrip(@PathVariable UUID userId, @RequestParam(required = false) String title) {
        return tripService.startTrip(userId, title);
    }

    @PatchMapping("/end/{tripId}")
    public Trip endTrip(@PathVariable UUID tripId) {
        return tripService.endTrip(tripId);
    }

    @GetMapping("/user/{userId}")
    public List<Trip> getTripsByUser(@PathVariable UUID userId) {
        return tripService.getTripsByUser(userId);
    }

    @GetMapping("/{tripId}")
    public Trip getTrip(@PathVariable UUID tripId) {
        return tripService.getTrip(tripId);
    }
}
