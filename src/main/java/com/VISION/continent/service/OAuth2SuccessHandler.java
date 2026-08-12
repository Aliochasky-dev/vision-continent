package com.VISION.continent.service;   // ou .security / .handler selon ton package

import com.VISION.continent.dtos.GoogleOAuth2Dto;
import com.VISION.continent.entity.User;
import com.VISION.continent.repository.UserRepository;
import com.VISION.continent.service.GoogleAuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final GoogleAuthService googleAuthService;
    private final UserRepository userRepository;   // Optionnel selon ton besoin

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // Extraction des informations Google
        String googleId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");

        // Création du DTO pour ton service
        GoogleOAuth2Dto googleOAuth2Dto = GoogleOAuth2Dto.builder()
                .id(googleId)
                .email(email)
                .prenom(firstName)
                .nom(lastName)
                .build();

        // Appel de ton service Google
        String jwtToken = googleAuthService.authenticateWithGoogle(googleOAuth2Dto);

        // Redirection avec le token dans l'URL (ou tu peux renvoyer en JSON)
        String targetUrl = "/api/auth/google/success?token=" + jwtToken;

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}