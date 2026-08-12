package com.VISION.continent.service;

import com.VISION.continent.dtos.GoogleOAuth2Dto;
import com.VISION.continent.entity.User;
import com.VISION.continent.repository.UserRepository;
import com.VISION.continent.service.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    /**
     * Authentifier ou créer un utilisateur via Google
     */
    public String authenticateWithGoogle(GoogleOAuth2Dto googleUser) {

        // Vérifier si l'utilisateur existe déjà par email
        Optional<User> existingUserOpt = userRepository.findByEmail(googleUser.getEmail());

        User user;

        if (existingUserOpt.isPresent()) {
            // Utilisateur existe déjà
            user = existingUserOpt.get();

            // Mise à jour du googleId s'il n'existe pas encore
            if (user.getGoogleId() == null || user.getGoogleId().isBlank()) {
                user.setGoogleId(googleUser.getId());
                userRepository.save(user);
            }
        } else {
            // === Création d'un nouvel utilisateur via Google ===
            user = User.builder()
                    .nom(googleUser.getNom() != null ? googleUser.getNom() : "")
                    .prenom(googleUser.getPrenom() != null ? googleUser.getPrenom() : "")
                    .email(googleUser.getEmail())
                    .telephone(generateTemporaryTelephone())   // Important : telephone est obligatoire
                    .googleId(googleUser.getId())
                    .password(passwordEncoder.encode(UUID.randomUUID().toString())) // Utilisation de pinHash
                    .role(User.Role.USER)
                    .statut(User.Statut.ACTIF)
                    .soldeFcfa(BigDecimal.ZERO)
                    .villeCameroun("Cameroun") // Valeur par défaut temporaire
                    .build();

            userRepository.save(user);
        }

        // Générer le token JWT
        // ⚠️ Attention : Vérifie la signature exacte de ta méthode generateToken dans JwtUtil
        return jwtUtil.generateToken(user.getTelephone(), user.getRole().name());
        // Ou selon ta méthode : jwtUtil.generateToken(user) ou avec UserDetails
    }

    /**
     * Génère un numéro de téléphone temporaire pour les utilisateurs Google
     * (car le champ telephone est obligatoire dans ta table)
     */
    private String generateTemporaryTelephone() {
        return "GOOGLE_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Méthode à implémenter plus tard pour extraire les infos du ID Token Google
     */
    public GoogleOAuth2Dto extractGoogleUserInfo(String idToken) {
        // TODO: Implémenter la vraie logique avec google-auth-library-java
        throw new UnsupportedOperationException("Méthode extractGoogleUserInfo non encore implémentée");
    }
}