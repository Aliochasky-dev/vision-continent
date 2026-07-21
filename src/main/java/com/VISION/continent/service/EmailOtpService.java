package com.VISION.continent.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailOtpService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Votre code de vérification VISION");
            message.setText("""
                Bonjour,

                Votre code OTP pour VISION est : %s

                Ce code est valable pendant 10 minutes.

                Cordialement,
                L'équipe VISION
                """.formatted(otp));

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'email OTP : " + e.getMessage());
            throw new RuntimeException("Impossible d'envoyer l'email OTP");
        }
    }
}