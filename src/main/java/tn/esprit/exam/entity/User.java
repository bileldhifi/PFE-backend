package tn.esprit.exam.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "users")
public class User

{
    @Id
    @GeneratedValue
    UUID id;

    @Column(nullable = false, unique = true)
    String email;

    @Column(nullable = false, unique = true)
    String username;

    @JsonIgnore
    @Column(name = "password_hash", nullable = false)
    String passwordHash;

    @Enumerated(EnumType.STRING)
    Role role = Role.USER;   // Default role

    @Column(name = "default_visibility", nullable = false)
    String defaultVisibility = "PRIVATE";

    @Column(name = "created_at", nullable = false)
    OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(columnDefinition = "TEXT")
    String bio;

    @Column(name = "avatar_url")
    String avatarUrl;

    @Column(name = "trips_count")
    Integer tripsCount = 0;

    @Column(name = "steps_count")
    Integer stepsCount = 0;

    @Column(name = "followers_count")
    Integer followersCount = 0;

    @Column(name = "following_count")
    Integer followingCount = 0;
}
