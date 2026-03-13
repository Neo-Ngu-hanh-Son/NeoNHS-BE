package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.service.FaceVerificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import java.util.Map;

@Slf4j
@Service
public class FaceVerificationServiceImpl implements FaceVerificationService {

    @Value("${face-service.base-url}")
    private String faceServiceBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String extractEmbedding(String imageBase64) {
        log.info("Calling Face-Service /api/face/extract ...");

        byte[] imageBytes = Base64.getDecoder().decode(imageBase64);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return "selfie.jpg";
            }
        });

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                faceServiceBaseUrl + "/api/face/extract",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || !Boolean.TRUE.equals(responseBody.get("success"))) {
            throw new RuntimeException("Face extraction failed: " + responseBody);
        }

        // embedding is a List<Double> in the response
        Object embeddingObj = responseBody.get("embedding");
        if (embeddingObj == null) {
            throw new RuntimeException("No embedding returned from Face-Service");
        }

        // Convert to JSON string for storage
        String embeddingJson;
        if (embeddingObj instanceof List) {
            embeddingJson = embeddingObj.toString();
        } else {
            embeddingJson = embeddingObj.toString();
        }

        log.info("Face embedding extracted successfully (512-dim)");
        return embeddingJson;
    }

    @Override
    public boolean compareFaces(String livePhotoBase64, String storedEmbedding) {
        log.info("Calling Face-Service /api/face/compare ...");

        byte[] imageBytes = Base64.getDecoder().decode(livePhotoBase64);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return "live_photo.jpg";
            }
        });
        body.add("stored_embedding", storedEmbedding);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                faceServiceBaseUrl + "/api/face/compare",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {
                });

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || !Boolean.TRUE.equals(responseBody.get("success"))) {
            throw new RuntimeException("Face comparison failed: " + responseBody);
        }

        boolean isMatch = Boolean.TRUE.equals(responseBody.get("is_match"));
        Object similarity = responseBody.get("similarity");

        log.info("Face comparison result: match={}, similarity={}", isMatch, similarity);
        return isMatch;
    }
}
