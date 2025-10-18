package tn.esprit.exam.service;

import org.springframework.web.multipart.MultipartFile;
import tn.esprit.exam.dto.MediaResponse;
import tn.esprit.exam.entity.Media;
import tn.esprit.exam.entity.MediaKind;

import java.io.IOException;
import java.util.UUID;

public interface IMediaService {
    MediaResponse uploadMedia(UUID postId, MultipartFile file, MediaKind type) throws IOException;
    MediaResponse uploadAvatar(MultipartFile file) throws IOException;
}
