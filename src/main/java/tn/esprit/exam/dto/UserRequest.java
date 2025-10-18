package tn.esprit.exam.dto;

public record UserRequest(
        String email,
        String username,
        String password,
        String bio,
        String defaultVisibility
) {
}
