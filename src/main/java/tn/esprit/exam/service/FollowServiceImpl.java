package tn.esprit.exam.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.exam.dto.FollowResponse;
import tn.esprit.exam.entity.Follow;
import tn.esprit.exam.entity.User;
import tn.esprit.exam.repository.FollowRepository;
import tn.esprit.exam.repository.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowServiceImpl implements IFollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final INotificationService notificationService;

    @Override
    @Transactional
    public FollowResponse followUser(UUID followerId, UUID followingId) {
        log.info("User {} following user {}", followerId, followingId);
        
        if (followerId.equals(followingId)) {
            throw new IllegalArgumentException("Cannot follow yourself");
        }
        
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new RuntimeException("Follower not found"));
        
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new RuntimeException("Following user not found"));
        
        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new IllegalArgumentException("Already following this user");
        }
        
        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(following);
        
        Follow saved = followRepository.save(follow);
        
        updateFollowCounts(followerId, followingId);
        
        // Create notification for the user being followed
        notificationService.createNotification(
                followingId,
                followerId,
                tn.esprit.exam.entity.NotificationType.FOLLOW,
                null,
                null
        );
        
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void unfollowUser(UUID followerId, UUID followingId) {
        log.info("User {} unfollowing user {}", followerId, followingId);
        
        Follow follow = followRepository.findByFollowerAndFollowing(
                userRepository.findById(followerId)
                        .orElseThrow(() -> new RuntimeException("Follower not found")),
                userRepository.findById(followingId)
                        .orElseThrow(() -> new RuntimeException("Following user not found"))
        ).orElseThrow(() -> new RuntimeException("Follow relationship not found"));
        
        followRepository.delete(follow);
        
        updateFollowCounts(followerId, followingId);
    }

    @Override
    public boolean isFollowing(UUID followerId, UUID followingId) {
        return followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    @Override
    public List<FollowResponse> getFollowers(UUID userId) {
        return followRepository.findFollowersByFollowingId(userId).stream()
                .map(user -> new FollowResponse(
                        null,
                        user.getId(),
                        user.getUsername(),
                        userId,
                        null,
                        null
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<FollowResponse> getFollowing(UUID userId) {
        return followRepository.findFollowingByFollowerId(userId).stream()
                .map(user -> new FollowResponse(
                        null,
                        userId,
                        null,
                        user.getId(),
                        user.getUsername(),
                        null
                ))
                .collect(Collectors.toList());
    }

    @Override
    public long getFollowersCount(UUID userId) {
        return followRepository.countByFollowingId(userId);
    }

    @Override
    public long getFollowingCount(UUID userId) {
        return followRepository.countByFollowerId(userId);
    }

    private FollowResponse mapToResponse(Follow follow) {
        return new FollowResponse(
                follow.getId(),
                follow.getFollower().getId(),
                follow.getFollower().getUsername(),
                follow.getFollowing().getId(),
                follow.getFollowing().getUsername(),
                follow.getCreatedAt()
        );
    }

    @Transactional
    private void updateFollowCounts(UUID followerId, UUID followingId) {
        User follower = userRepository.findById(followerId).orElseThrow();
        User following = userRepository.findById(followingId).orElseThrow();
        
        follower.setFollowingCount((int) followRepository.countByFollowerId(followerId));
        following.setFollowersCount((int) followRepository.countByFollowingId(followingId));
        
        userRepository.save(follower);
        userRepository.save(following);
    }
}

