package tn.esprit.exam.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.exam.dto.CommentRequest;
import tn.esprit.exam.dto.CommentResponse;
import tn.esprit.exam.dto.PostCommentUpdate;
import tn.esprit.exam.entity.Comment;
import tn.esprit.exam.entity.Post;
import tn.esprit.exam.entity.User;
import tn.esprit.exam.repository.CommentRepository;
import tn.esprit.exam.repository.PostRepository;
import tn.esprit.exam.repository.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements ICommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public PostCommentUpdate addComment(CommentRequest request, UUID userId) {
        log.info("User {} adding comment to post {}", userId, request.postId());
        
        Post post = postRepository.findById(request.postId())
                .orElseThrow(() -> new RuntimeException("Post not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(request.content());
        
        Comment saved = commentRepository.save(comment);
        
        long commentsCount = commentRepository.countByPostId(request.postId());
        
        PostCommentUpdate update = new PostCommentUpdate(
                request.postId(),
                saved.getId(),
                userId,
                user.getUsername(),
                saved.getContent(),
                commentsCount
        );
        
        sendCommentUpdate(update);
        
        return update;
    }

    @Override
    @Transactional
    public void deleteComment(UUID commentId, UUID userId) {
        log.info("User {} deleting comment {}", userId, commentId);
        
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        
        if (!comment.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to delete this comment");
        }
        
        UUID postId = comment.getPost().getId();
        commentRepository.delete(comment);
        
        long commentsCount = commentRepository.countByPostId(postId);
        
        PostCommentUpdate update = new PostCommentUpdate(
                postId,
                commentId,
                userId,
                null,
                null,
                commentsCount
        );
        
        sendCommentUpdate(update);
    }

    @Override
    public List<CommentResponse> getCommentsByPost(UUID postId) {
        return commentRepository.findAllByPostIdOrderByCreatedAt(postId).stream()
                .map(comment -> new CommentResponse(
                        comment.getId(),
                        comment.getPost().getId(),
                        comment.getUser().getId(),
                        comment.getUser().getUsername(),
                        comment.getContent(),
                        comment.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public long getCommentsCount(UUID postId) {
        return commentRepository.countByPostId(postId);
    }

    private void sendCommentUpdate(PostCommentUpdate update) {
        messagingTemplate.convertAndSend(
                "/topic/posts/" + update.postId() + "/comments",
                update
        );
    }
}

