package tn.esprit.exam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.exam.entity.Post;
import tn.esprit.exam.entity.Visibility;

import java.util.List;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {
    List<Post> findByTripId(UUID tripId);
    List<Post> findByVisibilityAndCountryAndCity(
            Visibility visibility, 
            String country, 
            String city
    );
    List<Post> findByTrackPointId(Long trackPointId);
}
