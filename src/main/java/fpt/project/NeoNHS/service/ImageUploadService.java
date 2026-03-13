package fpt.project.NeoNHS.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

public interface ImageUploadService {
    String uploadImage(MultipartFile file);
    List<String> uploadImages(MultipartFile[] files);
    String uploadVideo(MultipartFile file);
}
