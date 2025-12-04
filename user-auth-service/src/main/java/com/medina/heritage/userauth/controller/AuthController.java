package com.medina.heritage.userauth.controller;

import com.medina.heritage.userauth.dto.request.ChangePasswordRequest;
import com.medina.heritage.userauth.dto.request.LoginRequest;
import com.medina.heritage.userauth.dto.request.RegisterRequest;
import com.medina.heritage.userauth.dto.response.ApiResponse;
import com.medina.heritage.userauth.dto.response.AuthResponse;
import com.medina.heritage.userauth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PutMapping("/password/{userId}")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable UUID userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader(value = "X-User-Id", required = false) UUID userId) {
        // Sans JWT/Spring Security, le logout est géré côté client
        // Ce endpoint sert à enregistrer l'action de déconnexion (pour audit/logs)
        authService.logout(userId);
        return ResponseEntity.ok(ApiResponse.success("Logout successful"));
    }
}
