package tn.esprit.exam.service;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.esprit.exam.dto.UserRequest;
import tn.esprit.exam.dto.UserResponse;
import tn.esprit.exam.entity.Role;
import tn.esprit.exam.entity.User;
import tn.esprit.exam.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements IUserService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UserResponse> retrieveAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public UserResponse retrieveUser(UUID userId) {
        return userRepository.findById(userId)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public UserResponse addUser(UserRequest request) {
        User user = new User();
        user.setEmail(request.email());
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        userRepository.save(user);
        return toDto(user);
    }

    @Override
    public UserResponse modifyUser(UUID userId, UserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Only update fields that are not null
        if (request.username() != null) {
            user.setUsername(request.username());
        }
        if (request.email() != null) {
            user.setEmail(request.email());
        }
        if (request.bio() != null) {
            user.setBio(request.bio());
        }
        // Note: Avatar is now handled through Media entity, not directly through UserRequest
        if (request.defaultVisibility() != null) {
            user.setDefaultVisibility(request.defaultVisibility());
        }
        
        userRepository.save(user);
        return toDto(user);
    }

    @Override
    public void removeUser(UUID userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public UUID getUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // 🔹 Utility mapper
    private UserResponse toDto(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getRole().name(),
                user.getDefaultVisibility(),
                user.getCreatedAt(),
                user.getBio(),
                user.getAvatarMedia() != null ? user.getAvatarMedia().getUrl() : null,
                user.getTripsCount(),
                user.getStepsCount(),
                user.getFollowersCount(),
                user.getFollowingCount()
        );
    }
}
