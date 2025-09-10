package tn.esprit.exam.service;

import tn.esprit.exam.entity.Post;

import java.util.List;
import java.util.UUID;

public interface IPostService {
    Post addPost(UUID tripId, UUID userId, Post post);
    List<Post> getPostsByTrip(UUID tripId);
    List<Post> searchPublicPosts(String country, String city);
}
