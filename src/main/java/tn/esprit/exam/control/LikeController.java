package tn.esprit.exam.control;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.exam.dto.LikeResponse;
import tn.esprit.exam.dto.PostLikeUpdate;
import tn.esprit.exam.service.ILikeService;
import tn.esprit.exam.service.IUserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Slf4j
public class LikeController {

    private final ILikeService likeService;
    private final IUserService userService;

    @PostMapping("/{postId}/like")
    public ResponseEntity<PostLikeUpdate> likePost(
            @PathVariable UUID postId,
            Authentication auth
    ) {
        UUID userId = userService.getUserIdByEmail(auth.getName());
        log.info("User {} liking post {}", userId, postId);
        
        PostLikeUpdate update = likeService.likePost(postId, userId);
        return ResponseEntity.ok(update);
    }

    @DeleteMapping("/{postId}/like")
    public ResponseEntity<PostLikeUpdate> unlikePost(
            @PathVariable UUID postId,
            Authentication auth
    ) {
        UUID userId = userService.getUserIdByEmail(auth.getName());
        log.info("User {} unliking post {}", userId, postId);
        
        PostLikeUpdate update = likeService.unlikePost(postId, userId);
        return ResponseEntity.ok(update);
    }

    @GetMapping("/{postId}/likes")
    public ResponseEntity<List<LikeResponse>> getLikes(
            @PathVariable UUID postId
    ) {
        log.info("Fetching likes for post {}", postId);
        return ResponseEntity.ok(likeService.getLikesByPost(postId));
    }

    @GetMapping("/{postId}/like-status")
    public ResponseEntity<Boolean> isLiked(
            @PathVariable UUID postId,
            Authentication auth
    ) {
        UUID userId = userService.getUserIdByEmail(auth.getName());
        log.info("Checking if user {} liked post {}", userId, postId);
        
        boolean isLiked = likeService.isLiked(postId, userId);
        return ResponseEntity.ok(isLiked);
    }
}

