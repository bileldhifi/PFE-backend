package tn.esprit.exam.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.exam.dto.MediaResponse;
import tn.esprit.exam.entity.Media;
import tn.esprit.exam.entity.MediaKind;
import tn.esprit.exam.entity.Post;
import tn.esprit.exam.repository.MediaRepository;
import tn.esprit.exam.repository.PostRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements IMediaService {

    private final MediaRepository mediaRepository;
    private final PostRepository postRepository;

    private final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    @Override
    public MediaResponse uploadMedia(UUID postId, MultipartFile file, MediaKind type) throws IOException {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(filename);
        file.transferTo(filePath.toFile());

        Media media = new Media();
        media.setPost(post);
        media.setType(type);
        media.setUrl("/uploads/" + filename);
        media.setSizeBytes(file.getSize());

        Media saved = mediaRepository.save(media);
        return new MediaResponse(saved.getId(), saved.getType(), saved.getUrl(),
                saved.getSizeBytes(), saved.getWidth(), saved.getHeight(), saved.getDurationS());
    }
}
