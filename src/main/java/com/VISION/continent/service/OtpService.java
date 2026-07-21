package com.VISION.continent.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final RedisTemplate<String, String> redisTemplate;
    private final EmailOtpService emailOtpService;

    private static final int OTP_LENGTH = 6;
    private static final Duration OTP_EXPIRY = Duration.ofMinutes(10);

    public void generateAndSendOtp(String email) {
        String otp = generateOtp();
        String redisKey = "otp:email:" + email;

        redisTemplate.opsForValue().set(redisKey, otp, OTP_EXPIRY);
        emailOtpService.sendOtpEmail(email, otp);

        System.out.println("📧 OTP envoyé à " + email + " → " + otp); // à retirer en prod
    }

    public boolean verifyOtp(String email, String code) {
        String redisKey = "otp:email:" + email;
        String storedOtp = redisTemplate.opsForValue().get(redisKey);

        if (storedOtp == null) {
            throw new RuntimeException("Le code a expiré ou n'existe pas. Redemandez un nouveau code.");
        }

        if (!storedOtp.equals(code)) {
            return false;
        }

        redisTemplate.delete(redisKey);
        return true;
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
}