package tn.esprit.exam.service;

import tn.esprit.exam.dto.CommentRequest;
import tn.esprit.exam.dto.CommentResponse;
import tn.esprit.exam.dto.PostCommentUpdate;

import java.util.List;
import java.util.UUID;

public interface ICommentService {
    
    PostCommentUpdate addComment(CommentRequest request, UUID userId);
    
    void deleteComment(UUID commentId, UUID userId);
    
    List<CommentResponse> getCommentsByPost(UUID postId);
    
    long getCommentsCount(UUID postId);
}

