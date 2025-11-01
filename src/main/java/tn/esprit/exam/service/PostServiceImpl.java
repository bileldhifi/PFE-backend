package tn.esprit.exam.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.exam.dto.MediaResponse;
import tn.esprit.exam.dto.PostResponse;
import tn.esprit.exam.entity.*;
import tn.esprit.exam.repository.MediaRepository;
import tn.esprit.exam.repository.PostRepository;
import tn.esprit.exam.repository.TrackPointRepository;
import tn.esprit.exam.repository.TripRepository;
import tn.esprit.exam.repository.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service implementation for post operations
 * Uses constructor injection for better testability
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements IPostService {

    private final PostRepository postRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TrackPointRepository trackPointRepository;
    private final MediaRepository mediaRepository;

    private static final String UPLOAD_DIR = 
            System.getProperty("user.dir") + "/uploads/posts/";

    /**
     * Create a post with media attachments in a single transaction
     * Follows SOLID principles with single responsibility
     */
    @Override
    @Transactional
    public PostResponse createPostWithMedia(
            UUID tripId,
            String userEmail,
            Long trackPointId,
            Double latitude,
            Double longitude,
            String text,
            Visibility visibility,
            MultipartFile[] images
    ) throws IOException {
        log.info("Creating post for trip: {}, user: {}", tripId, userEmail);

        // Validate and fetch entities
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> {
                    log.error("Trip not found: {}", tripId);
                    return new RuntimeException("Trip not found");
                });

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userEmail);
                    return new RuntimeException("User not found");
                });

        // Create post entity
        Post post = new Post();
        post.setTrip(trip);
        post.setUser(user);
        post.setText(text);
        post.setVisibility(visibility);
        post.setTs(OffsetDateTime.now());

        // Set track point if provided
        if (trackPointId != null) {
            TrackPoint trackPoint = trackPointRepository
                    .findById(trackPointId)
                    .orElseThrow(() -> {
                        log.error("TrackPoint not found: {}", trackPointId);
                        return new RuntimeException("TrackPoint not found");
                    });
            post.setTrackPoint(trackPoint);
        }

        // Save post first to get ID for media association
        Post savedPost = postRepository.save(post);
        log.info("Post created with ID: {}", savedPost.getId());

        // Handle media uploads
        List<MediaResponse> mediaResponses = new ArrayList<>();
        if (images != null && images.length > 0) {
            mediaResponses = uploadPostImages(savedPost, images);
            log.info("Uploaded {} images for post: {}", 
                    images.length, 
                    savedPost.getId());
        }

        return mapToResponse(savedPost, latitude, longitude, mediaResponses);
    }

    /**
     * Upload images for a post
     * Private helper method following clean code principles
     */
    private List<MediaResponse> uploadPostImages(
            Post post, 
            MultipartFile[] images
    ) throws IOException {
        List<MediaResponse> mediaResponses = new ArrayList<>();

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("Created upload directory: {}", UPLOAD_DIR);
        }

        for (MultipartFile image : images) {
            if (image.isEmpty()) {
                continue;
            }

            // Validate file type
            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                log.warn("Skipping non-image file: {}", 
                        image.getOriginalFilename());
                continue;
            }

            // Generate unique filename
            String filename = UUID.randomUUID() + "_" + 
                    image.getOriginalFilename();
            Path filePath = uploadPath.resolve(filename);
            
            // Save file to disk
            image.transferTo(filePath.toFile());
            log.debug("Saved file: {}", filename);

            // Create media entity
            Media media = new Media();
            media.setPost(post);
            media.setType(MediaKind.PHOTO);
            media.setUrl("/uploads/posts/" + filename);
            media.setSizeBytes(image.getSize());

            Media savedMedia = mediaRepository.save(media);
            
            mediaResponses.add(new MediaResponse(
                    savedMedia.getId(),
                    savedMedia.getType(),
                    savedMedia.getUrl(),
                    savedMedia.getSizeBytes(),
                    savedMedia.getWidth(),
                    savedMedia.getHeight(),
                    savedMedia.getDurationS()
            ));
        }

        return mediaResponses;
    }

    @Override
    public List<PostResponse> getPostsByTrip(UUID tripId) {
        log.info("Fetching posts for trip: {}", tripId);
        
        return postRepository.findByTripId(tripId)
                .stream()
                .map(this::mapToResponseWithMedia)
                .toList();
    }

    @Override
    public List<PostResponse> searchPublicPosts(String country, String city) {
        log.info("Searching public posts - country: {}, city: {}", 
                country, 
                city);
        
        return postRepository
                .findByVisibilityAndCountryAndCity(
                        Visibility.PUBLIC, 
                        country, 
                        city
                )
                .stream()
                .map(this::mapToResponseWithMedia)
                .toList();
    }

    @Override
    public PostResponse getPostById(UUID postId) {
        log.info("Fetching post: {}", postId);
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.error("Post not found: {}", postId);
                    return new RuntimeException("Post not found");
                });
        
        return mapToResponseWithMedia(post);
    }

    /**
     * Map post entity to response DTO with media
     * Private helper method for code reusability
     */
    private PostResponse mapToResponseWithMedia(Post post) {
        List<MediaResponse> mediaResponses = post.getMedia()
                .stream()
                .map(m -> new MediaResponse(
                        m.getId(),
                        m.getType(),
                        m.getUrl(),
                        m.getSizeBytes(),
                        m.getWidth(),
                        m.getHeight(),
                        m.getDurationS()
                ))
                .toList();

        return new PostResponse(
                post.getId(),
                post.getText(),
                post.getVisibility(),
                post.getTs(),
                post.getTrip().getId(),
                post.getTrackPoint() != null ? 
                        post.getTrackPoint().getId() : null,
                post.getTrackPoint() != null ? 
                        post.getTrackPoint().getLat() : null,
                post.getTrackPoint() != null ? 
                        post.getTrackPoint().getLon() : null,
                post.getUser().getEmail(),
                post.getUser().getUsername(),
                mediaResponses
        );
    }

    /**
     * Map post to response with provided coordinates
     */
    private PostResponse mapToResponse(
            Post post, 
            Double latitude, 
            Double longitude, 
            List<MediaResponse> media
    ) {
        return new PostResponse(
                post.getId(),
                post.getText(),
                post.getVisibility(),
                post.getTs(),
                post.getTrip().getId(),
                post.getTrackPoint() != null ? 
                        post.getTrackPoint().getId() : null,
                latitude,
                longitude,
                post.getUser().getEmail(),
                post.getUser().getUsername(),
                media
        );
    }
}
