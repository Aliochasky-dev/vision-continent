package com.VISION.continent.controller;

import com.VISION.continent.dtos.ApiResponse;
import com.VISION.continent.dtos.AuthDto;
import com.VISION.continent.dtos.ForgotPasswordRequestDto;
import com.VISION.continent.dtos.ResetPasswordDto;
import com.VISION.continent.service.AuthService;
import com.VISION.continent.service.ResetPasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final ResetPasswordService resetPasswordService;

    /**
     * POST /api/auth/register
     * Body : { nom, prenom, telephone, email, pin, villeCameroun }
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(
            @Valid @RequestBody AuthDto.RegisterRequestDto req) {
        String message = authService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(message, null));
    }

    /**
     * POST /api/auth/login
     * Body : { telephone, pin }
     */
    @PostMapping("/login")
    public ResponseEntity<AuthDto.AuthResponse> login(
            @Valid @RequestBody AuthDto.LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    /**
     * POST /api/auth/forgot-password
     * Body : { email }
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDto req) {
        resetPasswordService.sendResetToken(req.getEmail());
        return ResponseEntity.ok(ApiResponse.ok("Email de réinitialisation envoyé", null));
    }

    /**
     * POST /api/auth/reset-password
     * Body : { token, newPassword }
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordDto req) {
        resetPasswordService.resetPassword(req.getToken(), req.getNewPassword());
        return ResponseEntity.ok(ApiResponse.ok("Mot de passe réinitialisé avec succès", null));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<AuthDto.AuthResponse> verifyEmail(
            @Valid @RequestBody AuthDto.VerifyOtpRequest req) {
        return ResponseEntity.ok(authService.verifyEmail(req));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // enlève "Bearer "
        String message = authService.logout(token);
        return ResponseEntity.ok(ApiResponse.ok(message, null));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resendOtp(
            @Valid @RequestBody AuthDto.ResendOtpRequest req) {
        String message = authService.resendOtp(req);
        return ResponseEntity.ok(ApiResponse.ok(message, null));
    }
}