package fpt.project.NeoNHS.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryImageUploadServiceImplTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @InjectMocks
    private CloudinaryImageUploadServiceImpl imageUploadService;

    @BeforeEach
    void setUp() {
        // We defer stubbing cloudinary.uploader() to individual tests to avoid UnnecessaryStubbingException
    }

    @Test
    void uploadImage_Success() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "dummy image content".getBytes()
        );

        Map<String, Object> uploadResult = new HashMap<>();
        String expectedUrl = "https://res.cloudinary.com/demo/image/upload/v1/test-image.jpg";
        uploadResult.put("secure_url", expectedUrl);

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(uploadResult);

        // Act
        String actualUrl = imageUploadService.uploadImage(file);

        // Assert
        assertEquals(expectedUrl, actualUrl);
        verify(cloudinary, times(1)).uploader();
        verify(uploader, times(1)).upload(any(byte[].class), any(Map.class));
    }

    @Test
    void uploadImage_Failure() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "dummy image content".getBytes()
        );

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class)))
                .thenThrow(new IOException("Cloudinary upload failed"));

        // Act & Assert
        IOException exception = assertThrows(IOException.class, () -> {
            imageUploadService.uploadImage(file);
        });

        assertEquals("Failed to upload image", exception.getMessage());
        assertEquals("Cloudinary upload failed", exception.getCause().getMessage());
        
        verify(cloudinary, times(1)).uploader();
        verify(uploader, times(1)).upload(any(byte[].class), any(Map.class));
    }
}
