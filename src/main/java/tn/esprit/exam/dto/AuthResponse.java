package tn.esprit.exam.dto;

public record AuthResponse(String accessToken,
                           String refreshToken,
                           UserResponse user) {
}
