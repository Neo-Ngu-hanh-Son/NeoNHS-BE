package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.exception.InvalidTokenException;
import fpt.project.NeoNHS.exception.OTPException;
import fpt.project.NeoNHS.helpers.HashingHelper;
import fpt.project.NeoNHS.helpers.RedisAuthKeys;
import fpt.project.NeoNHS.service.RedisAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisAuthServiceImpl implements RedisAuthService {

    private static final int OTP_TTL_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;
    private static final int REFRESH_TOKEN_TTL_DAYS = 30;

    private final StringRedisTemplate redis;

    public void saveOtp(String email, String otp) {
        redis.opsForValue().set(
                RedisAuthKeys.otp(email),
                otp,
                OTP_TTL_MINUTES,
                TimeUnit.MINUTES
        );
    }

    public void verifyOtp(String email, String otp) {
        if (isBlocked(email)) {
            throw new OTPException("Too many failed attempts. Please request a new OTP.");
        }

        String key = RedisAuthKeys.otp(email);
        String stored = redis.opsForValue().get(key);

        if (stored == null) throw new OTPException("OTP has expired. Please request a new one.");

        // Clear both keys and attempts
        if (stored.equals(otp)) {
            redis.delete(key);
            redis.delete(RedisAuthKeys.otpAttempts(email));
            return;
        }
        incrementAttempts(email);

        throw new OTPException("Invalid OTP. Please try again.");
    }

    public boolean isBlocked(String email) {
        String attempts = redis.opsForValue()
                .get(RedisAuthKeys.otpAttempts(email));

        return attempts != null && Integer.parseInt(attempts) >= MAX_ATTEMPTS;
    }

    private void incrementAttempts(String email) {
        String key = RedisAuthKeys.otpAttempts(email);

        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1) {
            redis.expire(key, OTP_TTL_MINUTES, TimeUnit.MINUTES);
        }
    }

    public void verifyRefreshToken(String incomingToken) {
        String key = getRefreshTokenKey(HashingHelper.sha256(incomingToken.trim()));
        Boolean exists = redis.hasKey(key);
        if (exists == null || !exists) {
            throw new InvalidTokenException();
        }
    }

    @Override
    public String getUserIdFromRefreshToken(String refreshToken) {
        String key = getRefreshTokenKey(HashingHelper.sha256(refreshToken));
        String userId = redis.opsForHash().get(key, "userId").toString();
        if (userId == null) {
            throw new InvalidTokenException();
        }
        return userId;
    }

    public String getRefreshTokenKey(String token) {
        return RedisAuthKeys.refreshToken(token);
    }

    public void saveRefreshToken(String token, String userId, String sessionId) {
        String key = getRefreshTokenKey(HashingHelper.sha256(token).trim());

        Map<String, String> data = Map.of(
                "userId", userId,
                "sessionId", sessionId,
                "issuedAt", String.valueOf(System.currentTimeMillis())
        );

        redis.opsForHash().putAll(key, data);
        redis.expire(key, REFRESH_TOKEN_TTL_DAYS, TimeUnit.DAYS);
    }

    public void deleteRefreshToken(String token) {
        redis.delete(getRefreshTokenKey(HashingHelper.sha256(token)));
    }
}
