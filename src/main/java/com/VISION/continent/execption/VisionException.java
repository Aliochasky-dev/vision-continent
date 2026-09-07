package com.VISION.continent.execption;

import com.VISION.continent.dtos.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

public class VisionException extends RuntimeException {
    public VisionException(String message) {
        super(message);
    }
}

@RestControllerAdvice
class GlobalExceptionHandler {

    // ─── Exceptions métier ────────────────────────────────
    @ExceptionHandler(VisionException.class)
    public ResponseEntity<ApiResponse<Void>> handleVision(VisionException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ex.getMessage()));
    }


    // ─── Validation (@Valid) ──────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(msg));
    }

    // ─── UUID / type invalide → 400 (B3) ──────────────────
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = "Paramètre invalide : " + ex.getName();
        if (ex.getRequiredType() != null && ex.getRequiredType().equals(java.util.UUID.class)) {
            message = "Identifiant invalide (format UUID attendu)";
        }
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(message));
    }

    // ─── Accès refusé (rôle insuffisant) → 403 ────────────
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccess(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Accès refusé"));
    }

    // ─── Non authentifié / token invalide → 401 (B4) ──────
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Non authentifié ou session expirée"));
    }

    // ─── Toutes les autres erreurs → 500 sans fuite ───────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
        ex.printStackTrace(); // à remplacer plus tard par un logger
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Une erreur interne est survenue"));
    }
}