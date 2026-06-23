package tn.esprit.exam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.exam.entity.ResetPasswordToken;
import tn.esprit.exam.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface ResetPasswordTokenRepository extends JpaRepository<ResetPasswordToken, UUID> {
    Optional<ResetPasswordToken> findByToken(String token);

    Optional<ResetPasswordToken> findTopByUserOrderByExpiryDateDesc(User user);

    Optional<ResetPasswordToken> findByUserAndCode(User user, String code);
}
