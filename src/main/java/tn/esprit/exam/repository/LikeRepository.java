package tn.esprit.exam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.exam.entity.Like;
import tn.esprit.exam.entity.Post;
import tn.esprit.exam.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface LikeRepository extends JpaRepository<Like, UUID> {
    
    Optional<Like> findByPostAndUser(Post post, User user);
    
    boolean existsByPostIdAndUserId(UUID postId, UUID userId);
    
    long countByPostId(UUID postId);
    
    void deleteByPostIdAndUserId(UUID postId, UUID userId);
}

