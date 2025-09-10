package tn.esprit.exam.service;

import org.springframework.web.multipart.MultipartFile;
import tn.esprit.exam.entity.Media;
import tn.esprit.exam.entity.MediaKind;

import java.io.IOException;
import java.util.UUID;

public interface IMediaService {
    Media uploadMedia(UUID postId, MultipartFile file, MediaKind type) throws IOException;
}
