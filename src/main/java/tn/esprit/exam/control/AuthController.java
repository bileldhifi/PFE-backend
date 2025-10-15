package tn.esprit.exam.control;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.exam.dto.AuthResponse;
import tn.esprit.exam.dto.ChangePasswordRequest;
import tn.esprit.exam.dto.LoginRequest;
import tn.esprit.exam.dto.UserRequest;
import tn.esprit.exam.dto.UserResponse;
import tn.esprit.exam.service.AuthService;
import tn.esprit.exam.service.IUserService;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final IUserService userService;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    public UserResponse register(@RequestBody UserRequest request) {
        return userService.addUser(request);
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email) {
        return authService.forgotPassword(email);
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,
                                @RequestParam String newPassword) {
        authService.resetPassword(token, newPassword);
        return "Password reset successful!";
    }

    @PutMapping("/change-password")
    public String changePassword(@RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return "Password changed successfully!";
    }

    @PostMapping("/refresh")
    public AuthResponse refreshToken(@RequestParam String refreshToken) {
        return authService.refreshToken(refreshToken);
    }
}
