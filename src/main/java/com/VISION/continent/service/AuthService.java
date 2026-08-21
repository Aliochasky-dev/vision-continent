package com.VISION.continent.service;

import com.VISION.continent.dtos.AuthDto;
import com.VISION.continent.entity.User;
import com.VISION.continent.execption.VisionException;
import com.VISION.continent.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final VisionUserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final LoginAttemptService loginAttemptService;
    private final OtpService otpService;
    private final TokenBlacklistService tokenBlacklistService;


    @Transactional
    public String register(AuthDto.RegisterRequestDto req) {
        if (userRepository.existsByTelephone(req.getTelephone())) {
            throw new VisionException("Ce numéro de téléphone est déjà utilisé");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new VisionException("Cet email est déjà utilisé");
        }
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new VisionException("Les mots de passe ne correspondent pas");
        }

        String nomComplet = req.getNomComplet().trim();
        int espace = nomComplet.indexOf(' ');
        String prenom = espace > 0 ? nomComplet.substring(0, espace) : nomComplet;
        String nom = espace > 0 ? nomComplet.substring(espace + 1).trim() : "";

        User user = User.builder()
                .nom(nom.isBlank() ? prenom : nom)
                .prenom(prenom)
                .telephone(req.getTelephone())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(User.Role.USER)
                .statut(User.Statut.ACTIF)
                .emailVerifie(false)
                .build();

        userRepository.save(user);

        otpService.generateAndSendOtp(user.getEmail());

        return "Compte créé. Un code de vérification a été envoyé à " + user.getEmail();
    }

    @Transactional
    public AuthDto.AuthResponse verifyEmail(AuthDto.VerifyOtpRequest req) {
        boolean valide = otpService.verifyOtp(req.getEmail(), req.getCode());
        if (!valide) {
            throw new VisionException("Code invalide");
        }

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new VisionException("Utilisateur introuvable"));

        user.setEmailVerifie(true);
        userRepository.save(user);

        // Génère le token avec l'ID utilisateur embarqué (claim "userId"),
        // au lieu de dépendre uniquement du téléphone (qui peut changer).
        String token = jwtUtil.generateToken(user);

        return AuthDto.AuthResponse.builder()
                .token(token)
                .telephone(user.getTelephone())
                .nomComplet(user.getPrenom() + " " + user.getNom())
                .role(user.getRole())
                .soldeFcfa(user.getSoldeFcfa())
                .build();
    }

    public String resendOtp(AuthDto.ResendOtpRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new VisionException("Utilisateur introuvable"));

        if (user.isEmailVerifie()) {
            throw new VisionException("Cet email est déjà vérifié");
        }

        otpService.generateAndSendOtp(user.getEmail());
        return "Un nouveau code a été envoyé à " + user.getEmail();
    }

    public AuthDto.AuthResponse login(AuthDto.LoginRequest req) {
        String identifiant = req.getIdentifiant();
        loginAttemptService.verifierBlocage(identifiant);

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            identifiant, req.getPassword()));
        } catch (BadCredentialsException e) {
            loginAttemptService.enregistrerEchec(identifiant, null);
            throw new VisionException("Identifiant ou mot de passe incorrect");
        }

        User user = trouverParTelephoneOuEmail(identifiant);

        if (user.getStatut() != User.Statut.ACTIF) {
            throw new VisionException("Compte suspendu ou banni. Contactez le support.");
        }

        if (!user.isEmailVerifie()) {
            throw new VisionException("Veuillez vérifier votre email avant de vous connecter");
        }

        loginAttemptService.reinitialiserEchecs(identifiant);

        // Génère le token avec l'ID utilisateur embarqué (claim "userId"),
        // au lieu de dépendre uniquement du téléphone (qui peut changer).
        String token = jwtUtil.generateToken(user);

        return AuthDto.AuthResponse.builder()
                .token(token)
                .telephone(user.getTelephone())
                .nomComplet(user.getPrenom() + " " + user.getNom())
                .role(user.getRole())
                .soldeFcfa(user.getSoldeFcfa())
                .build();
    }


    public String logout(String token) {
        String jti = jwtUtil.extractJti(token);
        Date expiration = jwtUtil.extractExpiration(token);

        Duration dureeRestante = Duration.between(java.time.Instant.now(), expiration.toInstant());
        tokenBlacklistService.revoquer(jti, dureeRestante);

        return "Déconnexion réussie";
    }

    private User trouverParTelephoneOuEmail(String identifiant) {
        return userRepository.findByTelephone(identifiant)
                .or(() -> userRepository.findByEmail(identifiant))
                .orElseThrow(() -> new VisionException("Utilisateur introuvable"));
    }
}