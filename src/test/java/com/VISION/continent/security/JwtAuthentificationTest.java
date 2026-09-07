package com.VISION.continent.security;

import com.VISION.continent.entity.User;
import com.VISION.continent.service.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Reproduit le scenario exact du bug 403 :
 * token dont le "sub" est le TELEPHONE et qui porte un claim "email",
 * alors que UserDetails#getUsername() renvoie le telephone.
 */
class JwtAuthentificationTest {

    private JwtUtil jwtUtil;
    private User admin;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "gRMn9O9XUnq5LqDGBYHv7zG0RwuHG4NZxJYpdEZsECg=");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", 86_400_000L);

        admin = User.builder()
                .id(1L)
                .nom("Alioch")
                .prenom("Nzimba")
                .telephone("656638229")
                .email("nzimbaalioch15@gmail.com")
                .password("{bcrypt}peu-importe")
                .role(User.Role.ADMIN)
                .statut(User.Statut.ACTIF)
                .build();
    }

    @Test
    @DisplayName("Un token emis pour un admin est valide face au UserDetails charge en base")
    void tokenAdminEstValide() {
        String token = jwtUtil.generateToken(admin);
        UserPrincipal principal = UserPrincipal.from(admin);

        // C'est precisement ce qui renvoyait false avant le correctif :
        // extractEmail(token) = email, getUsername() = telephone.
        assertTrue(jwtUtil.validateToken(token, principal),
                "Le token de l'admin doit etre reconnu comme valide");
    }

    @Test
    @DisplayName("L'identifiant extrait du token permet de retrouver l'utilisateur")
    void identifiantDuTokenCorrespondAuPrincipal() {
        String token = jwtUtil.generateToken(admin);

        assertEquals("656638229", jwtUtil.extractUsername(token));
        assertEquals("nzimbaalioch15@gmail.com", jwtUtil.extractEmail(token));
        assertEquals(1L, jwtUtil.extractUserId(token));
        assertEquals("ROLE_ADMIN", jwtUtil.extractRole(token));
    }

    @Test
    @DisplayName("Le principal expose les deux formes d'autorite : ADMIN et ROLE_ADMIN")
    void lesDeuxFormesDAutoriteSontExposees() {
        UserPrincipal principal = UserPrincipal.from(admin);

        assertTrue(principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN")));
        assertEquals(1L, principal.getId());
        assertEquals("656638229", principal.getUsername());
    }

    @Test
    @DisplayName("Un utilisateur simple n'obtient PAS l'autorite ADMIN")
    void utilisateurSimpleNaPasAdmin() {
        User simple = User.builder()
                .id(2L).nom("N").prenom("P")
                .telephone("690000000").email("user@vision.cm")
                .password("x").role(User.Role.USER).statut(User.Statut.ACTIF)
                .build();

        UserPrincipal principal = UserPrincipal.from(simple);

        assertTrue(principal.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().contains("ADMIN")),
                "Le filtre ne doit plus accorder ROLE_ADMIN a tout le monde");
    }

    @Test
    @DisplayName("Un token signe avec une autre cle est rejete")
    void tokenSigneAvecUneAutreCleEstRejete() {
        JwtUtil autre = new JwtUtil();
        ReflectionTestUtils.setField(autre, "secret", "une-toute-autre-cle-secrete-12345678");
        ReflectionTestUtils.setField(autre, "jwtExpirationMs", 86_400_000L);

        String tokenPirate = autre.generateToken(admin);

        assertFalse(jwtUtil.validateToken(tokenPirate, UserPrincipal.from(admin)));
    }

    @Test
    @DisplayName("Un token expire est rejete")
    void tokenExpireEstRejete() {
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", -1000L);
        String tokenExpire = jwtUtil.generateToken(admin);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", 86_400_000L);

        assertFalse(jwtUtil.validateToken(tokenExpire, UserPrincipal.from(admin)));
    }

    @Test
    @DisplayName("Un ancien token Google (sub = telephone, sans claim email) reste accepte")
    void ancienTokenSansEmailResteAccepte() {
        String ancien = jwtUtil.generateToken(admin.getTelephone(), "ADMIN");

        assertTrue(jwtUtil.validateToken(ancien, UserPrincipal.from(admin)));
        assertEquals("ROLE_ADMIN", jwtUtil.extractRole(ancien));
    }

    @Test
    @DisplayName("Un vieux token dont le sub est l'email reste accepte")
    void ancienTokenAvecEmailEnSubjectResteAccepte() {
        String ancien = jwtUtil.generateToken(admin.getEmail(), "ROLE_ADMIN");

        assertTrue(jwtUtil.validateToken(ancien, UserPrincipal.from(admin)));
    }
}
