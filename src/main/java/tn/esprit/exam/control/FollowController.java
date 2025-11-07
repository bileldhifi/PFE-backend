package tn.esprit.exam.control;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.exam.dto.FollowResponse;
import tn.esprit.exam.service.IFollowService;
import tn.esprit.exam.service.IUserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class FollowController {

    private final IFollowService followService;
    private final IUserService userService;

    @PostMapping("/{userId}/follow")
    public ResponseEntity<FollowResponse> followUser(
            @PathVariable UUID userId,
            Authentication auth
    ) {
        UUID currentUserId = userService.getUserIdByEmail(auth.getName());
        log.info("User {} following user {}", currentUserId, userId);
        
        FollowResponse response = followService.followUser(currentUserId, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}/follow")
    public ResponseEntity<Void> unfollowUser(
            @PathVariable UUID userId,
            Authentication auth
    ) {
        UUID currentUserId = userService.getUserIdByEmail(auth.getName());
        log.info("User {} unfollowing user {}", currentUserId, userId);
        
        followService.unfollowUser(currentUserId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<List<FollowResponse>> getFollowers(
            @PathVariable UUID userId
    ) {
        log.info("Fetching followers for user {}", userId);
        return ResponseEntity.ok(followService.getFollowers(userId));
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<List<FollowResponse>> getFollowing(
            @PathVariable UUID userId
    ) {
        log.info("Fetching following for user {}", userId);
        return ResponseEntity.ok(followService.getFollowing(userId));
    }

    @GetMapping("/{userId}/follow-status")
    public ResponseEntity<Boolean> isFollowing(
            @PathVariable UUID userId,
            Authentication auth
    ) {
        UUID currentUserId = userService.getUserIdByEmail(auth.getName());
        log.info("Checking if user {} follows user {}", currentUserId, userId);
        
        boolean isFollowing = followService.isFollowing(currentUserId, userId);
        return ResponseEntity.ok(isFollowing);
    }
}

