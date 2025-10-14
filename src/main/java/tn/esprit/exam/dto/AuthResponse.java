package tn.esprit.exam.dto;

public record AuthResponse(String accessToken,
                           UserResponse user) {
}
