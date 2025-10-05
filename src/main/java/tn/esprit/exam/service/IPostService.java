package tn.esprit.exam.service;

import tn.esprit.exam.dto.PostRequest;
import tn.esprit.exam.dto.PostResponse;
import tn.esprit.exam.entity.Post;

import java.util.List;
import java.util.UUID;

public interface IPostService {
    PostResponse addPost(UUID tripId, UUID userId, PostRequest request);
    List<PostResponse> getPostsByTrip(UUID tripId);
    List<PostResponse> searchPublicPosts(String country, String city);
}

