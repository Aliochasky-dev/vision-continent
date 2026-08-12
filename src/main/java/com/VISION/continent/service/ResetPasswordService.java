package com.VISION.continent.service;

import com.VISION.continent.entity.ResetToken;
import com.VISION.continent.entity.User;
import com.VISION.continent.repository.ResetTokenRepository;
import com.VISION.continent.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ResetPasswordService {

    private final UserRepository userRepository;
    private final ResetTokenRepository resetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailOtpService emailOtpService;

    @Transactional
    public void sendResetToken(String email) {
        String cleanEmail = email.trim();

        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new RuntimeException("Email non trouvé"));

        resetTokenRepository.deleteByEmail(cleanEmail);

        String code = genererCode();
        ResetToken resetToken = new ResetToken(code, cleanEmail);
        resetTokenRepository.save(resetToken);

        String corps = "Bonjour,\n\n" +
                "Voici votre code de réinitialisation de mot de passe :\n\n" +
                code + "\n\n" +
                "Ce code est valable pendant 10 minutes.\n\n" +
                "Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.\n\n" +
                "L'équipe VISION";

        emailOtpService.sendGenericEmail(cleanEmail, "Code de réinitialisation - VISION", corps);

        System.out.println("=====================================");
        System.out.println("EMAIL : " + cleanEmail);
        System.out.println("CODE GÉNÉRÉ : " + code);
        System.out.println("=====================================");
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        String cleanToken = token.trim();

        ResetToken resetToken = resetTokenRepository.findByToken(cleanToken)
                .orElseThrow(() -> new RuntimeException("Code invalide ou expiré"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            resetTokenRepository.delete(resetToken);
            throw new RuntimeException("Code expiré");
        }

        User user = userRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        user.setPassword(passwordEncoder.encode(newPassword.trim()));
        userRepository.save(user);

        resetTokenRepository.delete(resetToken);
    }

    private String genererCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}