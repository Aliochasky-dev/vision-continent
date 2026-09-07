package com.VISION.continent.service;

import com.VISION.continent.entity.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.jsonwebtoken.*;


import javax.crypto.SecretKey;


@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms:86400000}") // 24 heures par défaut
    private long jwtExpirationMs;

    private Algorithm getAlgorithm() {
        return Algorithm.HMAC256(secret);
    }

    // Génère un token avec email + rôle
    public String generateToken(String email, String role) {
        return JWT.create()
                .withSubject(email)
                .withClaim("role", role)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .sign(getAlgorithm());
    }

    // Extrait l'email (subject)
    public String extractUsername(String token) {
        return JWT.decode(token).getSubject();
    }

    // Extrait le rôle
    public String extractRole(String token) {
        return JWT.decode(token).getClaim("role").asString();
    }

    // Vérifie si le token est valide
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            JWTVerifier verifier = JWT.require(getAlgorithm()).build();
            verifier.verify(token);

            String username = extractUsername(token);
            return username.equals(userDetails.getUsername());
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    // Vérifie si le token est expiré
    public boolean isTokenExpired(String token) {
        Date expiration = JWT.decode(token).getExpiresAt();
        return expiration.before(new Date());
    }
}