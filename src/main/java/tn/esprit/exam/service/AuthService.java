package tn.esprit.exam.service;

import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.esprit.exam.config.JwtService;
import tn.esprit.exam.dto.AuthResponse;
import tn.esprit.exam.dto.LoginRequest;
import tn.esprit.exam.entity.User;
import tn.esprit.exam.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

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

        return new AuthResponse(token);
    }
}
