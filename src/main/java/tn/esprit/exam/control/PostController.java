package tn.esprit.exam.control;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.exam.dto.PostRequest;
import tn.esprit.exam.dto.PostResponse;
import tn.esprit.exam.entity.Post;
import tn.esprit.exam.service.IPostService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final IPostService postService;

    @PostMapping("/{tripId}/{userId}")
    public PostResponse addPost(@PathVariable UUID tripId,
                                @PathVariable UUID userId,
                                @RequestBody PostRequest request) {
        return postService.addPost(tripId, userId, request);
    }


    @GetMapping("/trip/{tripId}")
    public List<PostResponse> getPostsByTrip(@PathVariable UUID tripId) {
        return postService.getPostsByTrip(tripId);
    }

    @GetMapping("/public")
    public List<PostResponse> searchPublicPosts(@RequestParam(required = false) String country,
                                        @RequestParam(required = false) String city) {
        return postService.searchPublicPosts(country, city);
    }
}
