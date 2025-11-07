package tn.esprit.exam.service;

import tn.esprit.exam.dto.LikeResponse;
import tn.esprit.exam.dto.PostLikeUpdate;

import java.util.List;
import java.util.UUID;

public interface ILikeService {
    
    PostLikeUpdate likePost(UUID postId, UUID userId);
    
    PostLikeUpdate unlikePost(UUID postId, UUID userId);
    
    boolean isLiked(UUID postId, UUID userId);
    
    long getLikesCount(UUID postId);
    
    List<LikeResponse> getLikesByPost(UUID postId);
}

