package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.helpers.RedisKeys;
import fpt.project.NeoNHS.service.RedisAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisAuthServiceImpl implements RedisAuthService {

    private static final int OTP_TTL_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;

    private final StringRedisTemplate redis;

    public void saveOtp(String email, String otp) {
        redis.opsForValue().set(
                RedisKeys.otp(email),
                otp,
                OTP_TTL_MINUTES,
                TimeUnit.MINUTES
        );
    }

    public boolean verifyOtp(String email, String otp) {
        String key = RedisKeys.otp(email);
        String stored = redis.opsForValue().get(key);

        if (stored == null) return false;

        if (stored.equals(otp)) {
            redis.delete(key);               // one-time use
            redis.delete(RedisKeys.otpAttempts(email));
            return true;
        }

        incrementAttempts(email);
        return false;
    }

    public boolean isBlocked(String email) {
        String attempts = redis.opsForValue()
                .get(RedisKeys.otpAttempts(email));

        return attempts != null && Integer.parseInt(attempts) >= MAX_ATTEMPTS;
    }

    private void incrementAttempts(String email) {
        String key = RedisKeys.otpAttempts(email);

        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1) {
            redis.expire(key, OTP_TTL_MINUTES, TimeUnit.MINUTES);
        }
    }
}
