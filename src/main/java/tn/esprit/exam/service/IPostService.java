package tn.esprit.exam.service;

import org.springframework.web.multipart.MultipartFile;
import tn.esprit.exam.dto.PostResponse;
import tn.esprit.exam.entity.Visibility;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for post operations
 */
public interface IPostService {
    
    /**
     * Create a post with media attachments
     *
     * @param tripId Trip identifier
     * @param userEmail User email from authentication
     * @param trackPointId Optional track point identifier
     * @param latitude Location latitude
     * @param longitude Location longitude
     * @param text Post caption/text
     * @param visibility Post visibility
     * @param images Array of image files
     * @return Created post response
     * @throws IOException if file upload fails
     */
    PostResponse createPostWithMedia(
            UUID tripId,
            String userEmail,
            Long trackPointId,
            Double latitude,
            Double longitude,
            String text,
            Visibility visibility,
            MultipartFile[] images
    ) throws IOException;

    /**
     * Get all posts for a trip
     *
     * @param tripId Trip identifier
     * @return List of posts
     */
    List<PostResponse> getPostsByTrip(UUID tripId);

    /**
     * Search public posts by location
     *
     * @param country Optional country filter
     * @param city Optional city filter
     * @return List of public posts
     */
    List<PostResponse> searchPublicPosts(String country, String city);

    /**
     * Get post by ID
     *
     * @param postId Post identifier
     * @return Post response
     */
    PostResponse getPostById(UUID postId);
}
