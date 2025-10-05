package tn.esprit.exam.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.exam.dto.PostRequest;
import tn.esprit.exam.dto.PostResponse;
import tn.esprit.exam.entity.*;
import tn.esprit.exam.repository.PostRepository;
import tn.esprit.exam.repository.TrackPointRepository;
import tn.esprit.exam.repository.TripRepository;
import tn.esprit.exam.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements IPostService {

    private final PostRepository postRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TrackPointRepository trackPointRepository;


    @Override
    public PostResponse addPost(UUID tripId, UUID userId, PostRequest request) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        TrackPoint tp = trackPointRepository.findById(request.trackPointId())
                .orElseThrow(() -> new RuntimeException("TrackPoint not found"));

        Post post = new Post();
        post.setTrip(trip);
        post.setUser(user);
        post.setTrackPoint(tp);
        post.setText(request.text());
        post.setVisibility(request.visibility());
        post.setTs(OffsetDateTime.now());

        Post saved = postRepository.save(post);

        return new PostResponse(saved.getId(), saved.getText(), saved.getVisibility(),
                saved.getTs(), trip.getId(), tp.getId(), user.getEmail());
    }



    @Override
    public List<PostResponse> getPostsByTrip(UUID tripId) {
        return postRepository.findByTripId(tripId)
                .stream()
                .map(p -> new PostResponse(
                        p.getId(),
                        p.getText(),
                        p.getVisibility(),
                        p.getTs(),
                        p.getTrip().getId(),
                        p.getTrackPoint() != null ? p.getTrackPoint().getId() : null,
                        p.getUser().getEmail()
                ))
                .toList();
    }

    @Override
    public List<PostResponse> searchPublicPosts(String country, String city) {
        return postRepository.findByVisibilityAndCountryAndCity(Visibility.PUBLIC, country, city)
                .stream()
                .map(p -> new PostResponse(
                        p.getId(),
                        p.getText(),
                        p.getVisibility(),
                        p.getTs(),
                        p.getTrip().getId(),
                        p.getTrackPoint() != null ? p.getTrackPoint().getId() : null,
                        p.getUser().getEmail()
                ))
                .toList();
    }

}
