package tn.esprit.exam.control;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.exam.dto.PostResponse;
import tn.esprit.exam.entity.Visibility;
import tn.esprit.exam.service.IPostService;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for post operations
 * Follows RESTful API design patterns
 */
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final IPostService postService;

    /**
     * Create a new post with media attachments
     * Uses multipart form-data to handle images
     * Authenticated user is obtained from Security context
     *
     * @param tripId Trip identifier
     * @param trackPointId Optional track point identifier
     * @param latitude Location latitude
     * @param longitude Location longitude
     * @param text Post caption/text content
     * @param visibility Post visibility (PUBLIC/PRIVATE)
     * @param images Array of image files to upload
     * @param auth Spring Security authentication object
     * @return Created post response with media URLs
     * @throws IOException if file upload fails
     */
    @PostMapping(
            value = "/{tripId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public PostResponse createPost(
            @PathVariable UUID tripId,
            @RequestParam(required = false) Long trackPointId,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false) String text,
            @RequestParam Visibility visibility,
            @RequestPart(value = "images", required = false) 
                MultipartFile[] images,
            Authentication auth
    ) throws IOException {
        log.info("Creating post for trip: {} by user: {}", 
                tripId, 
                auth.getName());
        
        return postService.createPostWithMedia(
                tripId,
                auth.getName(), // Get email from authentication
                trackPointId,
                latitude,
                longitude,
                text,
                visibility,
                images
        );
    }

    /**
     * Get all posts for a specific trip
     *
     * @param tripId Trip identifier
     * @return List of posts with media
     */
    @GetMapping("/trip/{tripId}")
    public List<PostResponse> getPostsByTrip(
            @PathVariable UUID tripId
    ) {
        log.info("Fetching posts for trip: {}", tripId);
        return postService.getPostsByTrip(tripId);
    }

    /**
     * Search public posts by location
     *
     * @param country Optional country filter
     * @param city Optional city filter
     * @return List of public posts matching criteria
     */
    @GetMapping("/public")
    public List<PostResponse> searchPublicPosts(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String city
    ) {
        log.info("Searching public posts - country: {}, city: {}", 
                country, 
                city);
        return postService.searchPublicPosts(country, city);
    }

    /**
     * Get post by ID
     *
     * @param postId Post identifier
     * @return Post response with media
     */
    @GetMapping("/{postId}")
    public PostResponse getPostById(@PathVariable UUID postId) {
        log.info("Fetching post: {}", postId);
        return postService.getPostById(postId);
    }
}
