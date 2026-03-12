package fpt.project.NeoNHS.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import fpt.project.NeoNHS.exception.AppIOException;
import fpt.project.NeoNHS.service.ImageUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryImageUploadServiceImpl implements ImageUploadService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadImage(MultipartFile file) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.emptyMap()
            );

            String secureUrl = (String) uploadResult.get("secure_url");
            log.info("Successfully uploaded image to Cloudinary. URL: {}", secureUrl);
            return secureUrl;
        } catch (IOException e) {
            log.error("Failed to upload image to Cloudinary", e);
            throw new AppIOException("Failed to upload image: " + e.getMessage());
        }
    }

    @Override
    public List<String> uploadImages(MultipartFile[] files) {
        return Arrays.stream(files)
                .map(this::uploadImage)
                .collect(Collectors.toList());
    }

    public String uploadVideo(MultipartFile file) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("resource_type", "video")
            );

            String secureUrl = (String) uploadResult.get("secure_url");
            log.info("Successfully uploaded video / audio to Cloudinary. URL: {}", secureUrl);
            return secureUrl;
        } catch (IOException e) {
            log.error("Failed to upload image to Cloudinary", e);
            throw new AppIOException("Failed to upload image: " + e.getMessage());
        }
    }
}
