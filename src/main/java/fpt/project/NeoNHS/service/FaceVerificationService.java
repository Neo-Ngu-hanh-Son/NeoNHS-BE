package fpt.project.NeoNHS.service;

/**
 * Service to communicate with NeoNHS-Face-Service (Python microservice).
 * Handles face embedding extraction and comparison.
 */
public interface FaceVerificationService {

    /**
     * Extract 512-dim face embedding from a base64-encoded image.
     * Calls: POST /api/face/extract
     *
     * @param imageBase64 Base64-encoded image (selfie)
     * @return embedding as JSON string (e.g. "[0.123, -0.456, ...]")
     */
    String extractEmbedding(String imageBase64);

    /**
     * Compare a live photo against a stored embedding.
     * Calls: POST /api/face/compare
     *
     * @param livePhotoBase64 Base64-encoded live photo
     * @param storedEmbedding JSON string of 512-dim embedding
     * @return true if faces match (above threshold)
     */
    boolean compareFaces(String livePhotoBase64, String storedEmbedding);
}
