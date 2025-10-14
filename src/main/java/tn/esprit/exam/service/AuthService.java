package tn.esprit.exam.service;

import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.esprit.exam.config.JwtService;
import tn.esprit.exam.dto.AuthResponse;
import tn.esprit.exam.dto.LoginRequest;
import tn.esprit.exam.dto.UserResponse;
import tn.esprit.exam.entity.ResetPasswordToken;
import tn.esprit.exam.entity.User;
import tn.esprit.exam.repository.ResetPasswordTokenRepository;
import tn.esprit.exam.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ResetPasswordTokenRepository tokenRepository;
    private final MailService mailService;


    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        var claims = new HashMap<String, Object>();
        claims.put("role", user.getRole().name());
        claims.put("uid", user.getId().toString());

        String token = jwtService.generateToken(user.getEmail(), claims);

        // ✅ Convert to UserResponse DTO
        UserResponse userDto = new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getRole().name(),
                user.getDefaultVisibility(),
                user.getCreatedAt()
        );

        return new AuthResponse(token, userDto);
    }


    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = UUID.randomUUID().toString();
        ResetPasswordToken resetToken = new ResetPasswordToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));
        tokenRepository.save(resetToken);

        String resetLink = "http://localhost:8089/app-backend/auth/reset-password?token=" + token;

        String html = "<h2>Password Reset Request</h2>"
                + "<p>Hello " + user.getUsername() + ",</p>"
                + "<p>You requested to reset your password. Click the button below:</p>"
                + "<p><a href='" + resetLink + "' style='display:inline-block;padding:10px 20px;"
                + "background:#007bff;color:#fff;text-decoration:none;border-radius:5px;'>"
                + "Reset Password</a></p>"
                + "<p>This link will expire in 15 minutes.</p>"
                + "<p>If you did not request this, please ignore this email.</p>";

        try {
            mailService.sendHtmlEmail(user.getEmail(), "Password Reset Request", html);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send password reset email", e);
        }


        return "Reset password email sent!";
    }

    public void resetPassword(String token, String newPassword) {
        ResetPasswordToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.delete(resetToken); // one-time use
    }
}
