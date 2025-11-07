package tn.esprit.exam.service;

import tn.esprit.exam.dto.FollowResponse;

import java.util.List;
import java.util.UUID;

public interface IFollowService {
    
    FollowResponse followUser(UUID followerId, UUID followingId);
    
    void unfollowUser(UUID followerId, UUID followingId);
    
    boolean isFollowing(UUID followerId, UUID followingId);
    
    List<FollowResponse> getFollowers(UUID userId);
    
    List<FollowResponse> getFollowing(UUID userId);
    
    long getFollowersCount(UUID userId);
    
    long getFollowingCount(UUID userId);
}

