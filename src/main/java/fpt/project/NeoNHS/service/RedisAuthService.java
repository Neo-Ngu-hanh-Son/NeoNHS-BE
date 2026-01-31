package fpt.project.NeoNHS.service;

public interface RedisAuthService {
    void saveOtp(String email, String otp);

    boolean verifyOtp(String email, String otp);

    boolean isBlocked(String email);
}
