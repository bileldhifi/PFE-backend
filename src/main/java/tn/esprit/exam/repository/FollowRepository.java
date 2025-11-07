package tn.esprit.exam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.exam.entity.Follow;
import tn.esprit.exam.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, UUID> {
    
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);
    
    boolean existsByFollowerIdAndFollowingId(UUID followerId, UUID followingId);
    
    @Query("SELECT f.following FROM Follow f WHERE f.follower.id = :followerId")
    List<User> findFollowingByFollowerId(@Param("followerId") UUID followerId);
    
    @Query("SELECT f.follower FROM Follow f WHERE f.following.id = :followingId")
    List<User> findFollowersByFollowingId(@Param("followingId") UUID followingId);
    
    long countByFollowerId(UUID followerId);
    
    long countByFollowingId(UUID followingId);
}

