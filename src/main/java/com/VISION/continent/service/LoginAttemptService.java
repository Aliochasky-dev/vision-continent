package com.VISION.continent.service;

import com.VISION.continent.entity.LoginAttempt;
import com.VISION.continent.repository.LoginAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private static final int MAX_ECHECS       = 5;
    private static final int BLOCAGE_MINUTES  = 10;
    private static final int NETTOYAGE_JOURS  = 7;

    private final LoginAttemptRepository loginAttemptRepository;

    public void verifierBlocage(String identifier) {
        loginAttemptRepository.findByIdentifier(identifier).ifPresent(attempt -> {
            if (attempt.getBloqueJusquA() != null
                    && attempt.getBloqueJusquA().isAfter(LocalDateTime.now())) {

                long minutesRestantes = java.time.Duration
                        .between(LocalDateTime.now(), attempt.getBloqueJusquA())
                        .toMinutes() + 1;

                throw new RuntimeException(
                        "Trop de tentatives de connexion. " +
                                "Veuillez réessayer dans " + minutesRestantes + " minute(s)."
                );
            }
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enregistrerEchec(String identifier, String ipAddress) {
        LoginAttempt attempt = loginAttemptRepository
                .findByIdentifier(identifier)
                .orElse(LoginAttempt.builder()
                        .identifier(identifier)
                        .nbEchecs(0)
                        .build());

        if (attempt.getBloqueJusquA() != null
                && attempt.getBloqueJusquA().isBefore(LocalDateTime.now())) {
            attempt.setNbEchecs(0);
            attempt.setBloqueJusquA(null);
        }

        attempt.setNbEchecs(attempt.getNbEchecs() + 1);
        attempt.setDernierEchec(LocalDateTime.now());
        attempt.setIpAddress(ipAddress);

        if (attempt.getNbEchecs() >= MAX_ECHECS) {
            attempt.setBloqueJusquA(LocalDateTime.now().plusMinutes(BLOCAGE_MINUTES));
        }

        loginAttemptRepository.save(attempt);
    }

    @Transactional
    public void reinitialiserEchecs(String identifier) {
        loginAttemptRepository.findByIdentifier(identifier).ifPresent(attempt -> {
            attempt.setNbEchecs(0);
            attempt.setBloqueJusquA(null);
            attempt.setDernierEchec(null);
            loginAttemptRepository.save(attempt);
        });
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void nettoyerAnciennesEntrees() {
        loginAttemptRepository.deleteOldAttempts(
                LocalDateTime.now().minusDays(NETTOYAGE_JOURS));
    }
}