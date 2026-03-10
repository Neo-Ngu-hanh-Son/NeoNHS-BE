package fpt.project.NeoNHS.service;

public interface RedisAuthService {
    void saveOtp(String email, String otp);

    void verifyOtp(String email, String otp);

    boolean isBlocked(String email);
    String getRefreshTokenKey(String token);
    void saveRefreshToken(String token, String userId, String sessionId);
    void deleteRefreshToken(String token);
    void verifyRefreshToken(String incomingToken);

    String getUserIdFromRefreshToken(String refreshToken);
}
