package tn.esprit.exam.control;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.exam.dto.UserRequest;
import tn.esprit.exam.dto.UserResponse;
import tn.esprit.exam.entity.User;
import tn.esprit.exam.service.IUserService;

import java.util.List;
import java.util.UUID;

@Tag(name = "User Web Service")
@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

    IUserService userService;

    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.retrieveAllUsers();
    }

    @GetMapping("/{userId}")
    public UserResponse getUser(@PathVariable UUID userId) {
        return userService.retrieveUser(userId);
    }

    @PostMapping("/add")
    public UserResponse addUser(@RequestBody UserRequest request) {
        return userService.addUser(request);
    }

    @PutMapping("/{userId}")
    public UserResponse updateUser(@PathVariable UUID userId, @RequestBody UserRequest request) {
        return userService.modifyUser(userId, request);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable UUID userId) {
        userService.removeUser(userId);
    }
}
