package fpt.project.NeoNHS.helpers;

public final class RedisKeys {
    private RedisKeys() {}

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
}
