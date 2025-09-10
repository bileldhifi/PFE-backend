package tn.esprit.exam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.exam.entity.Media;

import java.util.UUID;

public interface MediaRepository extends JpaRepository<Media, UUID> {
}
