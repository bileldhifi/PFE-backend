package tn.esprit.exam.control;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.exam.dto.MediaResponse;
import tn.esprit.exam.entity.Media;
import tn.esprit.exam.entity.MediaKind;
import tn.esprit.exam.service.IMediaService;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
public class MediaController {

    private final IMediaService mediaService;

    @PostMapping(
            value = "/upload/{postId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public MediaResponse uploadMedia(@PathVariable UUID postId,
                                     @RequestPart("file") MultipartFile file,
                                     @RequestParam("type") MediaKind type) throws IOException {
        return mediaService.uploadMedia(postId, file, type);
    }
}
