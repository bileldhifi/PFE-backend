package tn.esprit.exam.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.exam.entity.*;
import tn.esprit.exam.repository.PostRepository;
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

    @Override
    public Post addPost(UUID tripId, UUID userId, Post post) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        post.setTrip(trip);
        post.setUser(user);
        post.setTs(OffsetDateTime.now());

        return postRepository.save(post);
    }

    @Override
    public List<Post> getPostsByTrip(UUID tripId) {
        return postRepository.findByTripId(tripId);
    }

    @Override
    public List<Post> searchPublicPosts(String country, String city) {
        return postRepository.findByVisibilityAndCountryAndCity(Visibility.PUBLIC, country, city);
    }
}
