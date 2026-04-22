package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.upload.ShortenImageRequest;
import fpt.project.NeoNHS.dto.response.upload.ImageUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.util.List;

public interface ImageUploadService {
    ImageUploadResponse uploadImage(MultipartFile file);
    ImageUploadResponse uploadImageFromUrl(ShortenImageRequest req);
    List<ImageUploadResponse> uploadImages(MultipartFile[] files);
    String uploadVideo(MultipartFile file);

    String deleteResource(String publicId);
}
