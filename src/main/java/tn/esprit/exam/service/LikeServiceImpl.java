package tn.esprit.exam.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.exam.dto.LikeResponse;
import tn.esprit.exam.dto.PostLikeUpdate;
import tn.esprit.exam.entity.Like;
import tn.esprit.exam.entity.Post;
import tn.esprit.exam.entity.User;
import tn.esprit.exam.repository.LikeRepository;
import tn.esprit.exam.repository.PostRepository;
import tn.esprit.exam.repository.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeServiceImpl implements ILikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public PostLikeUpdate likePost(UUID postId, UUID userId) {
        log.info("User {} liking post {}", userId, postId);
        
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (likeRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new IllegalArgumentException("Post already liked");
        }
        
        Like like = new Like();
        like.setPost(post);
        like.setUser(user);
        
        likeRepository.save(like);
        
        long likesCount = likeRepository.countByPostId(postId);
        
        PostLikeUpdate update = new PostLikeUpdate(
                postId,
                userId,
                user.getUsername(),
                true,
                likesCount
        );
        
        sendLikeUpdate(update);
        
        return update;
    }

    @Override
    @Transactional
    public PostLikeUpdate unlikePost(UUID postId, UUID userId) {
        log.info("User {} unliking post {}", userId, postId);
        
        likeRepository.deleteByPostIdAndUserId(postId, userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        long likesCount = likeRepository.countByPostId(postId);
        
        PostLikeUpdate update = new PostLikeUpdate(
                postId,
                userId,
                user.getUsername(),
                false,
                likesCount
        );
        
        sendLikeUpdate(update);
        
        return update;
    }

    @Override
    public boolean isLiked(UUID postId, UUID userId) {
        return likeRepository.existsByPostIdAndUserId(postId, userId);
    }

    @Override
    public long getLikesCount(UUID postId) {
        return likeRepository.countByPostId(postId);
    }

    @Override
    public List<LikeResponse> getLikesByPost(UUID postId) {
        return likeRepository.findAll().stream()
                .filter(like -> like.getPost().getId().equals(postId))
                .map(like -> new LikeResponse(
                        like.getId(),
                        like.getPost().getId(),
                        like.getUser().getId(),
                        like.getUser().getUsername(),
                        like.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    private void sendLikeUpdate(PostLikeUpdate update) {
        messagingTemplate.convertAndSend(
                "/topic/posts/" + update.postId() + "/likes",
                update
        );
    }
}

