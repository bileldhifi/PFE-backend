package tn.esprit.exam.control;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.exam.dto.UserRequest;
import tn.esprit.exam.dto.UserResponse;
import tn.esprit.exam.service.IUserService;
import tn.esprit.exam.repository.UserRepository;
import tn.esprit.exam.entity.User;

import java.util.List;
import java.util.UUID;

@Tag(name = "User Web Service")
@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

    IUserService userService;
    UserRepository userRepository;

    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.retrieveAllUsers();
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(Authentication auth) {
        String email = auth != null ? auth.getName() : null;
        if (email == null) {
            throw new RuntimeException("User not authenticated");
        }
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getUsername(),
            user.getRole().name(),
            user.getDefaultVisibility(),
            user.getCreatedAt(),
            user.getBio(),
            user.getAvatarUrl(),
            user.getTripsCount(),
            user.getStepsCount(),
            user.getFollowersCount(),
            user.getFollowingCount()
        );
    }

    @PostMapping("/add")
    public UserResponse addUser(@RequestBody UserRequest request) {
        return userService.addUser(request);
    }

    @PutMapping("/me")
    public UserResponse updateCurrentUser(Authentication auth, @RequestBody UserRequest request) {
        String email = auth != null ? auth.getName() : null;
        if (email == null) {
            throw new RuntimeException("User not authenticated");
        }
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        return userService.modifyUser(user.getId(), request);
    }

    @PutMapping("/{userId}")
    public UserResponse updateUser(@PathVariable UUID userId, @RequestBody UserRequest request) {
        return userService.modifyUser(userId, request);
    }

    @DeleteMapping("/me")
    public void deleteCurrentUser(Authentication auth) {
        String email = auth != null ? auth.getName() : null;
        if (email == null) {
            throw new RuntimeException("User not authenticated");
        }
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        userService.removeUser(user.getId());
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable UUID userId) {
        userService.removeUser(userId);
    }
}
