package fpt.project.NeoNHS.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import fpt.project.NeoNHS.dto.request.upload.ShortenImageRequest;
import fpt.project.NeoNHS.dto.response.upload.ImageUploadResponse;
import fpt.project.NeoNHS.exception.AppIOException;
import fpt.project.NeoNHS.service.ImageUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
    public ImageUploadResponse uploadImage(MultipartFile file) {
        long startTime = System.currentTimeMillis();
        log.info("[CloudinaryImageUploadService] Received file. Size: {} bytes", file.getSize());
        try {
            @SuppressWarnings("unchecked")
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            String secureUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            return ImageUploadResponse.builder()
                    .mediaUrl(secureUrl)
                    .publicId(publicId)
                    .build();
        } catch (IOException e) {
            log.error("[CloudinaryImageUploadService] Failed to upload image to Cloudinary", e);
            throw new AppIOException("Failed to upload image: " + e.getMessage());
        } finally {
            log.info("[CloudinaryImageUploadService] Total Request Time: {} ms", (System.currentTimeMillis() - startTime));
        }
    }

    @Override
    public ImageUploadResponse uploadImageFromUrl(ShortenImageRequest req) {
        log.info("[CloudinaryImageUploadService] Fetching and shortening external image URL: {}", req.getUrl());
        try {
            // Cloudinary uploader can take a URL string directly
            @SuppressWarnings("unchecked")
            Map uploadResult = cloudinary.uploader().upload(req.getUrl(), ObjectUtils.asMap(
                    "folder", "NeoNHS/Shortened",
                    "resource_type", "image"));

            String secureUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            return ImageUploadResponse.builder()
                    .mediaUrl(secureUrl)
                    .publicId(publicId)
                    .build();
        } catch (IOException e) {
            log.error("Failed to shorten image URL via Cloudinary", e);
            throw new AppIOException("Failed to shorten image URL: " + e.getMessage());
        }
    }

    @Override
    public List<ImageUploadResponse> uploadImages(MultipartFile[] files) {
        return Arrays.stream(files)
                .map(this::uploadImage)
                .collect(Collectors.toList());
    }

    public String uploadVideo(MultipartFile file) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("resource_type", "video"));

            String secureUrl = (String) uploadResult.get("secure_url");
            log.info("Successfully uploaded video / audio to Cloudinary. URL: {}", secureUrl);
            return secureUrl;
        } catch (IOException e) {
            log.error("Failed to upload image to Cloudinary", e);
            throw new AppIOException("Failed to upload image: " + e.getMessage());
        }
    }

    @Override
    public String deleteResource(String publicId) {
        try {
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            return (String) result.get("result");
        } catch (Exception e) {
            log.error("Failed to delete resource from Cloudinary. PublicId: {}", publicId);
            throw new AppIOException("Failed to delete resource: " + e.getMessage());
        }
    }
}
