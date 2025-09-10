package tn.esprit.exam.control;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
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
    public Post addPost(@PathVariable UUID tripId, @PathVariable UUID userId, @RequestBody Post post) {
        return postService.addPost(tripId, userId, post);
    }

    @GetMapping("/trip/{tripId}")
    public List<Post> getPostsByTrip(@PathVariable UUID tripId) {
        return postService.getPostsByTrip(tripId);
    }

    @GetMapping("/public")
    public List<Post> searchPublicPosts(@RequestParam(required = false) String country,
                                        @RequestParam(required = false) String city) {
        return postService.searchPublicPosts(country, city);
    }
}
