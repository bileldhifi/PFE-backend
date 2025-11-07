package tn.esprit.exam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.exam.entity.Post;
import tn.esprit.exam.entity.Visibility;

import java.util.List;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {
    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.trip.id = :tripId")
    List<Post> findByTripId(@Param("tripId") UUID tripId);
    
    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.visibility = :visibility " +
           "AND (:country IS NULL OR p.country = :country) " +
           "AND (:city IS NULL OR p.city = :city) " +
           "ORDER BY p.ts DESC")
    List<Post> findByVisibilityAndCountryAndCity(
            @Param("visibility") Visibility visibility, 
            @Param("country") String country, 
            @Param("city") String city
    );
    
    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.visibility = :visibility ORDER BY p.ts DESC")
    List<Post> findByVisibilityOrderByTsDesc(Visibility visibility);
    
    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.trackPoint.id = :trackPointId")
    List<Post> findByTrackPointId(@Param("trackPointId") Long trackPointId);
    
    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.user.id IN :userIds AND p.visibility = :visibility ORDER BY p.ts DESC")
    List<Post> findByUserIdInAndVisibilityOrderByTsDesc(
            @Param("userIds") List<UUID> userIds, 
            @Param("visibility") Visibility visibility
    );
}
