package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.upload.ImageUploadResponse;
import fpt.project.NeoNHS.service.ImageUploadService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/public/upload")
@RequiredArgsConstructor
@Tag(name = "Upload resource", description = "Upload images and videos to a service (Cloudinary)")
public class UploadController {
    private final ImageUploadService uploadService;

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ImageUploadResponse>> uploadImage(@RequestParam MultipartFile imageFile) {
        var res = uploadService.uploadImage(imageFile);
        return new ResponseEntity<>(
                ApiResponse.<ImageUploadResponse>builder()
                        .data(res)
                        .message("Upload successful")
                        .success(true)
                        .build(),
                HttpStatus.CREATED);
    }

    @PostMapping(value = "/image-url")
    public ResponseEntity<ApiResponse<ImageUploadResponse>> uploadImage(@RequestBody String url) {
        var res = uploadService.uploadImageFromUrl(url);
        return new ResponseEntity<>(
                ApiResponse.<ImageUploadResponse>builder()
                        .data(res)
                        .message("Upload successful")
                        .success(true)
                        .build(),
                HttpStatus.CREATED);
    }

    @PostMapping(value = "/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadVideo(@RequestParam MultipartFile videoFile) {
        var res = uploadService.uploadVideo(videoFile);
        return new ResponseEntity<>(
                ApiResponse.<String>builder()
                        .data(res)
                        .message("Upload successful")
                        .success(true)
                        .build(),
                HttpStatus.CREATED);
    }

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<ImageUploadResponse>>> uploadImages(@RequestParam MultipartFile[] imageFiles) {
        var res = uploadService.uploadImages(imageFiles);
        return new ResponseEntity<>(
                ApiResponse.<List<ImageUploadResponse>>builder()
                        .data(res)
                        .message("Batch upload successful")
                        .success(true)
                        .build(),
                HttpStatus.CREATED);
    }

    @DeleteMapping(value = "/image/{publicId}")
    public ResponseEntity<ApiResponse<String>> deleteResource(@PathVariable String publicId) {
        var res = uploadService.deleteResource(publicId);
        return new ResponseEntity<>(
                ApiResponse.<String>builder()
                        .data(res)
                        .message("Deletion successful")
                        .success(true).build(),
                HttpStatus.OK);
    }
}
