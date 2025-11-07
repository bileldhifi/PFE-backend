package tn.esprit.exam.control;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.exam.dto.CommentRequest;
import tn.esprit.exam.dto.CommentResponse;
import tn.esprit.exam.dto.PostCommentUpdate;
import tn.esprit.exam.service.ICommentService;
import tn.esprit.exam.service.IUserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final ICommentService commentService;
    private final IUserService userService;

    @PostMapping("/{postId}/comments")
    public ResponseEntity<PostCommentUpdate> addComment(
            @PathVariable UUID postId,
            @Valid @RequestBody CommentRequest request,
            Authentication auth
    ) {
        UUID userId = userService.getUserIdByEmail(auth.getName());
        log.info("User {} adding comment to post {}", userId, postId);
        
        CommentRequest commentRequest = new CommentRequest(postId, request.content());
        PostCommentUpdate update = commentService.addComment(commentRequest, userId);
        return ResponseEntity.ok(update);
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID commentId,
            Authentication auth
    ) {
        UUID userId = userService.getUserIdByEmail(auth.getName());
        log.info("User {} deleting comment {}", userId, commentId);
        
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable UUID postId
    ) {
        log.info("Fetching comments for post {}", postId);
        return ResponseEntity.ok(commentService.getCommentsByPost(postId));
    }
}

