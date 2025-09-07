package tn.esprit.exam.control;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping("/all")
    public List<User> getAllUsers() {
        return userService.retrieveAllUsers();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable UUID id) {
        return userService.retrieveUser(id);
    }

    @PostMapping("/add")
    public User addUser(@RequestBody User user) {
        return userService.addUser(user);
    }

    @PutMapping("/update")
    public User updateUser(@RequestBody User user) {
        return userService.modifyUser(user);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteUser(@PathVariable UUID id) {
        userService.removeUser(id);
    }
}
