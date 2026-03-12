package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.service.impl.CloudinaryImageUploadServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Tag(name = "Upload resource", description = "Test upload resource with cloudinary")
public class UploadController {
    private final CloudinaryImageUploadServiceImpl cloudinaryImageUploadServiceImpl;

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadImage(@RequestParam MultipartFile imageFile) {
        var res = cloudinaryImageUploadServiceImpl.uploadImage(imageFile);
        return new ResponseEntity<>(
                ApiResponse.<String>builder()
                        .data(res)
                        .message("Upload successful")
                        .success(true)
                        .build(),
                HttpStatus.CREATED);
    }

    @PostMapping(value = "/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadVideo(@RequestParam MultipartFile videoFile) {
        var res = cloudinaryImageUploadServiceImpl.uploadVideo(videoFile);
        return new ResponseEntity<>(
                ApiResponse.<String>builder()
                        .data(res)
                        .message("Upload successful")
                        .success(true)
                        .build(),
                HttpStatus.CREATED);
    }

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<String>>> uploadImages(@RequestParam MultipartFile[] imageFiles) {
        var res = cloudinaryImageUploadServiceImpl.uploadImages(imageFiles);
        return new ResponseEntity<>(
                ApiResponse.<List<String>>builder()
                        .data(res)
                        .message("Batch upload successful")
                        .success(true)
                        .build(),
                HttpStatus.CREATED);
    }
}
