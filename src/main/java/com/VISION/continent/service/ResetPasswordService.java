package com.VISION.continent.service;

import com.VISION.continent.entity.ResetToken;
import com.VISION.continent.entity.User;
import com.VISION.continent.repository.ResetTokenRepository;
import com.VISION.continent.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResetPasswordService {

    private final UserRepository userRepository;
    private final ResetTokenRepository resetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailOtpService emailOtpService;

    public void sendResetToken(String email) {
        String cleanEmail = email.trim();

        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new RuntimeException("Email non trouvé"));

        String token = UUID.randomUUID().toString();
        ResetToken resetToken = new ResetToken(token, cleanEmail);
        resetTokenRepository.save(resetToken);

        String lien = "http://localhost:8080/reset-password?token=" + token;
        String corps = "Bonjour,\n\n" +
                "Cliquez sur ce lien pour réinitialiser votre mot de passe :\n" +
                lien + "\n\n" +
                "Ce lien expire dans 1 heure.\n\n" +
                "Si vous n'avez pas demandé cette réinitialisation, ignorez cet email.\n\n" +
                "L'équipe VISION";

        emailOtpService.sendGenericEmail(cleanEmail, "Réinitialisation de mot de passe - VISION", corps);

        System.out.println("=====================================");
        System.out.println("EMAIL : " + cleanEmail);
        System.out.println("TOKEN GÉNÉRÉ : " + token);
        System.out.println("LIEN : " + lien);
        System.out.println("=====================================");
    }

    public void resetPassword(String token, String newPassword) {
        String cleanToken = token.trim();

        ResetToken resetToken = resetTokenRepository.findByToken(cleanToken)
                .orElseThrow(() -> new RuntimeException("Token invalide ou expiré"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            resetTokenRepository.delete(resetToken);
            throw new RuntimeException("Token expiré");
        }

        User user = userRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        user.setPassword(passwordEncoder.encode(newPassword.trim()));
        userRepository.save(user);

        resetTokenRepository.delete(resetToken);
    }
}