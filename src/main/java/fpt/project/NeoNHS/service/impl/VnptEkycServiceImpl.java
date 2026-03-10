package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.request.kyc.KycRequest;
import fpt.project.NeoNHS.dto.response.kyc.KycResponse;
import fpt.project.NeoNHS.service.VnptEkycService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class VnptEkycServiceImpl implements VnptEkycService {

    private static final String BASE_URL = "https://api.idg.vnpt.vn";

    @Value("${vnpt.ekyc.access-token}")
    private String accessToken;

    @Value("${vnpt.ekyc.token-id}")
    private String tokenId;

    @Value("${vnpt.ekyc.token-key}")
    private String tokenKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private String getCleanAccessToken() {
        String token = accessToken.trim();
        if (token.toLowerCase().startsWith("bearer ")) {
            token = token.substring(7).trim();
        }
        return token;
    }

    @Override
    public String getAccessToken() {
        log.info("Using VNPT eKYC access token from config");
        return accessToken;
    }

    @Override
    public KycResponse performKyc(KycRequest request) {
        log.info("Performing VNPT eKYC verification (REAL API)...");

        try {
            // ---- Step 0: Upload ảnh lên VNPT File Service ----
            log.info("Uploading front image...");
            String frontHash = uploadBase64Image(request.getFrontImageBase64(), "cccd_front.jpg");
            log.info("Front image hash: {}", frontHash);

            log.info("Uploading back image...");
            String backHash = uploadBase64Image(request.getBackImageBase64(), "cccd_back.jpg");
            log.info("Back image hash: {}", backHash);

            log.info("Uploading selfie image...");
            String selfieHash = uploadBase64Image(request.getSelfieImageBase64(), "selfie.jpg");
            log.info("Selfie image hash: {}", selfieHash);

            // ---- Step 1: OCR CCCD mặt trước ----
            String clientSession = generateClientSession();

            Map<String, Object> ocrFrontBody = new HashMap<>();
            ocrFrontBody.put("img_front", frontHash);
            ocrFrontBody.put("client_session", clientSession);
            ocrFrontBody.put("token", tokenId);
            ocrFrontBody.put("type", -1); // -1 = auto detect CMT cũ/mới/CCCD

            HttpEntity<Map<String, Object>> ocrFrontEntity = new HttpEntity<>(ocrFrontBody, buildAiHeaders());

            log.info("Calling OCR front API...");
            ResponseEntity<Map> ocrFrontResponse = restTemplate.exchange(
                    BASE_URL + "/ai/v1/ocr/id/front",
                    HttpMethod.POST,
                    ocrFrontEntity,
                    Map.class);

            Map<String, Object> ocrFrontData = ocrFrontResponse.getBody();
            log.info("OCR front response: {}", ocrFrontData);

            // ---- Step 2: OCR CCCD mặt sau ----
            Map<String, Object> ocrBackBody = new HashMap<>();
            ocrBackBody.put("img_back", backHash);
            ocrBackBody.put("client_session", clientSession);
            ocrBackBody.put("token", tokenId);
            ocrBackBody.put("type", -1); // -1 = auto detect CMT cũ/mới/CCCD

            HttpEntity<Map<String, Object>> ocrBackEntity = new HttpEntity<>(ocrBackBody, buildAiHeaders());

            log.info("Calling OCR back API...");
            ResponseEntity<Map> ocrBackResponse = restTemplate.exchange(
                    BASE_URL + "/ai/v1/ocr/id/back",
                    HttpMethod.POST,
                    ocrBackEntity,
                    Map.class);

            Map<String, Object> ocrBackData = ocrBackResponse.getBody();
            log.info("OCR back response: {}", ocrBackData);

            // ---- Step 3: Face compare (CCCD vs Selfie) ----
            Map<String, Object> faceBody = new HashMap<>();
            faceBody.put("img_front", frontHash);
            faceBody.put("img_face", selfieHash);
            faceBody.put("client_session", clientSession);
            faceBody.put("token", tokenId);

            HttpEntity<Map<String, Object>> faceEntity = new HttpEntity<>(faceBody, buildAiHeaders());

            log.info("Calling face compare API...");
            ResponseEntity<Map> faceResponse = restTemplate.exchange(
                    BASE_URL + "/ai/v1/face/compare",
                    HttpMethod.POST,
                    faceEntity,
                    Map.class);

            Map<String, Object> faceData = faceResponse.getBody();
            log.info("Face compare response: {}", faceData);

            // ---- Parse kết quả ----
            String fullName = extractNestedString(ocrFrontData, "object", "name");
            String idNumber = extractNestedString(ocrFrontData, "object", "id");
            String dob = extractNestedString(ocrFrontData, "object", "birth_day");
            String address = extractNestedString(ocrFrontData, "object", "recent_location");
            Double faceScore = extractNestedDouble(faceData, "object", "prob");

            return KycResponse.builder()
                    .success(true)
                    .message("KYC verification successful")
                    .fullName(fullName != null ? fullName : "")
                    .idNumber(idNumber != null ? idNumber : "")
                    .dateOfBirth(dob)
                    .address(address)
                    .faceMatchScore(faceScore)
                    .isFake(false)
                    .build();

        } catch (Exception e) {
            log.error("VNPT eKYC verification failed: {}", e.getMessage(), e);
            return KycResponse.builder()
                    .success(false)
                    .message("KYC verification failed: " + e.getMessage())
                    .build();
        }
    }

    private String uploadBase64Image(String base64Data, String filename) {
        byte[] imageBytes = Base64.getDecoder().decode(base64Data);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(getCleanAccessToken());
        headers.set("Token-id", tokenId);
        headers.set("Token-key", tokenKey);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        });
        body.add("title", filename);
        body.add("description", "eKYC upload");

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                BASE_URL + "/file-service/v1/addFile",
                HttpMethod.POST,
                entity,
                Map.class);

        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null && responseBody.containsKey("object")) {
            Object obj = responseBody.get("object");
            if (obj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> objMap = (Map<String, Object>) obj;
                return (String) objMap.get("hash");
            } else if (obj instanceof String) {
                return (String) obj;
            }
        }

        throw new RuntimeException("Failed to upload image, response: " + responseBody);
    }

    /**
     * Build headers cho các AI API calls
     */
    private HttpHeaders buildAiHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getCleanAccessToken());
        headers.set("Token-id", tokenId);
        headers.set("Token-key", tokenKey);
        headers.set("mac-address", "TEST1");
        return headers;
    }

    /**
     * Generate client_session theo format VNPT yêu cầu:
     * <PLATFORM>_<model>_<OS>_<DeviceType>_<SDK_ver>_<Device_id>_<Timestamp>
     */
    private String generateClientSession() {
        return "WEB_NeoNHS_Windows_PC_1.0_NeoNHS_" + System.currentTimeMillis();
    }

    /**
     * Extract nested string from response: response.object.key
     */
    @SuppressWarnings("unchecked")
    private String extractNestedString(Map<String, Object> response, String objectKey, String fieldKey) {
        if (response == null)
            return null;
        Object obj = response.get(objectKey);
        if (obj instanceof Map) {
            Map<String, Object> dataMap = (Map<String, Object>) obj;
            Object val = dataMap.get(fieldKey);
            return val != null ? val.toString() : null;
        }
        return null;
    }

    /**
     * Extract nested double from response: response.object.key
     */
    @SuppressWarnings("unchecked")
    private Double extractNestedDouble(Map<String, Object> response, String objectKey, String fieldKey) {
        if (response == null)
            return null;
        Object obj = response.get(objectKey);
        if (obj instanceof Map) {
            Map<String, Object> dataMap = (Map<String, Object>) obj;
            Object val = dataMap.get(fieldKey);
            if (val instanceof Number) {
                return ((Number) val).doubleValue();
            }
            if (val instanceof String) {
                try {
                    return Double.parseDouble((String) val);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return null;
    }
}
