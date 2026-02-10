package fpt.project.NeoNHS.helpers;

public final class RedisAuthKeys {
    private RedisAuthKeys() {}

    public static String otp(String email) {
        return "auth:otp:" + email;
    }

    public static String otpAttempts(String email) {
        return "auth:otp_attempts:" + email;
    }

    public static String emailVerifyToken(String token) {
        return "auth:email_verify:" + token;
    }

    public static String resetPasswordToken(String token) {
        return "auth:reset:" + token;
    }

    public static String refreshToken(String token) {
        return "auth:refresh_token:" + token;
    }
}
